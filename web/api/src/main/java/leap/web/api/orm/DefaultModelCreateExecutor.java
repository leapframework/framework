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
import leap.core.value.Record;
import leap.lang.Beans;
import leap.lang.Enumerable;
import leap.lang.Enumerables;
import leap.lang.New;
import leap.orm.command.InsertCommand;
import leap.orm.event.EntityListeners;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.Mappings;
import leap.orm.mapping.RelationMapping;
import leap.orm.mapping.RelationProperty;
import leap.web.api.meta.model.MApiProperty;
import leap.web.api.mvc.params.Partial;
import leap.web.api.remote.RestResource;
import leap.web.exception.BadRequestException;

import java.util.*;

public class DefaultModelCreateExecutor extends ModelExecutorBase implements ModelCreateExecutor {

    protected final ModelCreateExtension ex;

    protected CreateHandler   createOneHandler;
    protected EntityListeners listeners;

    public DefaultModelCreateExecutor(ModelExecutorContext context, ModelCreateExtension ex) {
        super(context);
        this.ex = null == ex ? ModelCreateExtension.EMPTY : ex;
    }

    @Override
    public ModelCreateExecutor withHandler(CreateHandler handler) {
        this.createOneHandler = handler;
        return this;
    }

    @Override
    public ModelCreateExecutor withListeners(EntityListeners listeners) {
        this.listeners = listeners;
        return this;
    }

    @Override
    public CreateOneResult createOne(Object request, Object id, Map<String, Object> extraProperties) {
        ModelExecutionContext context = new DefaultModelExecutionContext(this.context);

        Object v = ex.processCreationParams(context, request);
        if (null != v) {
            request = v;
        }
        if (null != ex.handler) {
            Object r = ex.handler.processCreationParams(context, request);
            if (null != r) {
                request = r;
            }
        }

        Map<String, Object> properties;
        if (request instanceof Partial) {
            properties = ((Partial) request).getProperties();
        } else if (request instanceof Map) {
            properties = (Map) request;
        } else {
            properties = Beans.toMap(request);
        }

        if (properties.isEmpty()) {
            throw new BadRequestException("No create properties!");
        }

        if (null != extraProperties) {
            properties.putAll(extraProperties);
        }

        ex.processCreationRecord(context, properties);
        if (null != ex.handler) {
            ex.handler.processCreationRecord(context, properties);
        }

        final ModelDynamic dynamic = ex.resolveCreateDynamic(context, properties);
        try {
            if(null != dynamic) {
                context.setDynamic(dynamic);
                EntityMapping.setDynamic(dynamic.getEntityDynamic());
            }

            Map<RelationProperty, Object[]> relationProperties = new LinkedHashMap<>();

            Set<String> removes = new HashSet<>();

            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String       name = entry.getKey();
                MApiProperty p    = tryGetProperty(context, am, name);

                if (null == p) {
                    if (ex.handleCreationPropertyNotFound(context, name, entry.getValue(), removes)) {
                        continue;
                    }
                    throw new BadRequestException("Property '" + name + "' not exists!");
                }

                if (p.isNotCreatableExplicitly()) {
                    if (null == properties.get(name)) {
                        removes.add(name);
                    } else {
                        if (ex.handleCreationPropertyReadonly(context, name, entry.getValue(), removes)) {
                            continue;
                        }
                        throw new BadRequestException("Property '" + name + "' is not creatable!");
                    }
                }

                if (p.isReference()) {
                    v = properties.get(name);
                    if (null == v) {
                        continue;
                    }

                    final RelationProperty rp = em.getRelationProperty(name);
                    final RelationMapping rm = em.getRelationMapping(rp.getRelationName());

                    final Enumerable e = Enumerables.tryOf(v);
                    if (null == e) {
                        if(!rm.isNestedCreatable()) {
                            throw new BadRequestException("Relation '" + rm.getName() + "' is not nested creatable!");
                        }
                        relationProperties.put(rp, new Object[]{v});
                    } else {
                        if(e.size() > 0 && !rm.isNestedCreatable()) {
                            throw new BadRequestException("Relation '" + rm.getName() + "' is not nested creatable!");
                        }
                        relationProperties.put(rp, e.toArray());
                    }
                }

                tryHandleSpecialValue(entry, p);
            }

            //Removes the not creatable properties.
            removes.forEach(properties::remove);

            Errors errors = dao.validate(em, properties);
            if (!errors.isEmpty()) {
                throw new ValidationException(errors);
            }

            ex.preCreateRecord(context, properties);
            if (null != ex.handler) {
                ex.handler.preCreateRecord(context, properties);
            }

            CreationImpl creation = new CreationImpl(id, properties, relationProperties);

            Object createdId = null;
            Record record    = null;

            if (!em.isRemoteRest()) {
                createdId = em.withContextListeners(listeners, () -> {
                    if (null != createOneHandler) {
                        return createOneHandler.create(context, creation);
                    } else {
                        return createByDb(creation);
                    }
                });

                if (null != createdId) {
                    record = dao.find(em, createdId);
                }
            } else {
                RestResource restResource =
                        restResourceFactory.createResource(dao.getOrmContext(), em);
                record = restResource.create(properties);
                createdId = Mappings.getId(em, record);
            }

            if (null == createdId) {
                return new CreateOneResult(null, null);
            }

            if (null != record) {
                record.put("$id", createdId);
            }

            Object entity = ex.postCreateRecord(context, createdId, record);
            if (null != entity) {
                return new CreateOneResult(createdId, entity);
            } else {
                return new CreateOneResult(createdId, record);
            }
        }finally {
            if(null != dynamic) {
                EntityMapping.removeDynamic();
            }
        }
    }

    protected void executeInsert(InsertCommand insert) {
        dao.withEvents(() -> insert.execute());
        if (null != ex.handler) {
            ex.handler.postCreateRecord(context, insert.id());
        }
    }

    protected Object createByDb(CreationImpl creation) {
        InsertCommand insert = dao.cmdInsert(em.getEntityName()).from(creation.getProperties());
        if (null != creation.getId()) {
            insert.withId(creation.getId());
        }

        if (creation.getRelationProperties().isEmpty()) {
            executeInsert(insert);
        } else {
            dao.doTransaction((conn) -> {
                executeInsert(insert);

                for (Map.Entry<RelationProperty, Object[]> entry : creation.getRelationProperties().entrySet()) {
                    RelationProperty rp = entry.getKey();
                    RelationMapping rm = em.getRelationMapping(rp.getRelationName());

                    if (!rm.isNestedCreatable() || entry.getValue().length <= 0) {
                        continue;
                    }

                    if (rm.isOneToMany()) {
                        EntityMapping targetEntity = md.getEntityMapping(rm.getTargetEntityName());
                        RelationMapping oneToMany = targetEntity.tryGetRelationMapping(rm.getInverseRelationName());

                        String localName = oneToMany.getJoinFields()[0].getLocalFieldName();
                        Object localId = insert.id();

                        List<Map> batchList = new ArrayList<>();
                        for (Object targetValue : entry.getValue()) {
                            if (targetValue instanceof Map) {
                                Map targetMap = (Map) targetValue;
                                targetMap.put(localName, localId);
                                batchList.add(targetMap);
                            }
                        }

                        dao.batchInsert(targetEntity, batchList);
                    } else if (rm.isManyToMany()) {
                        EntityMapping joinEntity = md.getEntityMapping(rm.getJoinEntityName());
                        RelationMapping manyToOne1 = joinEntity.tryGetKeyRelationMappingOfTargetEntity(em.getEntityName());

                        String localName;
                        String targetName;

                        if (joinEntity.getKeyFieldMappings()[0].getFieldName().equals(manyToOne1.getJoinFields()[0].getLocalFieldName())) {
                            localName = joinEntity.getKeyFieldNames()[0];
                            targetName = joinEntity.getKeyFieldNames()[1];
                        } else {
                            localName = joinEntity.getKeyFieldNames()[1];
                            targetName = joinEntity.getKeyFieldNames()[0];
                        }

                        Object localId = insert.id();

                        List<Map<String, Object>> batchId = new ArrayList<>();

                        for (Object targetId : entry.getValue()) {
                            batchId.add(New.hashMap(localName, localId, targetName, targetId));
                        }

                        dao.batchInsert(joinEntity, batchId);
                    }
                }
            });
        }

        return insert.id();
    }

    protected static class CreationImpl implements CreateParams {

        private final Object                          id;
        private final Map<String, Object>             properties;
        private final Map<RelationProperty, Object[]> relationProperties;

        public CreationImpl(Object id, Map<String, Object> properties, Map<RelationProperty, Object[]> relationProperties) {
            this.id = id;
            this.properties = properties;
            this.relationProperties = relationProperties;
        }

        public Object getId() {
            return id;
        }

        @Override
        public Map<String, Object> getProperties() {
            return properties;
        }

        @Override
        public Map<RelationProperty, Object[]> getRelationProperties() {
            return relationProperties;
        }

        @Override
        public Map<String, Object> getCombinedProperties() {
            if (null == relationProperties || relationProperties.isEmpty()) {
                return properties;
            }

            Map<String, Object> m = new LinkedHashMap<>(properties);
            relationProperties.forEach((rp, vals) -> {
                if (vals.length == 0) {
                    return;
                }
                if (rp.isMany()) {
                    m.put(rp.getName(), vals);
                } else {
                    if (vals.length > 1) {
                        throw new IllegalStateException("Found multi values fo to-one relation property '" + rp.getName() + "'");
                    }
                    m.put(rp.getName(), vals[0]);
                }
            });
            return m;
        }
    }

}
