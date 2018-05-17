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
import leap.lang.Beans;
import leap.lang.Enumerable;
import leap.lang.Enumerables;
import leap.lang.New;
import leap.orm.command.InsertCommand;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.RelationMapping;
import leap.orm.mapping.RelationProperty;
import leap.web.api.meta.model.MApiProperty;
import leap.web.api.mvc.params.Partial;
import leap.web.exception.BadRequestException;

import java.util.*;

public class DefaultModelCreateExecutor extends ModelExecutorBase implements ModelCreateExecutor {

    protected final ModelCreateExtension ex;

    public DefaultModelCreateExecutor(ModelExecutorContext context, ModelCreateExtension ex) {
        super(context);
        this.ex = null == ex ? ModelCreateExtension.EMPTY : ex;
    }

    @Override
    public CreateOneResult createOne(Object request, Object id, Map<String, Object> extraProperties) {
        Object v = ex.processCreationParams(context, request);
        if(null != v) {
            request = v;
        }
        if(null != ex.handler) {
            Object r = ex.handler.processCreationParams(context, request);
            if(null != r) {
                request = r;
            }
        }

        Map<String,Object> properties;
        if(request instanceof Partial) {
            properties = ((Partial) request).getProperties();
        }else if(request instanceof Map) {
            properties = (Map)request;
        }else{
            properties = Beans.toMap(request);
        }

        if(properties.isEmpty()) {
            throw new BadRequestException("No create properties!");
        }

        if(null != extraProperties) {
            properties.putAll(extraProperties);
        }

        ex.processCreationRecord(context, properties);
        if(null != ex.handler) {
            ex.handler.processCreationRecord(context, properties);
        }

        Map<RelationProperty, Object[]> relationProperties = new LinkedHashMap<>();

        Set<String> removingKeys = new HashSet<>();

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String name = entry.getKey();
            MApiProperty p = am.tryGetProperty(name);

            if(null == p) {
                if(!ex.handleCreationPropertyNotFound(context, name, entry.getValue())) {
                    throw new BadRequestException("Property '" + name + "' not exists!");
                }
            }

            if(p.isNotCreatableExplicitly()) {
                if(null == properties.get(name)) {
                    removingKeys.add(name);
                }else{
                    if(!ex.handleCreationPropertyReadonly(context, name, entry.getValue())) {
                        throw new BadRequestException("Property '" + name + "' is not creatable!");
                    }
                }
            }

            if(null != p.getMetaProperty() && p.getMetaProperty().isReference()) {
                v = properties.get(name);
                if(null == v) {
                    continue;
                }

                RelationProperty rp = em.getRelationProperty(name);

                Enumerable e = Enumerables.tryOf(v);
                if(null == e) {
                    relationProperties.put(rp, new Object[]{v});
                }else{
                    relationProperties.put(rp, e.toArray());
                }
            }

            tryHandleDateValue(entry, p);
        }

        //Removes the not creatable properties.
        removingKeys.forEach(properties::remove);

        Errors errors = dao.validate(em, properties);
        if(!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        ex.preCreateRecord(context, properties);
        if(null != ex.handler) {
            ex.handler.preCreateRecord(context, properties);
        }

        InsertCommand insert = dao.cmdInsert(em.getEntityName()).from(properties);
        if(null != id) {
            insert.withId(id);
        }

        if(relationProperties.isEmpty()) {
            executeInsert(insert);
        }else{

            dao.doTransaction((conn) -> {
                executeInsert(insert);

                for(Map.Entry<RelationProperty, Object[]> entry : relationProperties.entrySet()) {
                    //valid for many-to-many only ?

                    RelationProperty rp = entry.getKey();

                    RelationMapping rm = em.getRelationMapping(rp.getRelationName());
                    if(rm.isManyToMany()) {
                        EntityMapping joinEntity = md.getEntityMapping(rm.getJoinEntityName());

                        RelationMapping manyToOne1 = joinEntity.tryGetKeyRelationMappingOfTargetEntity(em.getEntityName());

                        String localName;
                        String targetName;

                        if(joinEntity.getKeyFieldMappings()[0].getFieldName().equals(manyToOne1.getJoinFields()[0].getLocalFieldName())){
                            localName  = joinEntity.getKeyFieldNames()[0];
                            targetName = joinEntity.getKeyFieldNames()[1];
                        }else{
                            localName  = joinEntity.getKeyFieldNames()[1];
                            targetName = joinEntity.getKeyFieldNames()[0];
                        }

                        Object localId = insert.id();

                        List<Map<String,Object>> batchId = new ArrayList<>();

                        for(Object targetId : entry.getValue()) {
                            batchId.add(New.hashMap(localName, localId, targetName, targetId));
                        }

                        dao.batchInsert(joinEntity, batchId);
                    }
                }

            });
        }

        return new CreateOneResult(insert.id());
    }

    protected void executeInsert(InsertCommand insert) {
        insert.execute();
        if(null != ex.handler) {
            ex.handler.postCreateRecord(context, insert.id());
        }
    }

}
