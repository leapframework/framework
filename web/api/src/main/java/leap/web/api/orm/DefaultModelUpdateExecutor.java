/*
 *
 *  * Copyright 2016 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package leap.web.api.orm;

import leap.core.validation.Errors;
import leap.core.validation.ValidationException;
import leap.lang.Enumerable;
import leap.lang.Enumerables;
import leap.lang.New;
import leap.orm.command.UpdateCommand;
import leap.orm.event.EntityListeners;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.mapping.RelationMapping;
import leap.orm.mapping.RelationProperty;
import leap.web.api.meta.model.MApiProperty;
import leap.web.api.mvc.params.Partial;
import leap.web.api.remote.RestResource;
import leap.web.exception.BadRequestException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultModelUpdateExecutor extends ModelExecutorBase implements ModelUpdateExecutor {

    protected final ModelUpdateExtension ex;

    private UpdateHandler   handler;
    private EntityListeners listeners;

    public DefaultModelUpdateExecutor(ModelExecutorContext context, ModelUpdateExtension ex) {
        super(context);
        this.ex = null == ex ? ModelUpdateExtension.EMPTY : ex;
    }

    @Override
    public ModelUpdateExecutor withHandler(UpdateHandler handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public ModelUpdateExecutor withListeners(EntityListeners listeners) {
        this.listeners = listeners;
        return this;
    }

    @Override
    public UpdateOneResult replaceUpdateOne(Object id, Map<String, Object> record) {
        if (null == record || record.isEmpty()) {
            throw new BadRequestException("No update properties");
        }

        ModelExecutionContext context = new DefaultModelExecutionContext(this.context);
        ex.processReplaceRecord(context, id, record);
        if (null != ex.handler) {
            ex.handler.processReplaceRecord(context, id, record);
        }

        Set<String> removes = new HashSet<>();

        if (!ex.processReplaceNONEProperties(context, id, record, removes)) {
            for (FieldMapping fm : em.getFieldMappings()) {
                if (!fm.isUpdate()) {
                    continue;
                }

                if (!record.containsKey(fm.getFieldName())) {
                    if (fm.isNullable()) {
                        record.put(fm.getFieldName(), null);
                    } else {
                        if (null != fm.getDefaultValue()) {
                            record.put(fm.getFieldName(), fm.getDefaultValue().getValue());
                        } else {
                            throw new BadRequestException("Property '" + fm.getFieldName() + "' is required!");
                        }
                    }
                } else {
                    Object value = record.get(fm.getFieldName());
                    if (null == value && !fm.isNullable()) {
                        if (null != fm.getDefaultValue()) {
                            record.put(fm.getFieldName(), fm.getDefaultValue().getValue());
                        } else {
                            throw new BadRequestException("Property '" + fm.getFieldName() + "' is required, but null!");
                        }
                    }
                }
            }
        }

        removes.forEach(record::remove);

        try {
            ex.preReplace(context, id, record);
            int affected = em.withContextListeners(listeners, () -> doUpdate(context, IdOrKey.ofId(id), record, false));
            Object entity = ex.postReplaceRecord(context, id, affected);

            UpdateOneResult result = new UpdateOneResult(affected, entity);

            ex.completeReplace(context, result, null);

            return result;
        }catch (Throwable e) {
            ex.completeReplace(context, null, e);
            throw e;
        }
    }

    @Override
    public UpdateOneResult partialUpdateOne(Object id, Partial partial) {
        if (null == partial || partial.isEmpty()) {
            throw new BadRequestException("No update properties");
        }

        return partialUpdateOne(id, partial.getProperties());
    }

    @Override
    public UpdateOneResult partialUpdateOne(Object id, Map<String, Object> properties) {
        ModelExecutionContext context = new DefaultModelExecutionContext(this.context);
        ex.processUpdateProperties(context, id, properties);
        if (null != ex.handler) {
            ex.handler.processUpdateProperties(context, id, properties);
        }

        ModelDynamic dynamic = ex.resolveUpdateDynamic(context, id, properties);
        int affected;
        try {
            if(null != dynamic) {
                context.setDynamic(dynamic);
                EntityMapping.setDynamic(dynamic.getEntityDynamic());
            }

            ex.preUpdate(context, id, properties);

            if (!em.isRemoteRest()) {
                affected = em.withContextListeners(listeners, () -> {
                    if (null != handler) {
                        return handler.partialUpdate(context, id, properties);
                    } else {
                        return doUpdate(context, IdOrKey.ofId(id), properties, true);
                    }
                });
            } else {
                RestResource restResource = restResourceFactory.createResource(dao.getOrmContext(), em);
                if (restResource.update(id, properties)) {
                    affected = 1;
                } else {
                    affected = 0;
                }
            }

            Object entity = ex.postUpdateProperties(context, id, affected);

            UpdateOneResult result = new UpdateOneResult(affected, entity);

            ex.completeUpdate(context, result, null);

            return result;
        }catch (Throwable e) {
            ex.completeUpdate(context, null, e);
            throw e;
        }finally {
            if(null != dynamic) {
                EntityMapping.removeDynamic();
            }
        }
    }

    @Override
    public UpdateOneResult partialUpdateOneByKey(Map<String, Object> key, Map<String, Object> properties) {
        if(em.isRemoteRest()) {
            throw new IllegalStateException("Can't update by key for remote entity");
        }
        if(key.isEmpty()) {
            throw new IllegalStateException("Can't update with empty key");
        }

        ModelExecutionContext context = new DefaultModelExecutionContext(this.context);
        ex.processUpdatePropertiesByKey(context, key, properties);

        int affected;
        try {
            ex.preUpdateByKey(context, key, properties);

            affected = em.withContextListeners(listeners, () -> {
                    return doUpdate(context, IdOrKey.ofKey(key), properties, true);
            });

            Object entity = ex.postUpdatePropertiesByKey(context, key, affected);

            UpdateOneResult result = new UpdateOneResult(affected, entity);

            ex.completeUpdate(context, result, null);

            return result;
        }catch (Throwable e) {
            ex.completeUpdate(context, null, e);
            throw e;
        }
    }

    protected int doUpdate(ModelExecutionContext context, IdOrKey idOrFilters, Map<String, Object> properties, boolean partial) {
        Map<RelationProperty, Object[]> relationProperties = new LinkedHashMap<>();

        Set<String> removes = new HashSet<>();

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String name = entry.getKey();

            MApiProperty p = tryGetProperty(context, am, name);

            if (null == p) {
                if (partial) {
                    if (ex.handleUpdatePropertyNotFound(context, name, entry.getValue(), removes)) {
                        continue;
                    }
                } else {
                    if (ex.handleReplacePropertyNotFound(context, name, entry.getValue(), removes)) {
                        continue;
                    }
                }
                throw new BadRequestException("Property '" + name + "' not exists!");
            }

            if (p.isNotUpdatableExplicitly()) {
                if (partial) {
                    if (ex.handleUpdatePropertyReadonly(context, name, entry.getValue(), removes)) {
                        continue;
                    }
                    throw new BadRequestException("Property '" + name + "' is not updatable!");
                } else {
                    if (ex.handleReplacePropertyReadonly(context, name, entry.getValue(), removes)) {
                        continue;
                    }
                    removes.add(name);
                }
            }

            if (null != p.getMetaProperty() && p.getMetaProperty().isReference()) {
                Object v = properties.get(name);
                if (null == v) {
                    continue;
                }

                RelationProperty rp = em.getRelationProperty(name);

                Enumerable e = Enumerables.tryOf(v);
                if (null == e) {
                    relationProperties.put(rp, new Object[]{v});
                } else {
                    relationProperties.put(rp, e.toArray());
                }
                continue;
            }

            tryHandleSpecialValue(entry, p);
        }

        Errors errors = dao.validate(em, properties, properties.keySet());
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        if (!removes.isEmpty()) {
            removes.forEach(properties::remove);
        }

        if (null != ex.handler && idOrFilters.isId()) {
            ex.handler.preUpdateProperties(context, idOrFilters.id, properties);
        }

        if (properties.isEmpty()) {
            throw new BadRequestException("No update properties");
        }

        AtomicInteger result = new AtomicInteger();

        if (relationProperties.isEmpty()) {
            result.set(executeUpdate(context, idOrFilters, properties));
        } else {
            if(idOrFilters.isKey()) {
                throw new IllegalStateException("Relation properties not supported for filtering update");
            }
            dao.doTransaction((conn) -> {
                result.set(executeUpdate(context, idOrFilters, properties));

                if (result.get() > 0) {
                    for (Map.Entry<RelationProperty, Object[]> entry : relationProperties.entrySet()) {
                        //todo : valid for many-to-many only ?

                        RelationProperty rp = entry.getKey();

                        RelationMapping rm = em.getRelationMapping(rp.getRelationName());
                        if (rm.isManyToMany()) {
                            EntityMapping joinEntity = md.getEntityMapping(rm.getJoinEntityName());

                            RelationMapping manyToOne1 = joinEntity.tryGetKeyRelationMappingOfTargetEntity(em.getEntityName());

                            String joinIdFieldName1 = manyToOne1.getJoinFields()[0].getLocalFieldName();

                            String localName;
                            String targetName;

                            if (joinEntity.getKeyFieldMappings()[0].getFieldName().equals(manyToOne1.getJoinFields()[0].getLocalFieldName())) {
                                localName = joinEntity.getKeyFieldNames()[0];
                                targetName = joinEntity.getKeyFieldNames()[1];
                            } else {
                                localName = joinEntity.getKeyFieldNames()[1];
                                targetName = joinEntity.getKeyFieldNames()[0];
                            }

                            Object localId = idOrFilters.id;

                            List<Map<String, Object>> batchId = new ArrayList<>();

                            for (Object targetId : entry.getValue()) {
                                batchId.add(New.hashMap(localName, localId, targetName, targetId));
                            }

                            //delete
                            dao.createCriteriaQuery(joinEntity).where(joinIdFieldName1 + " = ?", idOrFilters.id).delete();

                            //insert
                            dao.batchInsert(joinEntity, batchId);
                        }
                    }
                }

            });
        }

        return result.get();
    }

//    protected int executeUpdate(UpdateCommand update, Object id) {
//        int r = dao.withEvents(() -> update.execute());
//
//        if (null != ex.handler) {
//            ex.handler.postUpdateProperties(context, id, r);
//        }
//
//        return r;
//    }

    protected int executeUpdate(ModelExecutionContext context, IdOrKey idOrFilters, Map<String, Object> properties) {
        int r;
        if(idOrFilters.isId()) {
            final Object id = idOrFilters.id;
            UpdateCommand update =
                    dao.cmdUpdate(em.getEntityName()).withId(idOrFilters.id).from(properties);
            update.setAttribute(UpdateCommand.ORIGINAL_RECORD, context.getAttribute(UpdateCommand.ORIGINAL_RECORD));
            r = dao.withEvents(() -> update.execute());
            if (null != ex.handler) {
                ex.handler.postUpdateProperties(context, id, r);
            }
        }else {
            r = dao.createCriteriaQuery(em).where(idOrFilters.key).update(properties);
        }
        return r;
    }
}
