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
import leap.lang.*;
import leap.orm.command.UpdateCommand;
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

    public DefaultModelUpdateExecutor(ModelExecutorContext context, ModelUpdateExtension ex) {
        super(context);
        this.ex = null == ex ? ModelUpdateExtension.EMPTY : ex;
    }

    @Override
    public UpdateOneResult replaceUpdateOne(Object id, Map<String, Object> record) {
        if(null == record || record.isEmpty()) {
            throw new BadRequestException("No update properties");
        }

        ModelExecutionContext context = new DefaultModelExecutionContext(this.context);
        ex.processReplaceRecord(context, id, record);
        if(null != ex.handler) {
            ex.handler.processReplaceRecord(context, id, record);
        }

        Set<String> removes = new HashSet<>();

        if(!ex.processReplaceNONEProperties(context, id, record, removes)) {
            for(FieldMapping fm : em.getFieldMappings()) {
                if(!fm.isUpdate()) {
                    continue;
                }

                if(!record.containsKey(fm.getFieldName())) {
                    if(fm.isNullable()) {
                        record.put(fm.getFieldName(), null);
                    }else {
                        if(null != fm.getDefaultValue()) {
                            record.put(fm.getFieldName(), fm.getDefaultValue().getValue());
                        }else {
                            throw new BadRequestException("Property '" + fm.getFieldName() + "' is required!");
                        }
                    }
                }else {
                    Object value = record.get(fm.getFieldName());
                    if(null == value && !fm.isNullable()) {
                        if(null != fm.getDefaultValue()) {
                            record.put(fm.getFieldName(), fm.getDefaultValue().getValue());
                        }else {
                            throw new BadRequestException("Property '" + fm.getFieldName() + "' is required, but null!");
                        }
                    }
                }
            }
        }

        removes.forEach(record::remove);

        int affected = doUpdate(context, id, record, false);
        Object entity = ex.postReplaceRecord(context, id, affected);

        return new UpdateOneResult(affected, entity);
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
        if(null != ex.handler) {
            ex.handler.processUpdateProperties(context, id, properties);
        }

        int affected;

        if(!em.isRemoteRest()) {
            affected = doUpdate(context, id, properties, true);
        }else {
            RestResource restResource = restResourceFactory.createResource(dao.getOrmContext(), em);
            if(restResource.update(id, properties)) {
                affected = 1;
            }else {
                affected = 0;
            }
        }

        Object entity = ex.postUpdateProperties(context, id, affected);

        return new UpdateOneResult(affected, entity);
    }

    protected int doUpdate(ModelExecutionContext context, Object id, Map<String, Object> properties, boolean partial) {
        Map<RelationProperty, Object[]> relationProperties = new LinkedHashMap<>();

        Set<String> removes = new HashSet<>();

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String name = entry.getKey();

            MApiProperty p = am.tryGetProperty(name);

            if (null == p) {
                if(partial) {
                    if(ex.handleUpdatePropertyNotFound(context, name, entry.getValue(), removes)) {
                        continue;
                    }
                }else {
                    if(ex.handleReplacePropertyNotFound(context, name, entry.getValue(), removes)) {
                        continue;
                    }
                }
                throw new BadRequestException("Property '" + name + "' not exists!");
            }

            if (p.isNotUpdatableExplicitly()) {
                if(partial) {
                    if(ex.handleUpdatePropertyReadonly(context, name, entry.getValue(), removes)) {
                        continue;
                    }
                    throw new BadRequestException("Property '" + name + "' is not updatable!");
                }else {
                    if(ex.handleReplacePropertyReadonly(context, name, entry.getValue(), removes)) {
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

        if(!removes.isEmpty()) {
            removes.forEach(properties::remove);
        }

        if(null != ex.handler) {
            ex.handler.preUpdateProperties(context, id, properties);
        }

        if(properties.isEmpty()) {
            throw new BadRequestException("No update properties");
        }

        UpdateCommand update =
                dao.cmdUpdate(em.getEntityName()).withId(id).from(properties);

        AtomicInteger result = new AtomicInteger();

        dao.withEvents(() -> {
            if(relationProperties.isEmpty()) {
                result.set(executeUpdate(update, id));
            }else {
                dao.doTransaction((conn) -> {
                    result.set(executeUpdate(update, id));

                    if(result.get() > 0) {
                        for(Map.Entry<RelationProperty, Object[]> entry : relationProperties.entrySet()) {
                            //todo : valid for many-to-many only ?

                            RelationProperty rp = entry.getKey();

                            RelationMapping rm = em.getRelationMapping(rp.getRelationName());
                            if(rm.isManyToMany()) {
                                EntityMapping joinEntity = md.getEntityMapping(rm.getJoinEntityName());

                                RelationMapping manyToOne1 = joinEntity.tryGetKeyRelationMappingOfTargetEntity(em.getEntityName());

                                String joinIdFieldName1 = manyToOne1.getJoinFields()[0].getLocalFieldName();

                                String localName;
                                String targetName;

                                if(joinEntity.getKeyFieldMappings()[0].getFieldName().equals(manyToOne1.getJoinFields()[0].getLocalFieldName())){
                                    localName  = joinEntity.getKeyFieldNames()[0];
                                    targetName = joinEntity.getKeyFieldNames()[1];
                                }else{
                                    localName  = joinEntity.getKeyFieldNames()[1];
                                    targetName = joinEntity.getKeyFieldNames()[0];
                                }

                                Object localId = id;

                                List<Map<String,Object>> batchId = new ArrayList<>();

                                for(Object targetId : entry.getValue()) {
                                    batchId.add(New.hashMap(localName, localId, targetName, targetId));
                                }

                                //delete
                                dao.createCriteriaQuery(joinEntity).where(joinIdFieldName1 + " = ?", id).delete();

                                //insert
                                dao.batchInsert(joinEntity, batchId);
                            }
                        }
                    }

                });
            }
        });

        return result.get();
    }

    protected int executeUpdate(UpdateCommand update, Object id) {
        int r = update.execute();

        if(null != ex.handler) {
            ex.handler.postUpdateProperties(context, id, r);
        }

        return r;
    }

}
