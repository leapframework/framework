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

import java.util.HashMap;
import java.util.Map;

import leap.core.exception.InvalidOptimisticLockException;
import leap.core.exception.OptimisticLockException;
import leap.core.jdbc.PreparedStatementHandler;
import leap.db.Db;
import leap.lang.Args;
import leap.lang.Arrays2;
import leap.lang.convert.Converts;
import leap.lang.expression.Expression;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.params.Params;
import leap.orm.dao.Dao;
import leap.orm.event.EntityEventWithWrapperImpl;
import leap.orm.event.EntityEventHandler;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.mapping.Mappings;
import leap.orm.sql.SqlCommand;
import leap.orm.sql.SqlFactory;
import leap.orm.value.EntityWrapper;

public class DefaultUpdateCommand extends AbstractEntityDaoCommand implements UpdateCommand {

    protected final SqlFactory         sf;
    protected final EntityEventHandler eventHandler;

	protected SqlCommand    command;
    protected EntityWrapper entity;
    protected Object        id;
	protected Object        oldOptimisticLockValue;
	protected Object        newOptimisticLockValue;

	public DefaultUpdateCommand( Dao dao, EntityMapping em) {
		this(dao,em,null);
	}
	
	public DefaultUpdateCommand(Dao dao, EntityMapping em, SqlCommand command) {
	    super(dao,em);
	    this.sf           = dao.getOrmContext().getSqlFactory();
        this.eventHandler = context.getEntityEventHandler();
	    this.command      = command;
    }
	
    @Override
    public UpdateCommand withId(Object id) {
        if(em.getKeyFieldNames().length == 0){
            throw new IllegalStateException("Entity '" + em.getEntityName() + "' has no id");
        }

        this.id = id;
    	return this;
    }

	@Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public UpdateCommand from(Object record) {
		Args.notNull(record,"record");
        entity = EntityWrapper.wrap(em, record);
        return this;
    }
	
	@Override
    public UpdateCommand set(String name, Object value) {
		Args.notEmpty(name,"name");
        if(null == entity) {
            entity = EntityWrapper.wrap(em, new HashMap<>());
        }
		entity.set(name, value);
	    return this;
    }
	
	@Override
    public int execute() {
		prepare();

        if(eventHandler.isHandleUpdateEvent(context, em)) {
            return doExecuteWithEvent();
        }else{
            return doExecuteUpdate();
        }
    }

    protected int doExecuteWithEvent() {
        int result;

        EntityEventWithWrapperImpl e = new EntityEventWithWrapperImpl(context, em, entity);

        //pre without transaction.
        eventHandler.preUpdateEntityNoTrans(context, em, e);

        if(eventHandler.isUpdateEventTransactional(context, em)) {
            result = dao.doTransaction((status) -> {
                e.setTransactionStatus(status);

                //pre with transaction.
                eventHandler.preUpdateEntityInTrans(context, em, e);

                int affected = doExecuteUpdate();

                //post with transaction.
                eventHandler.postUpdateEntityInTrans(context, em, e);

                e.setTransactionStatus(null);

                return affected;
            });

        }else{
            result = doExecuteUpdate();
        }

        //post without transaction.
        eventHandler.postUpdateEntityNoTrans(context, em, e);

        return result;
    }

    protected int doExecuteUpdate() {

        //Create command.
        if(null == command) {
            String[] fields = entity.getFieldNames().toArray(Arrays2.EMPTY_STRING_ARRAY);
            command = sf.createUpdateCommand(context, em, fields);
        }

        //Creates map for saving.
        Map<String,Object> fields = entity.toMap();

        //Prepared id and serialization.
        prepareIdAndSerialization(id, fields);

        int result = command.executeUpdate(this, fields);
        if(em.hasOptimisticLock()){
            if(result < 1){
                String id  = Mappings.getIdToString(em, fields);
                throw new OptimisticLockException("Failed to update entity '" + em.getEntityName() +
                        "', id=[" + id + "], version=[" + oldOptimisticLockValue +
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
                        value = expression.getValue(entity);
                        setGeneratedValue(fm, value);
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
        											 "' in field '" + fm.getFieldName() + "' is invalid,must be long value");
        } 
		
		//TODO : check max value 
		newOptimisticLockValue = oldValue + 1;
		entity.set(fm.getNewOptimisticLockFieldName(),newOptimisticLockValue);
	}
	
	protected void setGeneratedValue(FieldMapping fm,Object value){
        entity.set(fm.getFieldName(), value);
	}
}
