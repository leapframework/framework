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
package leap.orm.mapping;

import leap.core.metamodel.ReservedMetaFieldName;
import leap.db.model.DbColumn;
import leap.db.model.DbTable;
import leap.lang.Args;
import leap.lang.Assert;
import leap.lang.New;
import leap.lang.Strings;
import leap.lang.annotation.Nullable;
import leap.lang.beans.BeanType;
import leap.lang.exception.ObjectNotFoundException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.orm.domain.EntityDomain;
import leap.orm.interceptor.EntityExecutionInterceptor;
import leap.orm.model.Model;
import leap.orm.validation.EntityValidator;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EntityMapping {
	private static final Log log = LogFactory.get(EntityMapping.class);
	
	protected final String						 entityName;
	protected final Class<?>      		         entityClass;
	protected final BeanType                     beanType;
	protected final DbTable		  		         table;
	protected final FieldMapping[]               fieldMappings;
    protected final FieldMapping[]               whereFieldMappings;
	protected final FieldMapping[]               keyFieldMappings;
	protected final String[]                     keyFieldNames;
	protected final String[]					 keyColumnNames;
	protected final boolean				         autoIncrementKey;
	protected final DbColumn			         autoIncrementKeyColumn;
	protected final FieldMapping                 autoIncrementKeyField;
	protected final FieldMapping			     optimisticLockField;
	protected final EntityExecutionInterceptor   insertInterceptor;
	protected final EntityExecutionInterceptor   updateInterceptor;
	protected final EntityExecutionInterceptor   deleteInterceptor;
	protected final EntityExecutionInterceptor   findInterceptor;
	protected final EntityDomain			     domain;
	protected final Class<? extends Model>       modelClass;
	protected final EntityValidator[]            validators;
	protected final RelationMapping[]			 relationMappings;
	
	private final Map<String,FieldMapping> columnNameToFields;
	private final Map<String,FieldMapping> fieldNameToFields;
	private final Map<String,FieldMapping> metaNameToFields;
	
	public EntityMapping(String entityName,
						Class<?> entityClass,DbTable table,List<FieldMapping> fieldMappings,
						EntityExecutionInterceptor insertInterceptor,EntityExecutionInterceptor updateInterceptor,
						EntityExecutionInterceptor deleteInterceptor,EntityExecutionInterceptor findIncerceptor,
						EntityDomain domain, Class<? extends Model> modelClass,
						List<EntityValidator> validators,
						List<RelationMapping> relationMappings) {
		
		Args.notEmpty(entityName,"entity name");
		Args.notNull(table,"table");
		Args.notEmpty(fieldMappings,"field mappings");
		
		this.entityName		   = entityName;
	    this.entityClass       = entityClass;
	    this.beanType          = null == entityClass ? null : BeanType.of(entityClass);
	    this.table             = table;
	    this.insertInterceptor = insertInterceptor;
	    this.updateInterceptor = updateInterceptor;
	    this.deleteInterceptor = deleteInterceptor;
	    this.findInterceptor   = findIncerceptor;
	    this.domain			   = domain;
	    this.modelClass        = modelClass;
	    this.validators        = null == validators ? new EntityValidator[]{} : validators.toArray(new EntityValidator[validators.size()]);
	    this.relationMappings  = null == relationMappings ? new RelationMapping[]{} : relationMappings.toArray(new RelationMapping[relationMappings.size()]);
	    
	    this.fieldMappings          = fieldMappings.toArray(new FieldMapping[fieldMappings.size()]);
	    this.columnNameToFields     = createColumnNameToFieldsMap();
	    this.fieldNameToFields      = createFieldNameToFieldsMap();
	    this.metaNameToFields		= createMetaNameToFieldsMap();
        this.whereFieldMappings     = evalWhereFieldMappings();
	    this.keyFieldMappings       = evalKeyFieldMappings();
	    this.keyFieldNames          = evalKeyFieldNames();
	    this.keyColumnNames			= evalKeyColumnNames();
	    this.autoIncrementKey       = table.getPrimaryKeyColumnNames().length == 1 && table.getPrimaryKeyColumns()[0].isAutoIncrement();
	    this.autoIncrementKeyColumn = autoIncrementKey ? table.getPrimaryKeyColumns()[0] : null;
	    this.autoIncrementKeyField  = autoIncrementKey ? keyFieldMappings[0] : null;
	    this.optimisticLockField    = findOptimisticLockField();
    }
	
	public String getEntityName(){
		return entityName;
	}
	
	public String getTableName(){
		return table.getName();
	}

	@Nullable
	public Class<?> getEntityClass() {
		return entityClass;
	}
	
	public DbTable getTable() {
		return table;
	}
	
	@Nullable
	public BeanType getBeanType() {
		return beanType;
	}
	
	@Nullable
	public EntityDomain getDomain() {
		return domain;
	}
	
	public Class<? extends Model> getModelClass() {
		return modelClass;
	}

	public FieldMapping[] getFieldMappings() {
		return fieldMappings;
	}
	
	public FieldMapping[] getKeyFieldMappings() {
		return keyFieldMappings;
	}

	public String[] getKeyFieldNames() {
		return keyFieldNames;
	}
	
	public String[] getKeyColumnNames() {
		return keyColumnNames;
	}
	
	public EntityValidator[] getValidators() {
		return validators;
	}
	
	public RelationMapping[] getRelationMappings() {
		return relationMappings;
	}

	public FieldMapping getFieldMapping(String fieldName) throws ObjectNotFoundException {
		FieldMapping fm = tryGetFieldMapping(fieldName);
		
		if(null == fm){
			throw new ObjectNotFoundException("Field mapping '" + fieldName + "' not exists in entity '" + getEntityName() + "'");
		}
		
		return fm;
	}
	
	public FieldMapping tryGetFieldMapping(String fieldName) {
		Args.notNull(fieldName,"field name");
		return fieldNameToFields.get(fieldName.toLowerCase());
	}
	
	/**
	 * Returns the {@link FieldMapping} object mapping to the given column (ignore case).
	 * 
	 * @throws ObjectNotFoundException if no {@link FieldMapping} mapping to the given column.
	 */
	public FieldMapping getFieldMappingByColumn(String columnName) throws ObjectNotFoundException {
		FieldMapping fm = tryGetFieldMappingByColumn(columnName);
		
		if(null == fm){
			throw new ObjectNotFoundException("No field mapped to the column '" + columnName + "' in entity '" + getEntityName() + "'");
		}
		
		return fm;
	}
	
	public FieldMapping tryGetFieldMappingByColumn(String columnName) {
		Args.notNull(columnName,"column name");
		return columnNameToFields.get(columnName.toLowerCase());
	}
	
	public FieldMapping getFieldMappingByMetaName(ReservedMetaFieldName metaFieldName) throws ObjectNotFoundException {
		Args.notNull(metaFieldName,"metaFieldName");
		return getFieldMappingByMetaName(metaFieldName.getFieldName());
	}
	
	public FieldMapping getFieldMappingByMetaName(String metaFieldName) throws ObjectNotFoundException {
		FieldMapping fm = tryGetFieldMappingByMetaName(metaFieldName);
		if(null == fm){
			throw new ObjectNotFoundException("No meta field '" + metaFieldName + "' in entity '" + getEntityName() + "'");
		}
		return fm;
	}
	
	public FieldMapping tryGetFieldMappingByMetaName(ReservedMetaFieldName metaFieldName) {
		if(null == metaFieldName){
			return null;
		}
		return metaNameToFields.get(metaFieldName.getFieldName().toLowerCase());
	}
	
	public FieldMapping tryGetFieldMappingByMetaName(String metaFieldName) {
		if(null == metaFieldName){
			return null;
		}
		return metaNameToFields.get(metaFieldName.toLowerCase());
	}
	
	public boolean isAutoIncrementKey() {
		return autoIncrementKey;
	}
	
	public boolean isCompositeKey() {
		return keyColumnNames.length > 1;
	}
	
	public DbColumn getAutoIncrementKeyColumn() {
		return autoIncrementKeyColumn;
	}
	
	public FieldMapping getAutoIncrementKeyField() {
		return autoIncrementKeyField;
	}
	
	public boolean hasOptimisticLock(){
		return null != optimisticLockField;
	}

    public boolean hasWhereFields() {
        return whereFieldMappings.length > 0;
    }

    public FieldMapping[] getWhereFieldMappings() {
        return whereFieldMappings;
    }

    public FieldMapping getOptimisticLockField() {
		return optimisticLockField;
	}

	public EntityExecutionInterceptor getInsertInterceptor() {
		return insertInterceptor;
	}

	public EntityExecutionInterceptor getUpdateInterceptor() {
		return updateInterceptor;
	}

	public EntityExecutionInterceptor getDeleteInterceptor() {
		return deleteInterceptor;
	}

	public EntityExecutionInterceptor getFindInterceptor() {
		return findInterceptor;
	}

	private FieldMapping[] evalKeyFieldMappings(){
		List<FieldMapping> list = New.arrayList();
		
		for(FieldMapping fm : this.fieldMappings){
			if(fm.isPrimaryKey()){
				list.add(fm);
			}
		}
		
		return list.toArray(new FieldMapping[list.size()]);
	}

    private FieldMapping[] evalWhereFieldMappings(){
        List<FieldMapping> list = New.arrayList();

        for(FieldMapping fm : this.fieldMappings){
            if(fm.isWhere()){
                Assert.isTrue(null != fm.getWhereValue(),
                             "There where value expression must not be null of where field '" + fm.getFieldName() + "'");
                list.add(fm);
            }
        }

        return list.toArray(new FieldMapping[list.size()]);
    }
	
	private String[] evalKeyFieldNames(){
		String[] names = new String[keyFieldMappings.length];
		for(int i=0;i<names.length;i++){
			names[i] = keyFieldMappings[i].getFieldName();
		}
		return names;
	}
	
	private String[] evalKeyColumnNames(){
		String[] names = new String[keyFieldMappings.length];
		for(int i=0;i<names.length;i++){
			names[i] = keyFieldMappings[i].getColumnName();
		}
		return names;
	}

	private Map<String,FieldMapping> createColumnNameToFieldsMap(){
		Map<String,FieldMapping> map = New.linkedHashMap();
		for(FieldMapping fm : fieldMappings){
			map.put(fm.getColumn().getName().toLowerCase(),fm);
		}
		return Collections.unmodifiableMap(map);
	}
	
	private Map<String,FieldMapping> createFieldNameToFieldsMap(){
		Map<String,FieldMapping> map = New.linkedHashMap();
		for(FieldMapping fm : fieldMappings){
			map.put(fm.getFieldName().toLowerCase(),fm);
		}
		return Collections.unmodifiableMap(map);
	}
	
	private Map<String,FieldMapping> createMetaNameToFieldsMap() {
		Map<String,FieldMapping> map = New.hashMap();
		
		for(FieldMapping fm : fieldMappings) {
			String metaName = fm.getMetaFieldName();
			if(!Strings.isEmpty(metaName)) {
				String key = metaName.toLowerCase();
				
				FieldMapping exists = map.get(key);
				if(null != exists) {
					log.warn("Found duplicated meta field name '" + metaName + 
							 "' in entity '" + getEntityName() + 
							 "', fields [" + fm.getFieldName() + "," + exists.getFieldName() + "]");	
				}
				
				map.put(key, fm);
			}
		}
		
		return Collections.unmodifiableMap(map);
	}
	
	private FieldMapping findOptimisticLockField(){
		for(FieldMapping fm : fieldMappings){
			if(fm.isOptimisticLock()){
				return fm;
			}
		}
		return null;
	}

	@Override
    public String toString() {
	    return "Entity[name=" + getEntityName() + ",table=" + getTableName() + ",class=" + (entityClass == null ? "null" : entityClass.getName()) + "]";
    }
}