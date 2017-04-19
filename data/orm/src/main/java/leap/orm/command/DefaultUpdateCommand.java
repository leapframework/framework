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
package leap.orm.command;

import java.util.Map;

import leap.core.exception.InvalidOptimisticLockException;
import leap.core.exception.OptimisticLockException;
import leap.lang.Args;
import leap.lang.convert.Converts;
import leap.lang.expression.Expression;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.params.Params;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.mapping.Mappings;
import leap.orm.sql.SqlCommand;
import leap.orm.sql.SqlFactory;
import leap.orm.value.Entity;

public class DefaultUpdateCommand extends AbstractEntityDaoCommand implements UpdateCommand {

    private static final Log log = LogFactory.get(DefaultUpdateCommand.class);
	
    protected final SqlFactory sf;
    protected final Entity     entity;
	
	protected SqlCommand   command;
	protected Params   	   parameters;
	protected Object	   oldOptimisticLockValue;
	protected Object	   newOptimisticLockValue;

	public DefaultUpdateCommand( Dao dao, EntityMapping em) {
		this(dao,em,null);
	}
	
	public DefaultUpdateCommand(Dao dao, EntityMapping em, SqlCommand command) {
	    super(dao,em);
	    this.sf = dao.getOrmContext().getSqlFactory();
	    this.entity = new Entity(em.getEntityName());
	    this.command = command;
    }
	
    @Override
    @SuppressWarnings("rawtypes")
    public UpdateCommand id(Object id) {
    	Args.notNull(id,"id");
    	
		String[] keyNames = em.getKeyFieldNames();
		if(keyNames.length == 1){
			entity.put(keyNames[0],id);
			return this;
		}
		
		if(keyNames.length == 0){
			throw new IllegalStateException("Model '" + em.getEntityName() + "' has no id fields");
		}
		
		if(id == null){
			for(int i=0;i<keyNames.length;i++){
				entity.put(keyNames[i],null);
			}
		}else{
			if(!(id instanceof Map)){
				throw new IllegalArgumentException("The given id must be a Map object for composite id model");
			}
            Map map = (Map)id;
			for(int i=0;i<keyNames.length;i++){
				entity.put(keyNames[i], map.get(keyNames[i]));
			}
		}
    	
    	return this;
    }

	@Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public UpdateCommand setAll(Object bean) {
		Args.notNull(bean,"bean");
		
		if(bean instanceof Map){
			return setAll((Map)bean);
		}else{
			this.parameters = context.getParameterStrategy().createParams(bean);
			this.entity.putAll(parameters.map());
		    return this;
		}
    }
	
	@Override
    public UpdateCommand setAll(Map<String, Object> fields) {
		Args.notNull(fields,"fields");
		this.parameters = context.getParameterStrategy().createParams(fields);
		entity.putAll(fields);
		return this;
    }

	@Override
    public UpdateCommand set(String name, Object value) {
		Args.notEmpty(name,"name");
		this.entity.put(name, value);
	    return this;
    }
	
	@Override
    public int execute() {
		prepare();
		
		SqlCommand command = this.command;
		
		if(null == command){
            command = sf.createUpdateCommand(context, em, entity.keySet().toArray(new String[entity.size()]));
            log.debug("Create update sql : {}", command.getSql());
		}
		
	    int result = command.executeUpdate(this, entity);
	    
	    if(em.hasOptimisticLock()){
		    if(result < 1){
		    	String id  = Mappings.getIdToString(em, entity);
		    	throw new OptimisticLockException("Failed to update entity '" + em.getEntityName() + 
		    									  "', id=[" + id + "], verion=[" + oldOptimisticLockValue + 
		    									  "], may be an optimistic locking conflict occured");
		    }else{ 
		    	setGeneratedValue(em.getOptimisticLockField(), newOptimisticLockValue);
		    }
	    }
	    
	    return result;
    }
	
	protected void prepare(){
		for(FieldMapping fm : em.getFieldMappings()){

			if(fm.isOptimisticLock()){
				prepareOptimisticLock(fm);
			}else{
                Object value = entity.get(fm.getFieldName());

                if(null == value) {
                    Expression expression = fm.getUpdateValue();
                    if (null != expression) {
                        value = expression.getValue(this, entity);
                        setGeneratedValue(fm, value);
                    }
                }

                if(null != value && null != fm.getSerializer()) {
                    Object encoded = fm.getSerializer().trySerialize(fm, value);
                    if(encoded != value) {
                        entity.set(fm.getFieldName(), encoded);
                    }
                }
			}
		}
	}
	
	protected void prepareOptimisticLock(FieldMapping fm){
		oldOptimisticLockValue = entity.get(fm.getFieldName());
		
		if(null == oldOptimisticLockValue){
			throw new InvalidOptimisticLockException("Value in optimistic locking field '" + fm.getFieldName() + "' must not be null");
		}
		
		Long oldValue;
		try {
            oldValue = Converts.toLong(oldOptimisticLockValue);
        } catch (Exception e) {
        	throw new InvalidOptimisticLockException("Optimistic locking value '" + oldOptimisticLockValue + 
        											 "' in field '" + fm.getFieldName() + "' is invalid,must be integer value");
        } 
		
		//TODO : check max value 
		newOptimisticLockValue = oldValue + 1;
		entity.set(fm.getNewOptimisticLockFieldName(),newOptimisticLockValue);
	}
	
	protected void setGeneratedValue(FieldMapping fm,Object value){
		entity.put(fm.getFieldName(), value);
		if(null != parameters){
			parameters.set(fm.getFieldName(), value);
		}
	}
}
