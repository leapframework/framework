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
import leap.db.Db;
import leap.lang.Arrays2;
import leap.lang.Strings;
import leap.lang.expression.Expression;
import leap.lang.params.Params;
import leap.orm.OrmContext;
import leap.orm.dao.Dao;
import leap.orm.event.CreateEntityEventHandler;
import leap.orm.interceptor.EntityExecutionContext;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.sql.SqlCommand;
import leap.orm.sql.SqlFactory;
import leap.orm.value.EntityWrapper;

import java.util.Map;

public class DefaultInsertCommand extends AbstractEntityDaoCommand implements InsertCommand,EntityExecutionContext {

    protected final SqlFactory               sf;
    protected final CreateEntityEventHandler eventHandler;

    protected SqlCommand    command;
    protected EntityWrapper entity;
    protected Object        id;
	protected Object        generatedId;

	public DefaultInsertCommand(Dao dao,EntityMapping em) {
		this(dao, em, null);
	}
	
	public DefaultInsertCommand(Dao dao,EntityMapping em, SqlCommand command) {
	    super(dao,em);
	    this.sf		      = dao.getOrmContext().getSqlFactory();
	    this.command      = command;
        this.eventHandler = em.getCreateEntityEventHandler();
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
        return null == id ? generatedId : id;
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
            throw new IllegalStateException("Model '" + em.getEntityName() + "' has no id fields");
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

        if(null != eventHandler) {
            return doExecuteWithEvent();
        }else{
            return doExecuteUpdate();
        }
    }

    protected int doExecuteWithEvent() {
        int result;

        //pre without transaction.
        eventHandler.preCreateEntity(entity);

        if(eventHandler.isTransactional()) {
            result = dao.doTransaction((status) -> {
                //pre with transaction.
                eventHandler.preCreateEntityWithTransaction(entity, status);

                int affected = doExecuteUpdate();

                //post with transaction.
                eventHandler.postCreateEntityWithTransaction(entity, status);

                return affected;
            });

        }else{
            result = doExecuteUpdate();
        }

        //post without transaction.
        eventHandler.postCreateEntity(entity);

        return result;
    }

    protected int doExecuteUpdate() {

        //Create command.
        if(null == command) {
            String[] fields = entity.getFieldNames().toArray(Arrays2.EMPTY_STRING_ARRAY);
            command = sf.createInsertCommand(context, em, fields);
        }

        //Resolve statement handler.
        PreparedStatementHandler<Db> preparedStatementHandler = null;
        if(null != em.getInsertInterceptor()){
            preparedStatementHandler = em.getInsertInterceptor().getPreparedStatementHandler(this);
        }

        //Creates map for saving.
        Map<String,Object> map = context.getParameterStrategy().toMap(entity.raw());

        //Prepared id
        if(null != id) {
            String[] keyNames = em.getKeyFieldNames();
            if(keyNames.length == 1){
                map.put(keyNames[0],id);
            }else{
                Params idParams = context.getParameterStrategy().createIdParameters(context, em, id);
                for(int i=0;i<keyNames.length;i++){
                    map.put(keyNames[i], idParams.get(keyNames[i]));
                }
            }
        }

        //Serialize field(s).
        for(FieldMapping fm : em.getFieldMappings()){
            if(null != fm.getSerializer()) {
                Object value = entity.get(fm.getFieldName());
                Object encoded = fm.getSerializer().trySerialize(fm, value);
                if(encoded != value) {
                    map.put(fm.getFieldName(), encoded);
                }
            }
        }

        if(null != preparedStatementHandler){
            return command.executeUpdate(this, map, preparedStatementHandler);
        }else{
            return command.executeUpdate(this, map);
        }
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
	}
	
	protected void setGeneratedValue(FieldMapping fm,Object value){
		entity.set(fm.getFieldName(), value);
	}
}