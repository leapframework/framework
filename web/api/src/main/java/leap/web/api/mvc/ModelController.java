/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.web.api.mvc;

import leap.core.validation.Errors;
import leap.core.validation.ValidationException;
import leap.core.value.Record;
import leap.lang.*;
import leap.orm.OrmMetadata;
import leap.orm.command.InsertCommand;
import leap.orm.command.UpdateCommand;
import leap.orm.dao.Dao;
import leap.orm.mapping.*;
import leap.orm.query.CriteriaQuery;
import leap.web.api.annotation.ResourceWrapper;
import leap.web.api.config.ApiConfig;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.meta.model.MApiProperty;
import leap.web.api.mvc.params.QueryOptionsBase;
import leap.web.api.mvc.params.Partial;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.orm.ModelQueryExecutor;
import leap.web.api.orm.QueryListResult;
import leap.web.exception.BadRequestException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The model class must be an orm model/entity class.
 */
@ResourceWrapper
public abstract class ModelController<T> extends ApiController implements ApiInitializable {

    protected final Class<T>      modelClass = getModelClass();
    protected final Dao           dao        = Dao.of(modelClass);
    protected final OrmMetadata   md         = dao.getOrmContext().getMetadata();
    protected final EntityMapping em         = md.getEntityMapping(modelClass);
    protected MApiModel           am;

    private Class<T> getModelClass() {
        return (Class<T>) Types.getActualTypeArgument(this.getClass().getGenericSuperclass());
    }

    public void postApiInitialized(ApiConfig c, ApiMetadata m) {
        am = m.getModel(modelClass);
    }

    /**
     * Creates the record with auto generated id.
     */
    protected ApiResponse create(Object request) {
        return create(request, null);
    }

    /**
     * Creates the record with specified id.
     */
    protected ApiResponse create(Object request, Object id) {
        createRecordAndReturnId(request, id);
        return ApiResponse.OK;
    }

    /**
     * Creates the record with auto generated id.
     */
    protected ApiResponse createAndReturn(Object request) {
        return createAndReturn(request, null);
    }

    /**
     * Creates the record with specified id.
     */
    protected ApiResponse createAndReturn(Object request, Object id) {
        id = createRecordAndReturnId(request, id);
        return ApiResponse.created(dao.find(em, id));
    }

    /**
     * Creates the record with specified id.
     */
    protected ApiResponse createAndReturn(Object request, Map<String,Object> extraProperties) {
        Object id = createRecordAndReturnId(request, null, extraProperties);
        return ApiResponse.created(dao.find(em, id));
    }

    /**
     * Creates the record with specified id.
     */
    protected ApiResponse createAndReturn(Object request, Object id, Map<String,Object> extraProperties) {
        id = createRecordAndReturnId(request, id, extraProperties);
        return ApiResponse.created(dao.find(em, id));
    }

    /**
     * Creates a new record of model and returns the id.
     */
    protected Object createRecordAndReturnId(Object request, Object id) {
        return createRecordAndReturnId(request, id, null);
    }

    /**
     * Creates a new record of model and returns the id.
     *
     * @param request the request bean contains properties of model.
     * @param id the id of model, pass null if use auto generated id.
     *
     * @return the id of new record.
     */
    protected Object createRecordAndReturnId(Object request, Object id, Map<String, Object> extraProperties) {
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

        InsertCommand insert = dao.cmdInsert(modelClass);

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

        return insert.id();
    }

    /**
     * Gets the record of the specified id.
     */
    protected ApiResponse get(Object id) {
        return ApiResponse.of(dao.findOrNull(em,id));
    }

    /**
     * Gets the record of the specified id.
     */
    protected ApiResponse get(Object id, QueryOptionsBase options) {
        Record record = dao.findOrNull(em, id);
        if(null == record) {
            return ApiResponse.of(null);
        }

        if(null != options && !Strings.isEmpty(options.getExpand())) {
            String[] properties = Strings.split(options.getExpand());

            for(String name : properties) {

                MApiProperty ap = am.tryGetProperty(name);
                if(null == ap) {
                    throw new BadRequestException("The expand property '" + name + "' not exists!");
                }

                //todo : check expandable?

                RelationProperty rp = em.tryGetRelationProperty(name);
                if(null == rp) {
                    throw new BadRequestException("Property '" + name + "' cannot be expanded");
                }

                RelationMapping rm = em.getRelationMapping(rp.getRelationName());

                CriteriaQuery query =
                        dao.createCriteriaQuery(rp.getTargetEntityName())
                            .joinById(em.getEntityName(), rm.getInverseRelationName(), "t_" + em.getEntityName(), id);

                if(rp.isMany()) {
                    //todo : limit
                    record.put(rp.getName(), query.list());
                }else{
                    record.put(rp.getName(), query.firstOrNull());
                }
            }
        }

        return ApiResponse.of(record);
    }

    /**
     * Returns all the records.
     */
    protected ApiResponse<List<T>> getAll(QueryOptions options) {
        return queryList(options);
    }

    /**
     * Query the model records with the {@link QueryOptions}.
     */
    protected ApiResponse<List<T>> queryList(QueryOptions options) {
        return queryList(options, null);
    }

    /**
     * Query the model records with the {@link QueryOptions}.
     */
    protected ApiResponse<List<T>> queryList(QueryOptions options, Map<String, Object> filters) {

        ModelQueryExecutor executor = new ModelQueryExecutor(apiConfig, am, dao, em);

        QueryListResult result = executor.queryList(options, filters);

        if(result.count == -1) {
            return ApiResponse.of(result.list);
        }else{
            return ApiResponse.of(result.list).setHeader("X-Total-Count", String.valueOf(result.count));
        }
    }

    /**
     * Update partial properties of model.
     */
    protected ApiResponse updatePartial(Object id, Partial<T> partial) {
        if(null == partial || partial.isEmpty()) {
            return ApiResponse.badRequest("No update properties");
        }

        Map<String,Object> properties = partial.getProperties();
        for(String name : properties.keySet()) {
            MApiProperty p = am.tryGetProperty(name);
            if(null == p) {
                throw new BadRequestException("Property '" + name + "' not exists!");
            }
            if(p.isNotUpdatableExplicitly()) {
                throw new BadRequestException("Property '" + name + "' is not updatable!");
            }
        }

        Errors errors = dao.validate(em, properties, properties.keySet());
        if(!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        UpdateCommand update =
                dao.cmdUpdate(modelClass).id(id).setAll(partial.getProperties());

        if(update.execute() > 0) {
            return ApiResponse.NO_CONTENT;
        }else{
            return ApiResponse.NOT_FOUND;
        }
    }

    /**
     * Deletes a record.
     */
    protected ApiResponse delete(Object id) {
        if(dao.delete(modelClass, id) > 0) {
            return ApiResponse.ACCEPTED;
        }else{
            return ApiResponse.NOT_FOUND;
        }
    }
}