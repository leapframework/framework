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

import leap.core.el.EL;
import leap.core.exception.InvalidOptimisticLockException;
import leap.core.exception.OptimisticLockException;
import leap.core.validation.Errors;
import leap.core.validation.ValidationException;
import leap.db.support.JsonColumnSupport;
import leap.lang.Args;
import leap.lang.Arrays2;
import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.lang.expression.Expression;
import leap.lang.json.JSON;
import leap.orm.dao.Dao;
import leap.orm.event.EntityEvent;
import leap.orm.event.EntityEventHandler;
import leap.orm.event.EntityEventWithWrapperImpl;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.mapping.Mappings;
import leap.orm.sql.SqlCommand;
import leap.orm.sql.SqlFactory;
import leap.orm.validation.EntityValidator;
import leap.orm.value.EntityWrapper;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class DefaultUpdateCommand extends AbstractEntityDaoCommand implements UpdateCommand {

    protected final SqlFactory         sf;
    protected final EntityEventHandler eventHandler;
    protected final UpdateHandler      handler;

    protected EntityWrapper entity;
    protected Object        id;
    protected Object        oldOptimisticLockValue;
    protected Object        newOptimisticLockValue;

    protected Boolean selective;

    public DefaultUpdateCommand(Dao dao, EntityMapping em) {
        super(dao, em);
        this.sf = dao.getOrmContext().getSqlFactory();
        this.eventHandler = context.getEntityEventHandler();
        this.handler = null == em.getUpdateHandler() ? UpdateHandler.NOP : em.getUpdateHandler();
    }

    @Override
    public UpdateCommand withId(Object id) {
        if (em.getKeyFieldNames().length == 0) {
            throw new IllegalStateException("Entity '" + em.getEntityName() + "' has no id");
        }

        this.id = id;
        return this;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public UpdateCommand from(Object record) {
        Args.notNull(record, "record");
        entity = EntityWrapper.wrap(context, em, record);
        return this;
    }

    @Override
    public UpdateCommand set(String name, Object value) {
        Args.notEmpty(name, "name");
        if (null == entity) {
            entity = EntityWrapper.wrap(context, em, new HashMap<>());
        }
        entity.set(name, value);
        return this;
    }

    @Override
    public UpdateCommand setAll(Map<String, Object> fields) {
        if (null != fields) {
            fields.forEach(this::set);
        }
        return this;
    }

    @Override
    public UpdateCommand selective() {
        selective = true;
        return this;
    }

    @Override
    public int execute() {
        prepare();

        if (eventHandler.isHandleUpdateEvent(context, em)) {
            return doExecuteWithEvent();
        } else {
            return doExecuteUpdate();
        }
    }

    protected int doExecuteWithEvent() {
        int result;

        EntityEventWithWrapperImpl e = new EntityEventWithWrapperImpl(context, entity, id, EntityEvent.Type.UPDATE);

        //pre without transaction.
        eventHandler.preUpdateEntityNoTrans(context, em, e);

        if (em.hasSecondaryTable() || eventHandler.isUpdateEventTransactional(context, em)) {
            result = dao.doTransaction((status) -> {
                e.setTransactionStatus(status);

                //pre with transaction.
                eventHandler.preUpdateEntityInTrans(context, em, e);

                int affected = doExecuteUpdate();

                //post with transaction.
                e.setAffected(affected);
                eventHandler.postUpdateEntityInTrans(context, em, e);

                e.setTransactionStatus(null);

                return affected;
            });

        } else {
            result = doExecuteUpdate();
            e.setAffected(result);
        }

        //post without transaction.
        eventHandler.postUpdateEntityNoTrans(context, em, e);

        return result;
    }

    protected int doExecuteUpdate() {
        //Creates map for saving.
        handler.preProcessUpdateRecord(this, entity);
        Map<String, Object> map = resolveFieldsMap(entity);
        //Prepared id and serialization.
        prepareIdAndSerialization(id, map);
        handler.postProcessUpdateRecord(this, map);

        Integer result = 0;

        result = handler.handleUpdate(this, map, () -> {
            String[]   fields         = map.keySet().toArray(Arrays2.EMPTY_STRING_ARRAY);
            SqlCommand primaryCommand = sf.createUpdateCommand(context, em, fields);
            if (null != primaryCommand) {
                final Map<String, Object> updateEmbedded = extractEmbeddedFields(map);
                if (null != updateEmbedded && updateEmbedded.size() > 0) {
                    final JsonColumnSupport jcs = db.getDialect().getJsonColumnSupport();
                    if (null != jcs && jcs.isUpdateByKeys()) {
                        map.putAll(updateEmbedded);
                        return primaryCommand.executeUpdate(this, map);
                    } else if (null != jcs) {
                        map.put(em.getEmbeddingColumnName(), JSON.stringify(updateEmbedded));
                        return primaryCommand.executeUpdate(this, map);
                    } else {
                        return dao.doTransaction((s) -> {
                            String dbEmbedded =
                                    dao.createCriteriaQuery(em).select(em.getEmbeddingColumnName()).whereById(id).forUpdate()
                                            .scalarOrNull().getString();
                            Map<String, Object> mergedEmbedded = mergeEmbeddedFields(updateEmbedded, dbEmbedded);
                            map.put(em.getEmbeddingColumnName(), JSON.stringify(mergedEmbedded));
                            return primaryCommand.executeUpdate(this, map);
                        });
                    }
                } else {
                    return primaryCommand.executeUpdate(this, map);
                }
            } else {
                return 0;
            }
        });

        if (em.hasSecondaryTable()) {
            String[]   fields           = map.keySet().toArray(Arrays2.EMPTY_STRING_ARRAY);
            SqlCommand secondaryCommand = sf.createUpdateCommand(context, em, fields, true);
            if (null != secondaryCommand) {
                if (null == result) {
                    result = secondaryCommand.executeUpdate(this, map);
                } else if (result > 0) {
                    secondaryCommand.executeUpdate(this, map);
                }
            }
        }

        if (null != result && em.hasOptimisticLock()) {
            if (result < 1) {
                String id = Mappings.getIdToString(em, map);
                throw new OptimisticLockException("Failed to update entity '" + em.getEntityName() +
                        "', id=[" + id + "], version=[" + oldOptimisticLockValue +
                        "], may be an optimistic locking conflict occurred");
            } else {
                setGeneratedValue(em.getOptimisticLockField(), newOptimisticLockValue);
            }
        }

        return result;
    }

    protected Map<String, Object> resolveFieldsMap(EntityWrapper entity) {
        Map<String, Object> map = entity.toMap();
        if (null != selective && selective) {
            map.entrySet().removeIf(entry -> null == entry.getValue());
        }
        return map;
    }

    protected void prepare() {
        FieldMapping[] fms = em.getFieldMappings();

        Map<String, Object> exprVars = null;
        for (FieldMapping fm : fms) {
            if (fm.isOptimisticLock()) {
                prepareOptimisticLock(fm);
            } else {
                Object value = entity.get(fm.getFieldName());
                if (null == value && fm.isUpdate()) {
                    Expression ifExpr = fm.getUpdateIf();

                    if (null != ifExpr) {
                        if (null == exprVars) {
                            exprVars = prepareExprVars(fms);
                        }
                        if (!EL.test(ifExpr, null, exprVars)) {
                            continue;
                        }
                    }
                    Expression valueExpr = fm.getUpdateValue();
                    if (null != valueExpr) {
                        value = valueExpr.getValue(entity);
                        setGeneratedValue(fm, value);
                    }
                } else if (value instanceof Expression) {
                    value = ((Expression) value).getValue(entity);
                    setGeneratedValue(fm, value);
                }
            }
        }

        if (em.isAutoValidate()) {
            EntityValidator validator = context.getEntityValidator();
            Errors          errors    = validator.validate(entity, entity.getFieldNames());
            if (!errors.isEmpty()) {
                throw new ValidationException(errors);
            }
        }
    }

    private Map<String, Object> prepareExprVars(FieldMapping[] fms) {
        Map<String, Object> vars = new HashMap<>(entity.toMap());
        vars.put("$record", getOriginalRecord());
        vars.put("$fms", fms);
        return vars;
    }

    protected Object getOriginalRecord() {
        Object record = attributes.get(ORIGINAL_RECORD);
        if (null == record) {
            return dao.findOrNull(em, id);
        }
        return record;
    }

    protected void prepareOptimisticLock(FieldMapping fm) {
        oldOptimisticLockValue = entity.get(fm.getFieldName());

        if (null == oldOptimisticLockValue) {
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
        entity.set(fm.getNewOptimisticLockFieldName(), newOptimisticLockValue);
    }

    protected void setGeneratedValue(FieldMapping fm, Object value) {
        entity.set(fm.getFieldName(), value);
    }

    protected Map<String, Object> extractEmbeddedFields(Map<String, Object> map) {
        if (null == em.getEmbeddingColumn() || !em.hasEmbeddedFieldMappings()) {
            return null;
        }

        Map<String, Object> embedded = new LinkedHashMap<>();
        map.forEach((n, v) -> {
            FieldMapping fm = em.tryGetFieldMapping(n);
            if (null != fm && fm.isEmbedded()) {
                embedded.put(n, v);
            }
        });
        embedded.keySet().forEach(map::remove);
        return embedded;
    }

    protected Map<String, Object> mergeEmbeddedFields(Map<String, Object> toUpdate, String dbValue) {
        if (Strings.isEmpty(dbValue)) {
            return toUpdate;
        }

        Map<String, Object> dbFields = JSON.decodeMap(dbValue);
        dbFields.putAll(toUpdate);
        return dbFields;
    }
}
