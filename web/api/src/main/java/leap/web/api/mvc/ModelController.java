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
import leap.web.annotation.NonAction;
import leap.web.api.config.ApiConfig;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.meta.model.MApiProperty;
import leap.web.api.mvc.params.Partial;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.query.Filters;
import leap.web.api.query.FiltersParser;
import leap.web.exception.BadRequestException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ModelController<T extends Model> extends ApiController implements ApiInitializable {

    private final Class<T> modelClass = getModelClass();
    private final Dao      dao        = Dao.of(modelClass);
    private final EntityMapping em    = dao.getOrmContext().getMetadata().getEntityMapping(modelClass);
    private MApiModel           am;

    private Class<T> getModelClass() {
        return (Class<T>) Types.getActualTypeArgument(this.getClass().getGenericSuperclass());
    }

    @NonAction
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
                applyFilters(query, options.getFilters());
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

    private void applyFilters(CriteriaQuery<T> query, String expr) {
        Filters filters = FiltersParser.parse(expr);

        StringBuilder where = new StringBuilder();

        List<Object> args = new ArrayList<>();

        FiltersParser.Node[] nodes = filters.nodes();
        for(int i=0;i<nodes.length;i++) {
            FiltersParser.Node node = nodes[i];

            if(node.isParen()) {
                where.append(node.literal());
                continue;
            }

            if(node.isAnd()) {
                where.append(" and ");
                continue;
            }

            if(node.isOr()) {
                where.append(" or ");
                continue;
            }

            String name = nodes[i].literal();
            FiltersParser.Token op = nodes[++i].token();
            String value = nodes[++i].literal();

            MApiProperty ap = am.tryGetProperty(name);
            if(null == ap) {
                throw new BadRequestException("Property '" + name + "' not exists in model '" + am.getName() + "'");
            }

            if(!ap.isFilterable()) {
                throw new BadRequestException("Property '" + name + "' is not filterable!");
            }

            FieldMapping fm = em.getFieldMapping(name);

            where.append(name).append(' ').append(toSqlOperator(op)).append(" ?");

            args.add(Converts.convert(value, fm.getJavaType()));
        }

        query.where(where.toString(), args.toArray());
    }

    private String toSqlOperator(FiltersParser.Token op) {

        if(op == FiltersParser.Token.EQ) {
            return "=";
        }

        if(op == FiltersParser.Token.GE) {
            return ">=";
        }

        if(op == FiltersParser.Token.LE) {
            return "<=";
        }

        if(op == FiltersParser.Token.GT) {
            return ">";
        }

        if(op == FiltersParser.Token.LT) {
            return "<";
        }

        if(op == FiltersParser.Token.NE) {
            return "<>";
        }

        //todo : in
//        if(op == FiltersParser.Token.IN)

        if(op == FiltersParser.Token.LIKE) {
            return "like";
        }

        throw new IllegalStateException("Not supported operator '" + op + "'");
    }


}