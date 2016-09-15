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
import leap.orm.command.UpdateCommand;
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
import java.util.concurrent.atomic.AtomicInteger;

public class ModelUpdateExecutor extends ModelExecutorBase {

    public ModelUpdateExecutor(ApiConfig c, MApiModel am, Dao dao, EntityMapping em) {
        super(c, am, dao, em);
    }

    public UpdateOneResult partialUpdateOne(Object id, Partial partial) {
        if (null == partial || partial.isEmpty()) {
            throw new BadRequestException("No update properties");
        }

        Map<RelationProperty, Object[]> relationProperties = new LinkedHashMap<>();

        Map<String, Object> properties = partial.getProperties();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String name = entry.getKey();

            MApiProperty p = am.tryGetProperty(name);

            if (null == p) {
                throw new BadRequestException("Property '" + name + "' not exists!");
            }

            if (p.isNotUpdatableExplicitly()) {
                throw new BadRequestException("Property '" + name + "' is not updatable!");
            }

            if (null != p.getProperty() && p.getProperty().isReference()) {
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
        }

        Errors errors = dao.validate(em, properties, properties.keySet());
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        UpdateCommand update =
                dao.cmdUpdate(em.getEntityName()).id(id).setAll(partial.getProperties());

        AtomicInteger result = new AtomicInteger();

        if(relationProperties.isEmpty()) {
            result.set(update.execute());
        }else {
            dao.doTransaction((conn) -> {
                update.execute();

                for(Map.Entry<RelationProperty, Object[]> entry : relationProperties.entrySet()) {
                    //todo : valid for many-to-many only ?

                    RelationProperty rp = entry.getKey();

                    RelationMapping rm = em.getRelationMapping(rp.getRelationName());
                    if(rm.isManyToMany()) {
                        EntityMapping joinEntity = md.getEntityMapping(rm.getJoinEntityName());

                        RelationMapping manyToOne1 = joinEntity.tryGetKeyRelationMappingOfTargetEntity(em.getEntityName());

                        String joinIdFieldName1 = manyToOne1.getJoinFields()[0].getLocalFieldName();

                        boolean localFirst = true;
                        if(!joinEntity.getKeyFieldMappings()[0].getFieldName().equals(joinIdFieldName1)){
                            localFirst = false;
                        }

                        Object localId = id;

                        List<Object[]> batchId = new ArrayList<>();

                        for(Object targetId : entry.getValue()) {

                            if(localFirst) {
                                batchId.add(new Object[]{localId, targetId});
                            }else{
                                batchId.add(new Object[]{targetId, localId});
                            }

                        }

                        //delete
                        dao.createCriteriaQuery(joinEntity).where(joinIdFieldName1 + " = ?", id).delete();

                        //insert
                        dao.batchInsert(joinEntity, batchId);
                    }
                }

            });
        }

        return new UpdateOneResult(result.get());
    }
}
