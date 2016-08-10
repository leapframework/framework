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

import leap.core.validation.ValidationException;
import leap.lang.Beans;
import leap.lang.Strings;
import leap.lang.Types;
import leap.lang.convert.Converts;
import leap.lang.reflect.Reflection;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.model.Model;
import leap.orm.query.CriteriaQuery;
import leap.web.api.mvc.params.Partial;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.query.Filters;
import leap.web.exception.BadRequestException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ModelController<T extends Model> extends ApiController {

    private final Class<T> modelClass = getModelClass();
    private final Dao      dao        = Dao.of(modelClass);
    private final EntityMapping em    = dao.getOrmContext().getMetadata().getEntityMapping(modelClass);

    private Class<T> getModelClass() {
        return (Class<T>) Types.getActualTypeArgument(this.getClass().getGenericSuperclass());
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
        createRecord(request, id);
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
    protected ApiResponse<T> createAndReturn(Object request, Object id) {
        return ApiResponse.of(createRecord(request, id));
    }

    /**
     * Creates a new record of model.
     *
     * @param request the request bean contains properties of model.
     * @param id the id of model, pass null if use auto generated id.
     *
     * @return the created model object.
     */
    protected T createRecord(Object request, Object id) {
        T m = Reflection.newInstance(modelClass);

        if(null != id) {
            m.id(id);
        }

        Map<String,Object> props = Beans.toMap(request);

        m.setAll(props);

        if (!m.validate(props.keySet())) {
            throw new ValidationException(m.errors());
        }

        m.create();

        return m;
    }

    /**
     * Gets the record of the specified id.
     */
    protected ApiResponse<T> get(Object id) {
        return ApiResponse.of(dao.findOrNull(modelClass,id));
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
        //todo : validates the query options.

        CriteriaQuery<T> query = dao.createCriteriaQuery(modelClass);

        List<T> list;
        if(null == options) {
            list = query.limit(apiConfig.getMaxPageSize()).list();
        }else{
            //todo : parse and validate order by expression.
            if(!Strings.isEmpty(options.getOrderBy())) {
                query.orderBy(options.getOrderBy());
            }

            //todo : parse and validate filters expression.
            if(!Strings.isEmpty(options.getFilters())) {
                Map<String,String> props = Filters.parse(options.getFilters());

                StringBuilder where = new StringBuilder();

                Map<String,Object> params = new HashMap<>(props.size());

                final AtomicInteger i = new AtomicInteger(0);
                props.forEach((name,value) -> {
                    FieldMapping fm = em.getFieldMapping(name);
                    if(null == fm) {
                        throw new BadRequestException("The filtering property '" + name + "' not found in model");
                    }

                    if(i.getAndIncrement() > 0) {
                        where.append(" and ");
                    }
                    where.append(name).append("=:").append(name);

                    params.put(name, Converts.convert(value, fm.getJavaType()));
                });

                query.where(where.toString()).params(params);
            }

            list = query.pageResult(options.getPage(apiConfig.getDefaultPageSize())).list();
        }

        return ApiResponse.of(list);
    }

    /**
     * Update partial properties of model.
     */
    protected ApiResponse updatePartial(Object id, Partial<T> partial) {
        if(null == partial || partial.isEmpty()) {
            return ApiResponse.badRequest("No update properties");
        }

        T model = Reflection.newInstance(modelClass);

        model.id(id);
        model.setAll(partial.getProperties());

        if(!model.validate(partial.getProperties().keySet())) {
            throw new ValidationException(model.errors());
        }

        if(model.tryUpdate()) {
            return ApiResponse.NO_CONTENT;
        }

        return ApiResponse.NOT_FOUND;
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