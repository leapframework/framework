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

import leap.core.jdbc.PreparedStatementHandler;
import leap.core.validation.Errors;
import leap.core.validation.ValidationException;
import leap.db.Db;
import leap.lang.Arrays2;
import leap.lang.Strings;
import leap.lang.expression.Expression;
import leap.orm.OrmContext;
import leap.orm.dao.Dao;
import leap.orm.event.EntityEventWithWrapperImpl;
import leap.orm.event.EntityEventHandler;
import leap.orm.interceptor.EntityExecutionContext;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.sql.SqlCommand;
import leap.orm.sql.SqlFactory;
import leap.orm.validation.EntityValidator;
import leap.orm.value.EntityWrapper;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultInsertCommand extends AbstractEntityDaoCommand implements InsertCommand,EntityExecutionContext {

    protected final SqlFactory         sf;
    protected final EntityEventHandler eventHandler;

    protected EntityWrapper entity;
    protected Object        id;
	protected Object        generatedId;

	public DefaultInsertCommand(Dao dao,EntityMapping em) {
	    super(dao,em);
	    this.sf		      = dao.getOrmContext().getSqlFactory();
        this.eventHandler = context.getEntityEventHandler();
    }

    @Override
    public OrmContext getOrmContext() {
        return dao.getOrmContext();
    }

    @Override
    public EntityMapping getEntityMapping() {
	    return em;
    }

    @Override
    public Object id() {
        if(null != id) {
            return id;
        }

        if(null != generatedId) {
            return generatedId;
        }

        if(em.getKeyFieldNames().length == 1){
            return entity.get(em.getKeyFieldNames()[0]);
        }else if(em.getKeyFieldNames().length > 0){
            Map<String,Object> map = new LinkedHashMap<>();

            for(String name : em.getKeyFieldNames()) {
                map.put(name, entity.get(name));
            }

            return map;
        }

        return null;
    }

	@Override
    public boolean isReturnGeneratedId() {
	    return true;
    }

	@Override
    public void setGeneratedId(Object id) {
		this.generatedId = id;
        if(null != entity) {
            entity.set(em.getKeyFieldNames()[0], id);
        }
    }

    @Override
    public InsertCommand withId(Object id) {
        if(em.getKeyFieldNames().length == 0){
            throw new IllegalStateException("Entity '" + em.getEntityName() + "' has no id");
        }
        this.id = id;
        return this;
    }

    @Override
    public InsertCommand from(Object record) {
        entity = EntityWrapper.wrap(em, record);
        return this;
    }
	
	@Override
    public int execute() {
		prepare();

        if(eventHandler.isHandleCreateEvent(context, em)) {
            return doExecuteWithEvent();
        }else{
            return doExecuteUpdate();
        }
    }

    protected int doExecuteWithEvent() {
        int result;

        EntityEventWithWrapperImpl e = new EntityEventWithWrapperImpl(context, em, entity, id);

        //pre without transaction.
        eventHandler.preCreateEntityNoTrans(context, em, e);

        if(em.hasSecondaryTable() || eventHandler.isCreateEventTransactional(context, em)) {
            result = dao.doTransaction((status) -> {
                e.setTransactionStatus(status);

                //pre with transaction.
                eventHandler.preCreateEntityInTrans(context, em, e);

                int affected = doExecuteUpdate();

                //post with transaction.
                eventHandler.postCreateEntityInTrans(context, em, e);

                e.setTransactionStatus(null);

                return affected;
            });

        }else{
            result = doExecuteUpdate();
        }

        //post without transaction.
        eventHandler.postCreateEntityNoTrans(context, em, e);

        return result;
    }

    protected int doExecuteUpdate() {
        String[]   fields           = entity.getFieldNames().toArray(Arrays2.EMPTY_STRING_ARRAY);
        SqlCommand primaryCommand   = sf.createInsertCommand(context, em, fields);
        SqlCommand secondaryCommand = em.hasSecondaryTable() ? sf.createInsertCommand(context, em, fields, true) : null;

        //Resolve statement handler.
        PreparedStatementHandler<Db> handler = null;
        if(null != em.getInsertInterceptor()){
            handler = em.getInsertInterceptor().getPreparedStatementHandler(this);
        }

        //Creates map for saving.
        Map<String,Object> map = entity.toMap();

        //Prepared id and serialization
        prepareIdAndSerialization(id, map);

        //Executes
        int result = primaryCommand.executeUpdate(this, map, handler);

        if(null != secondaryCommand) {
            secondaryCommand.executeUpdate(this, withGeneratedId(map));
        }

        return result;
    }

    protected Map<String,Object> withGeneratedId(Map<String,Object> map) {
        if(null != generatedId) {
            map.put(em.getKeyColumnNames()[0], generatedId);
        }
        return map;
    }
	
	protected void prepare(){
		for(FieldMapping fm : em.getFieldMappings()){
            Object value = entity.get(fm.getFieldName());

            if(null == value) {
                if (!Strings.isEmpty(fm.getSequenceName())) {
                    //the insertion sql must use $fieldName$
                    value = db.getDialect().getNextSequenceValueSqlString(fm.getSequenceName());
                    entity.set(fm.getFieldName(), value);
                } else {
                    Expression expression = fm.getInsertValue();
                    if (null != expression) {
                        value = expression.getValue(entity);

                        if (fm.isPrimaryKey()) {
                            generatedId = value;
                        }

                        setGeneratedValue(fm, value);
                    } else {
                        expression = fm.getDefaultValue();

                        if (null != expression) {
                            value = expression.getValue(entity);

                            setGeneratedValue(fm, value);
                        }
                    }
                }
            }
		}

        if(em.isAutoValidate()) {
            EntityValidator validator = context.getEntityValidator();
            Errors errors = validator.validate(entity);
            if(!errors.isEmpty()) {
                throw new ValidationException(errors);
            }
        }
	}
	
	protected void setGeneratedValue(FieldMapping fm,Object value){
		entity.set(fm.getFieldName(), value);
	}
}