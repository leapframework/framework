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

import leap.lang.Types;
import leap.orm.OrmMetadata;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.web.api.annotation.ResourceWrapper;
import leap.web.api.config.ApiConfig;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.mvc.params.DeleteOptions;
import leap.web.api.mvc.params.Partial;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.mvc.params.QueryOptionsBase;
import leap.web.api.orm.*;

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
    protected MApiModel am;

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
    protected ApiResponse createAndReturn(Object request, Map<String, Object> extraProperties) {
        Object id = createRecordAndReturnId(request, null, extraProperties);
        return ApiResponse.created(dao.find(em, id));
    }

    /**
     * Creates the record with specified id.
     */
    protected ApiResponse createAndReturn(Object request, Object id, Map<String, Object> extraProperties) {
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
     * @param id      the id of model, pass null if use auto generated id.
     * @return the id of new record.
     */
    protected Object createRecordAndReturnId(Object request, Object id, Map<String, Object> extraProperties) {
        ModelCreateExecutor executor = new ModelCreateExecutor(apiConfig, am, dao, em);

        return executor.createOne(request, id, extraProperties).id;
    }

    /**
     * Gets the record of the specified id.
     */
    protected ApiResponse get(Object id) {
        return ApiResponse.of(dao.findOrNull(em, id));
    }

    /**
     * Gets the record of the specified id.
     */
    protected ApiResponse get(Object id, QueryOptionsBase options) {
        ModelQueryExecutor executor = new ModelQueryExecutor(apiConfig, am, dao, em);

        QueryOneResult result = executor.queryOne(id, options);

        return ApiResponse.of(result.record);
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

        if (result.count == -1) {
            return ApiResponse.of(result.list);
        } else {
            return ApiResponse.of(result.list).setHeader("X-Total-Count", String.valueOf(result.count));
        }
    }

    /**
     * Update partial properties of model.
     */
    protected ApiResponse updatePartial(Object id, Partial<T> partial) {
        ModelUpdateExecutor executor = new ModelUpdateExecutor(apiConfig, am, dao, em);

        UpdateOneResult result = executor.partialUpdateOne(id, partial);

        if (result.affectedRows > 0) {
            return ApiResponse.NO_CONTENT;
        } else {
            return ApiResponse.NOT_FOUND;
        }
    }

    /**
     * Deletes a record.
     */
    protected ApiResponse delete(Object id) {
        if (dao.delete(modelClass, id) > 0) {
            return ApiResponse.ACCEPTED;
        } else {
            return ApiResponse.NOT_FOUND;
        }
    }

    /**
     * Deletes a record.
     */
    protected ApiResponse deleteFully(Object id) {
        if (dao.deleteFully(modelClass, id)) {
            return ApiResponse.ACCEPTED;
        } else {
            return ApiResponse.NOT_FOUND;
        }
    }

    /**
     * Deletes a record.
     */
    protected ApiResponse delete(Object id, DeleteOptions options) {
        ModelDeleteExecutor executor = new ModelDeleteExecutor(apiConfig, am, dao, em);

        if(executor.deleteOne(id, options).success) {
            return ApiResponse.ACCEPTED;
        }else{
            return ApiResponse.NOT_FOUND;
        }
    }
}