/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.orm.model;

import leap.core.exception.*;
import leap.core.metamodel.ReservedMetaFieldName;
import leap.core.transaction.TransactionCallback;
import leap.core.transaction.TransactionCallbackWithResult;
import leap.core.transaction.TransactionStatus;
import leap.core.validation.Errors;
import leap.core.validation.ValidatableBean;
import leap.core.value.Record;
import leap.db.Db;
import leap.lang.Args;
import leap.lang.Arrays2;
import leap.lang.Named;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.beans.DynaBean;
import leap.lang.collection.WrappedCaseInsensitiveMap;
import leap.lang.convert.Converts;
import leap.lang.expression.Expression;
import leap.lang.json.JsonIgnore;
import leap.lang.json.JsonStringable;
import leap.lang.json.JsonWriter;
import leap.lang.meta.annotation.ComplexType;
import leap.lang.params.NamedParamsBase;
import leap.lang.params.Params;
import leap.lang.tostring.ToStringBuilder;
import leap.orm.annotation.Instrument;
import leap.orm.callback.CreateCallback;
import leap.orm.callback.PostCreateCallback;
import leap.orm.callback.PostUpdateCallback;
import leap.orm.callback.UpdateCallback;
import leap.orm.command.InsertCommand;
import leap.orm.command.UpdateCommand;
import leap.orm.dao.Dao;
import leap.orm.dmo.Dmo;
import leap.orm.linq.Condition;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.model.ModelRegistry.ModelContext;
import leap.orm.query.CriteriaQuery;
import leap.orm.query.CriteriaWhere;
import leap.orm.query.EntityQuery;
import leap.orm.query.Query;
import leap.orm.value.Entity;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("unchecked")
@ComplexType
public abstract class Model implements DynaBean,ValidatableBean,JsonStringable {
	
	//This variable will be accessed by sub-model-class in the instrumented code, cannot change to private modifier.
	protected static final ThreadLocal<String> className = new ThreadLocal<String>();
	
	@Instrument
	public static <T extends Model> T newInstance() {
		ModelContext context = context();
		return (T)context.getBeanType().newInstance();
	}
	
	@Instrument
	public static <T extends Model> T newInstance(Object id) {
		ModelContext context = context();
		return ((T)context.getBeanType().newInstance()).id(id);
	}
	
	//---cmd---
	/**
	 * Creates a new {@link InsertCommand} command for creating a new record.
	 */
	@Instrument
	public static InsertCommand cmdInsert() {
		ModelContext context = context();
		return context.getDao().cmdInsert(context.getEntityMapping());
	}
	
	/**
	 * Creates a new {@link UpdateCommand} command for updating an exists record.
	 */
	@Instrument
	public static UpdateCommand cmdUpdate(Object id) {
		Args.notNull(id,"id");
		ModelContext context = context();
		return context.getDao().cmdUpdate(context.getEntityMapping()).id(id);
	}
	
	//---static create----
	@Instrument
	public static <T extends Model> T create(Map<String, Object> fields) {
		T m = newInstance();
		
		m.setAll(fields);
		m.create();
		
		return m;
	}
	
	//---static update---
	@Instrument
	public static boolean update(Object id,Map<String, Object> fields) {
		return cmdUpdate(id).setAll(fields).execute() > 0;
	}
	
	@Instrument
	public static boolean update(Object id,String field,Object value) {
		return cmdUpdate(id).set(field, value).execute() > 0;
	}
	
	@Instrument
	public static int updateAll(Map<String, Object> fields, String whereExpression, Object... args) {
		return query().where(whereExpression,args).update(fields);
	}
	
	//---static delete---
	@Instrument
	public static void delete(Object id) throws RecordNotDeletedException{
		ModelContext context = context();
		EntityMapping em = context.getEntityMapping();
		if(context.getDao().delete(em, id) <= 0) {
			throw new RecordNotDeletedException("Record not deleted, checks is record exists or failed to delete?");
		}
	}
	
	@Instrument
	public static boolean tryDelete(Object id) throws RecordNotDeletedException{
		ModelContext context = context();
		EntityMapping em = context.getEntityMapping();
		return context.getDao().delete(em, id) > 0;
	}
	
	/**
	 * Deletes all the records of this model.
	 *
	 * <p/>
	 *
	 * Warning!!! Be careful to use the method.
	 */
	@Instrument
	public static int deleteAll() {
		ModelContext context = context();
		EntityMapping em = context.getEntityMapping();
		return context.getDao().deleteAll(em);
	}
	
	/**
	 * Deletes all the records in the given id array.
	 */
	@Instrument
	public static int[] deleteAll(Object[] ids) {
		ModelContext context = context();
		EntityMapping em = context.getEntityMapping();
		return context.getDao().batchDelete(em, ids);
	}
	
	/**
	 * Delete all the records with the given where expression.
	 */
	@Instrument
	public static int deleteAll(String whereExpression) {
		return query().where(whereExpression).delete();
	}
	
	/**
	 * Delete all the records with the given lambda condition.
	 */
	@Instrument
	public static <T extends Model> int deleteAll(Condition<T> condition) {
		return ((CriteriaQuery<T>)query()).where(condition).delete();
	}
	
	/**
	 * Delete all the records with the given where expression.
	 */
	@Instrument
	public static int deleteAll(String whereExpression,Object... args) {
		return query().where(whereExpression, args).delete();
	}
	
	/**
	 * Delete all the records with the given where expression.
	 */
    @Instrument
    @SuppressWarnings("rawtypes")
	public static int deleteAll(String whereExpression,Map<String, Object> params) {
		return ((CriteriaQuery)query().where(whereExpression).params(params)).delete();
	}
	
	@Instrument
	public static int deleteBy(String field,Object value) {
		return query().where(field + "=?",value).delete();
	}
	
	//---count---
	@Instrument
	public static long count() {
		return query().count();
	}
	
	//---find---
	/**
	 * Returns the record for the id.
	 * 
	 * @throws RecordNotFoundException if record not exists.
	 */
	@Instrument
	public static <T extends Model> T find(Object id) {
		ModelContext context = context();
		EntityMapping em = context.getEntityMapping();
		return (T)context.getDao().find(em.getEntityName(), em.getModelClass(), id);
	}
	
    /**
     * Returns the record for the id.
     * 
     * <p>
     * Returns <code>null</code> if not exists.
     */
    @Instrument
    public static <T extends Model> T findOrNull(Object id) {
        ModelContext context = context();
        EntityMapping em = context.getEntityMapping();
        return (T)context.getDao().findOrNull(em.getEntityName(), em.getModelClass(), id);
    }
	
	/**
	 * Returns a {@link List} contains all the records for the give id array.
	 * 
	 * @throws RecordNotFoundException if the returned size less than the id array's size.
	 */
	@Instrument
	public static <T extends Model> List<T> findList(Object[] ids) {
		ModelContext context = context();
		EntityMapping em = context.getEntityMapping();
		return (List<T>)context.getDao().findList(em, em.getModelClass(), ids);
	}
	
	/**
     * Returns a {@link List} contains the exists records for the give id array.
     * 
     * <p>
     * Do not throws {@link RecordNotFoundException} if the some of record(s) not exists.
     */
    @Instrument
    public static <T extends Model> List<T> findListIfExists(Object[] ids) {
        ModelContext context = context();
        EntityMapping em = context.getEntityMapping();
        return (List<T>) context.getDao().findListIfExists(em, em.getModelClass(), ids);
    }
	
	@Instrument
	public static <T extends Model> T findBy(String field,Object value) throws EmptyRecordsException,TooManyRecordsException {
		return (T)query().where(field + "=?",value).singleOrNull();
	}

	/**
	 * Returns all the records or the model.
     */
	@Instrument
	public static <T extends Model> List<T> findAll() {
		return (List<T>)query().list();
	}
	
	/**
	 * Returns all the records. Same as {@link #findAll}.
	 */
	@Instrument
	public static <T extends Model> List<T> all(){
		return (List<T>)query().list();
	}
	
	//---first,last---
	/**
	 * Returns the first record.
	 * 
	 * @throws EmptyRecordsException if no records exists.
	 */
	@Instrument
	public static <T extends Model> T first() {
		return (T)query().orderByIdAsc().first();
	}
	
	@Instrument
	public static <T extends Model> List<T> first(int num) {
		Args.assertTrue(num > 0,"num must be > 0");
		return (List<T>)query().orderByIdAsc().limit(num).list();
	}
	
	@Instrument
	public static <T extends Model> T firstOrNull() {
		return (T)query().orderByIdAsc().firstOrNull();
	}
	
	@Instrument
	public static <T extends Model> T last() {
		return (T)query().orderByIdDesc().first();
	}
	
	@Instrument
	public static <T extends Model> List<T> last(int num) {
		Args.assertTrue(num > 0,"num must be > 0");
		return (List<T>)query().orderByIdDesc().limit(num).list();
	}
	
	@Instrument
	public static <T extends Model> T lastOrNull() {
		return (T)query().orderByIdDesc().firstOrNull();
	}
	
	@Instrument
	public static <T extends Model> List<T> lastCreated(int num) {
		Args.assertTrue(num > 0,"num must be > 0");
		return (List<T>)query().lastCreated(num).list();
	}
	
	@Instrument
	public static <T extends Model> List<T> lastUpdated(int num) {
		Args.assertTrue(num > 0,"num must be > 0");
		return (List<T>)query().lastUpdated().list();
	}
	
	//---query---
	
	/**
	 * Returns a {@link Query} object for querying records use the sql statement or sql key. 
	 */
	@Instrument
	public static <T extends Model> EntityQuery<T> query(String sqlOrKey) {
		Args.notEmpty(sqlOrKey,"sqlOrKey");
		ModelContext context = context();
		EntityMapping em = context.getEntityMapping();
		if(sqlOrKey.indexOf(' ') < 0) {
			return (EntityQuery<T>)context.getDao().createNamedQuery(em, em.getModelClass(),sqlOrKey);
		}else{
			return (EntityQuery<T>)context.getDao().createSqlQuery(em, em.getModelClass(),sqlOrKey);
		}
	}
	
	/**
	 * Returns a {@link CriteriaQuery} object for querying records without where condition.
	 */
	@Instrument
	public static <T extends Model> CriteriaQuery<T> query() {
		ModelContext context = context();
		EntityMapping em = context.getEntityMapping();
		return (CriteriaQuery<T>)context.getDao().createCriteriaQuery(em,em.getModelClass());
	}
	
	/**
	 * Returns a {@link CriteriaWhere} object for building where condition.
	 */
	@Instrument
	public static <T extends Model> CriteriaWhere<T> where() {
		return (CriteriaWhere<T>)query().where();
	}
	
	/**
	 * Returns a {@link CriteriaQuery} object for querying records with the given lambda condition.
	 */
	@Instrument
	public static <T extends Model> CriteriaQuery<T> where(Condition<T> condition) {
		return ((CriteriaQuery<T>)query()).where(condition);
	}
	
	/**
	 * Returns a {@link CriteriaQuery} object for querying records with the given where condition.
	 */
	@Instrument
	public static <T extends Model> CriteriaQuery<T> where(String expression) {
		return where(expression,Arrays2.EMPTY_OBJECT_ARRAY);
	}
	
	@Instrument
	public static <T extends Model> CriteriaQuery<T> where(String expression,Object... args) {
		return (CriteriaQuery<T>)query().where(expression, args);
	}

    @Instrument
    public static <T extends Model> CriteriaQuery<T> where(String expression,Map<String,Object> params) {
        return (CriteriaQuery<T>)query().where(expression).params(params);
    }
	
	//------------------ batch -------------------------------------------
	@Instrument
	public static int[] createAll(Object[] records) {
		ModelContext context = context();
		EntityMapping em = context.getEntityMapping();
		return context.getDao().batchInsert(em, records);
	}

	@Instrument
	public static int[] updateAll(Object[] records) {
		ModelContext context = context();
		EntityMapping em = context.getEntityMapping();
		return context.getDao().batchUpdate(em, records);
	}
	
	//----------------- transaction ---------------------------------------
	@Instrument
	public static void doTransaction(TransactionCallback callback) {
		ModelContext context = context();
		context.getDao().doTransaction(callback);
	}
	
	@Instrument
	public static <T> T doTransaction(TransactionCallbackWithResult<T> callback) {
		ModelContext context = context();
		return context.getDao().doTransaction(callback);
	}
	
	@Instrument
	public static void doTransaction(TransactionCallback callback, boolean requiresNew) {
		ModelContext context = context();
		context.getDao().doTransaction(callback, requiresNew);
	}
	
	@Instrument
	public static <T> T doTransaction(TransactionCallbackWithResult<T> callback, boolean requiresNew) {
		ModelContext context = context();
		return context.getDao().doTransaction(callback, requiresNew);
	}
	
	//-------------------- static context methods ------------------------
	
	@Instrument
	public static Dao dao(){
		return context().getDao();
	}
	
	@Instrument
	public static Dmo dmo(){
		return context().getDmo();
	}
	
	@Instrument
	public static EntityMapping metamodel(){
		return context().getEntityMapping();
	}
	
	@Instrument
	public static Db db(){
		return context().getOrmContext().getDb();
	}
	
	@Instrument
	protected static ModelContext context(){
		return ModelRegistry.getModelContext(getClassName());
	}
	
	protected static ModelFieldValidation validates(String... fields){
		return new ModelFieldValidation(getClassName(), fields);
	}
	
	@Instrument
	protected static String className(){
		return getClassName();
	}
	
	protected static String getClassName(){
		String name = className.get();
		if(null == name){
			throw new IllegalStateException("Failed to determine Model class name, are you sure models have been instrumented?");
		}
		return name;
	}
	
	protected static void setClassName(String name){
		className.set(name);
	}
	
	//-------------------- non-static members ------------------------
	protected EntityMapping 	  em;
	protected Dao	        	  dao;
	protected Map<String, Object> fields = new WrappedCaseInsensitiveMap<Object>();
	
	private BeanType beanType;
	private Errors	 errors;
	
    public Model() {

    }

	public final String getEntityName() {
    	_init();
	    return em.getEntityName();
    }

    public final EntityMapping getEntityMapping() {
        _init();
        return em;
    }
    
    public final boolean contains(String field) {
	    return fields.containsKey(field);
    }

    public final Object get(String field) {
    	BeanProperty bp = beanType().tryGetProperty(field,true);
    	if(null != bp){
    		return bp.getValue(this);
    	}else{
    		return fields.get(field);
    	}
    }
	
    public final Object get(Named field) {
	    return get(field.getName());
    }
    
    public String getString(String field){
    	return Converts.toString(get(field));
    }
    
    public Integer getInteger(String field){
    	return Converts.convert(get(field), Integer.class);
    }
    
    public Map<String, Object> fields() {
		Map<String, Object> map = new WrappedCaseInsensitiveMap<>();
		
		map.putAll(fields);
		
    	BeanProperty[] props = beanType().getProperties();
    	for(int i=0;i<props.length;i++){
    		BeanProperty p = props[i];
    		if(p.isField()){
    			map.put(p.getName(),p.getValue(this));	
    		}
    	}
    	
	    return map;
    }
	
	/**
	 * Returns a {@link Errors} object represents the validation errors of this model object.
	 * 
	 * <p>
	 * Returns <code>null</code> if this model did not validated.
	 */
	public Errors errors(){
		return errors;
	}
	
	/**
	 * Returns the value of id.
	 */
	public final Object id(){
		_init();
		return doGetId();
	}
	
	/**
	 * Sets the value of id.
	 */
	public final <T extends Model> T id(Object id){
		_init();
		doSetId(id);
		return (T)this;
	}
	
	/**
	 * Sets the value of field.
	 */
	public final <T extends Model> T set(String field,Object value){
		if("id".equalsIgnoreCase(field)) {
			return id(value);
		}else{
			doSet(field, value);
			return (T)this;
		}
	}
	
	/**
	 * Returns the value of createdAt.
	 */
	public final Timestamp createdAt() {
		return Converts.convert(doGetReservedFieldValue(ReservedMetaFieldName.CREATED_AT), Timestamp.class);
	}
	
	/**
	 * Returns the value of updatedAt.
	 */
	public final Timestamp updatedAt() {
		return Converts.convert(doGetReservedFieldValue(ReservedMetaFieldName.UPDATED_AT), Timestamp.class);
	}
	
	public final <T extends Model> T set(Named field,Object value){
		set(field.getName(), value);
    	return (T)this;
    }
    
    public <T extends Model> T setAll(Map<String,? extends Object> fields){
    	Args.notNull(fields);
    	if(!fields.isEmpty()){
        	for(Entry<String, ?> entry : fields.entrySet()){
        		set(entry.getKey(), (Object)entry.getValue());
        	}
    	}
    	return (T)this;
    }
    
    /**
     * Insert a new record if id is<code>null</code> or update an exists record owherwise.
     * 
     * @see #id()
     */
    public final <T extends Model> T save() throws RecordNotSavedException {
    	_init();
    	doSave();
    	return (T)this;
    }
    
    public final boolean trySave() {
    	_init();
    	return tryDoSave();
    }

	/**
	 * Creates a new record. (Same as {@link #create()}
	 */
	public final <T extends Model> T insert() throws RecordNotSavedException {
		if(!tryCreate()) {
			throw new RecordNotSavedException("Record not created");
		}
		return (T)this;
	}
    
    /**
     * Creates a new record. (Same as {@link #insert()}
     */
    public final <T extends Model> T create() throws RecordNotSavedException {
    	if(!tryCreate()) {
    		throw new RecordNotSavedException("Record not created");
    	}
    	return (T)this;
    }
    
    /**
     * Creates a new record.
     */
    public final <T extends Model> void create(CreateCallback<T> callback) throws RecordNotSavedException {
    	if(!tryCreate(callback)) {
    		throw new RecordNotSavedException("Record not created");
    	}
    }
    
    /**
     * Creates a new record.
     */
    public final <T extends Model> void create(PostCreateCallback<T> callback) throws RecordNotSavedException {
    	if(!tryCreate(callback)) {
    		throw new RecordNotSavedException("Record not created");
    	}
    }
    
    /**
     * Creates a new record.
     */
    public final boolean tryCreate() {
    	_init();
    	return doCreate();
    }
    
    /**
     * Creates a new record.
     */
    public final <T extends Model> boolean tryCreate(CreateCallback<T> callback) {
    	_init();
    	
    	return dao.doTransaction((TransactionStatus s) -> {
	    		
    				callback.preCreate((T)this,s);
    		
    				if(s.isRollbackOnly()) {
    					return false;
    				}
    				
		    		boolean r = doCreate();
		    		
		    		if(r){
			    		callback.postCreate((T)this,s);
			    		
	    				if(s.isRollbackOnly()) {
	    					return false;
	    				}
		    		}
		    		
		    		return r;
		    	});
    }
    
    /**
     * Updates an exists record.
     * 
     * @throws IllegalStateException if no id exists.
     * @throws RecordNotSavedException if the record not updated.
     */
    public final <T extends Model> T update() throws RecordNotSavedException{
    	if(!tryUpdate()) {
    		throw new RecordNotSavedException("Record not updated");
    	}
    	return (T)this;
    }
    
    /**
     * Updates an exists record.
     * 
     * @throws IllegalStateException if no id exists.
     * @throws RecordNotSavedException if the record not updated.
     */
    public final <T extends Model> T update(UpdateCallback<T> callback) throws RecordNotSavedException{
    	if(!tryUpdate(callback)) {
    		throw new RecordNotSavedException("Record not updated");
    	}
    	return (T)this;
    }
    
    /**
     * Updates an exists record.
     * 
     * @throws IllegalStateException if no id exists.
     * @throws RecordNotSavedException if the record not updated.
     */
    public final <T extends Model> T update(PostUpdateCallback<T> callback) throws RecordNotSavedException{
    	if(!tryUpdate(callback)) {
    		throw new RecordNotSavedException("Record not updated");
    	}
    	return (T)this;
    }
    
    /**
     * Updates an exists record.
     * 
     * @throws IllegalStateException if no id exists.
     */
    public final boolean tryUpdate() throws IllegalStateException {
    	_init();
    	
    	Object id = id();
    	if(null == id){
    		throw new IllegalStateException("Cannot update, id is null");
    	}
    	
    	return doUpdate(id);
    }
    
    /**
     * Updates a exists record.
     */
    public final <T extends Model> boolean tryUpdate(UpdateCallback<T> callback) {
    	_init();
    	
    	Object id = id();
    	if(null == id){
    		throw new IllegalStateException("Cannot update, id is null");
    	}
    	
    	return dao.doTransaction((TransactionStatus s) -> {
	    		
    				callback.preUpdate((T)this,s);
    		
    				if(s.isRollbackOnly()) {
    					return false;
    				}
    				
		    		boolean r = doUpdate(id);
		    		
		    		if(r){
			    		callback.postUpdate((T)this,s);
			    		
	    				if(s.isRollbackOnly()) {
	    					return false;
	    				}
		    		}
		    		
		    		return r;
		    	});
    }
    
    /**
     * Updates all the records matched the where expression with all the fields.
     */
    public final int updateAll(String whereExpression) {
    	_init();
    	
    	return thisQuery().where(whereExpression).update(getUpdateFields());
    }
    
    /**
     * Updates all the records matched the where expression with all the fields.
     */
    public final <T extends Model> int updateAll(Condition<T> condition) {
    	return ((CriteriaQuery<T>)thisQuery()).where(condition).update(getUpdateFields());
    }
    
    /**
     * Updates all the records matched the where expression with all the fields.
     */
    public final int updateAll(String whereExpression,Object... args) {
    	return thisQuery().where(whereExpression,args).update(getUpdateFields());
    }
    
    /**
     * Updates all the records matched the where expression with all the fields.
     */
    @SuppressWarnings("rawtypes")
    public final int updateAll(String whereExpression,Map<String, Object> params) {
    	CriteriaQuery q = ((CriteriaQuery)thisQuery());
    	q.where(whereExpression).params(params);
    	return q.update(getUpdateFields());
    }
    
    private <T extends Model> CriteriaQuery<T> thisQuery() {
    	_init();
    	return (CriteriaQuery<T>)dao.createCriteriaQuery(em,em.getModelClass());
    }
    
    private Map<String, Object> getUpdateFields() {
    	Map<String, Object> map = new HashMap<String, Object>();

    	for(FieldMapping fm : em.getFieldMappings()) {
    		if(fm.isPrimaryKey() || !fm.isUpdatable()) {
    			continue;
    		}
    		
    		//TODO : should update optimistic lock ?
			if(fm.isOptimisticLock()){
				continue;
			}else{
				Object value = get(fm.getFieldName());
				
				if(null == value) {
					Expression expression = fm.getUpdateValue();
					if(null != expression){
						value = expression.getValue(this, fields);	
					}
				}
			
				map.put(fm.getFieldName(), value);
			}
    	}
    	
	    return map;
    }
    
    /**
     * Deletes current record
     * 
     * @throws RecordNotDeletedException if the record not exists or failed to delete.
     */
    public final void delete() throws RecordNotDeletedException {
    	if(!tryDelete()) {
    		throw new RecordNotDeletedException("Record not deleted, checks that is the record exists or failed to delete ?");
    	}
    }

    /**
     * Delete the record, returns true if deleted, returns false if the record not exists.
     */
    public final boolean tryDelete(){
    	_init();
    	return doDelete();
    }

    /**
     * Reload the record from underlying database.
     */
    public final boolean load(){
    	_init();
    	return doLoad();
    }

    /**
     * Same as {@link #load()}
     */
    public final boolean refresh() {
        return load();
    }

    /**
     * Returns <code>true</code> if this model is valid after validation.
     * 
     * <p>
     * Returns <code>false</code> if this model has errors after validation. 
     */
    public final boolean validate(){
    	return validate(0);
    }
    
	/**
     * Returns <code>true</code> if this model is valid after validation.
     * 
     * <p>
     * Returns <code>false</code> if this model has {@link Errors} after validation. 
     */
    public final boolean validate(int maxErrors){
        return validate(maxErrors, null);
    }

    /**
     * Validates the values in {@link #fields()} only.
     *
     * @see  {@link #validate()}
     */
    public final boolean validate(Iterable<String> fields) {
        return validate(0, fields);
    }

    /**
     * Validates the values of the given field names only.
     *
     * @see  {@link #validate()}
     */
    public final boolean validate(int maxErrors, Iterable<String> fields) {
        _init();

        errors = dao.validate(this.em,this,fields);

        if(maxErrors > 0 && errors.size() >= maxErrors){
            return false;
        }

        doValidate(errors,maxErrors);
        return errors.isEmpty();
    }
    
	protected Object doGetId(){
		String[] keyNames = em.getKeyFieldNames();
		if(keyNames.length == 1){
			return get(keyNames[0]);
		}
		
		if(keyNames.length == 0){
			return null;
		}
		
		Map<String, Object> id = new LinkedHashMap<String, Object>();
		for(int i=0;i<keyNames.length;i++){
			id.put(keyNames[i], get(keyNames[i]));
		}
		return id;
    }
	
	@SuppressWarnings("rawtypes")
	protected void doSetId(Object id){
		_init();
		String[] keyNames = em.getKeyFieldNames();
		if(keyNames.length == 1){
			doSet(keyNames[0],id);
			return;
		}
		
		if(keyNames.length == 0){
			throw new IllegalStateException("Model '" + getEntityName() + "' has no id fields");
		}
		
		if(id == null){
			for(int i=0;i<keyNames.length;i++){
				doSet(keyNames[i],null);
			}
		}else{
			if(!(id instanceof Map)){
				throw new IllegalArgumentException("The given id must be a Map object for composite id model");
			}
            Map map = (Map)id;
			for(int i=0;i<keyNames.length;i++){
				doSet(keyNames[i], map.get(keyNames[i]));
			}
		}
    }
	
	protected Object doGetReservedFieldValue(ReservedMetaFieldName name) {
		return get(em.getFieldMappingByMetaName(name).getFieldName());
	}
	
	protected void doSetReservedFieldValue(ReservedMetaFieldName name,Object value) {
		set(em.getFieldMappingByMetaName(name).getFieldName(),value);
	}

	protected void doSet(String field,Object value){
    	BeanProperty bp = beanType().tryGetProperty(field, true);
    	if(null != bp){
    		bp.setValue(this, value);
    	}else{
    		fields.put(field, value);
    	}
	}
    
    protected void doSave(){
    	Object id = id();
    	if(null == id){
    		if(!doCreate() ){
    			throw new RecordNotSavedException("Record not created");
    		}
    	}else{
    		if(!doUpdate(id)){
    			throw new RecordNotSavedException("Record not updated, use 'create()' instead of 'save()' ?");
    		}
    	}
    }
    
    protected boolean tryDoSave(){
    	Object id = id();
    	if(null == id){
    		return doCreate();
    	}else{
    		return doUpdate(id);
    	}
    }
    
    protected boolean doCreate() {
    	return dao.cmdInsert(em).setAll(createParameters()).execute() > 0;
    }
    
    protected boolean doUpdate(Object id) {
		return dao.cmdUpdate(em).setAll(createParameters()).execute() > 0;
    }
    
    protected boolean doDelete(){
    	return dao.delete(em.getEntityName(), ensureGetId()) > 0;
    }
    
    protected boolean doLoad(){
    	Object id = ensureGetId();

    	Record entity = dao.find(em, id);
    			
    	fields.clear();
    	setAll(entity);
    	
    	return true;
    }
    
    /**
     * Can be override by sub-class to perform customized validation.
     */
    protected void doValidate(Errors errors,int maxErrors){
    	
    }
    
    protected final Object ensureGetId(){
    	Object id = id();
    	if(null == id){
    		throw new IllegalStateException("Cannot perform current operation, 'id' must not be null in model '" + em.getEntityName() + "'");
    	}
    	return id;
    }
    
    protected final void _init(){
    	if(null == em){
    		ModelContext context = ModelRegistry.getModelContext(this.getClass().getName());
    		this.em  = context.getEntityMapping();
    		this.dao = context.getDao();
    	}
    }
    
    protected final BeanType beanType(){
    	if(null == beanType){
    		beanType = BeanType.of(this.getClass());
    	}
    	return beanType;
    }
    
    protected Params createParameters() {
	    return new NamedParamsBase(fields()) {
			@Override
            public Params set(String name, Object value) {
				Model.this.set(name,value);
				return this;
            }

			@Override
            public Params setAll(Map<String, ? extends Object> m) {
				Model.this.setAll(m);
				return this;
			}

			@Override
            protected void setRawValue(String name, Object value) {
	            Model.this.set(name, value);
            }
		};
    }

    //-----json--------
    @Override
    public void toJson(JsonWriter w) {
        Map<String, Object> map = new WrappedCaseInsensitiveMap<>();
        map.putAll(fields);

        BeanProperty[] props = beanType().getProperties();
        for(int i=0;i<props.length;i++){
            BeanProperty p = props[i];
            if(p.isField()) {
                if(p.isAnnotationPresent(JsonIgnore.class)) {
                    map.remove(p.getName());
                }else{
                    map.put(p.getName(), p.getValue(this));
                }
            }
        }

        w.map(map);
    }

    //----implements DyanBean ---
	@Override
    public Object getProperty(String name) {
	    return get(name);
    }

	@Override
    public void setProperty(String name, Object value) {
		set(name,value);
    }

	@Override
    public Map<String, Object> getProperties() {
	    return fields();
    }

	@Override
    public String toString() {
		_init();
	    
		ToStringBuilder s = new ToStringBuilder(this);

		for(FieldMapping fm : em.getFieldMappings()){
			s.append(fm.getFieldName(), get(fm.getFieldName()));
		}
		
	    return s.toString();
    }
}