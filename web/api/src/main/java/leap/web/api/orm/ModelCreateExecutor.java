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
import leap.orm.command.InsertCommand;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.RelationMapping;
import leap.orm.mapping.RelationProperty;
import leap.web.api.config.ApiConfig;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.meta.model.MApiProperty;
import leap.web.api.mvc.params.Partial;
import leap.web.exception.BadRequestException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModelCreateExecutor extends ModelExecutorBase {

    public ModelCreateExecutor(ApiConfig c, MApiModel am, Dao dao, EntityMapping em) {
        super(c, am, dao, em);
    }

    public ModelCreateResult createOne(Object request, Object id, Map<String, Object> extraProperties) {
        Map<String,Object> properties;
        if(request instanceof Partial) {
            properties = ((Partial) request).getProperties();
        }else{
            properties = Beans.toMap(request);
        }

        if(properties.isEmpty()) {
            throw new BadRequestException("No create properties!");
        }

        if(null != extraProperties) {
            properties.putAll(extraProperties);
        }

        Map<RelationProperty, Object[]> relationProperties = new LinkedHashMap<>();

        for(String name : properties.keySet()) {
            MApiProperty p = am.tryGetProperty(name);
            if(null == p) {
                throw new BadRequestException("Property '" + name + "' not exists!");
            }
            if(p.isNotCreatableExplicitly()) {
                throw new BadRequestException("Property '" + name + "' is not creatable!");
            }

            if(null != p.getProperty() && p.getProperty().isReference()) {

                Object v = properties.get(name);

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
        }

        Errors errors = dao.validate(em, properties);
        if(!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        InsertCommand insert = dao.cmdInsert(em.getEntityName());

        if(null != id) {
            insert.id(id);
        }

        insert.setAll(properties);

        if(relationProperties.isEmpty()) {
            insert.execute();
        }else{

            dao.doTransaction((conn) -> {
                insert.execute();

                for(Map.Entry<RelationProperty, Object[]> entry : relationProperties.entrySet()) {
                    //valid for many-to-many only ?

                    RelationProperty rp = entry.getKey();

                    RelationMapping rm = em.getRelationMapping(rp.getRelationName());
                    if(rm.isManyToMany()) {
                        EntityMapping joinEntity = md.getEntityMapping(rm.getJoinEntityName());

                        RelationMapping manyToOne1 = joinEntity.tryGetKeyRelationMappingOfTargetEntity(em.getEntityName());

                        boolean localFirst = true;
                        if(!joinEntity.getKeyFieldMappings()[0].getFieldName().equals(manyToOne1.getJoinFields()[0].getLocalFieldName())){
                            localFirst = false;
                        }

                        Object localId = insert.id();

                        List<Object[]> batchId = new ArrayList<>();

                        for(Object targetId : entry.getValue()) {

                            if(localFirst) {
                                batchId.add(new Object[]{localId, targetId});
                            }else{
                                batchId.add(new Object[]{targetId, localId});
                            }

                        }

                        dao.batchInsert(joinEntity, batchId);
                    }
                }

            });
        }

        return new ModelCreateResult(insert.id());
    }

}
