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
import leap.lang.Beans;
import leap.lang.Strings;
import leap.lang.Types;
import leap.lang.convert.Converts;
import leap.orm.command.InsertCommand;
import leap.orm.command.UpdateCommand;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.mapping.RelationProperty;
import leap.orm.query.CriteriaQuery;
import leap.orm.query.PageResult;
import leap.web.Params;
import leap.web.api.annotation.ResourceWrapper;
import leap.web.api.config.ApiConfig;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.meta.model.MApiProperty;
import leap.web.api.mvc.params.Partial;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.query.Filters;
import leap.web.api.query.FiltersParser;
import leap.web.api.query.OrderBy;
import leap.web.api.query.OrderByParser;
import leap.web.exception.BadRequestException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The model class must be an orm model/entity class.
 */
@ResourceWrapper
public abstract class ModelController<T> extends ApiController implements ApiInitializable {

    protected final Class<T>      modelClass = getModelClass();
    protected final Dao           dao        = Dao.of(modelClass);
    protected final EntityMapping em         = dao.getOrmContext().getMetadata().getEntityMapping(modelClass);
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

        for(String name : properties.keySet()) {
            MApiProperty p = am.tryGetProperty(name);
            if(null == p) {
                throw new BadRequestException("Property '" + name + "' not exists!");
            }
            if(p.isNotCreatableExplicitly()) {
                throw new BadRequestException("Property '" + name + "' is not creatable!");
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

        insert.execute();

        if(null != id) {
            return id;
        }else{
            return insert.getGeneratedId();
        }
    }

    /**
     * Gets the record of the specified id.
     */
    protected ApiResponse get(Object id) {
        return ApiResponse.of(dao.findOrNull(em,id));
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
        //todo : validates the query options.

        CriteriaQuery<Record> query = dao.createCriteriaQuery(em);

        long count = -1;
        List<Record> list;
        if(null == options) {
            list = query.limit(apiConfig.getMaxPageSize()).list();
        }else{
            if(!Strings.isEmpty(options.getOrderBy())) {
                applyOrderBy(query, options.getOrderBy());
            }

            applyFilters(query, options.getParams(), options.getFilters(), filters);

            PageResult result = query.pageResult(options.getPage(apiConfig.getDefaultPageSize()));

            list = result.list();

            if(options.isTotal()) {
                count = result.getTotalCount();
            }
        }

        if(count == -1) {
            return ApiResponse.of(list);
        }else{
            return ApiResponse.of(list).setHeader("X-Total-Count", String.valueOf(count));
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

    private void applyOrderBy(CriteriaQuery query, String expr) {
        OrderBy orderBy = OrderByParser.parse(expr);

        OrderBy.Item[] items = orderBy.items();

        StringBuilder s = new StringBuilder();

        for(int i=0;i<items.length;i++) {

            if(i > 0) {
                s.append(',');
            }

            OrderBy.Item item = items[i];

            String name = item.name();

            MApiProperty ap = am.tryGetProperty(name);
            if(null == ap) {
                throw new BadRequestException("Property '" + name + "' not exists in model '" + am.getName() + "'");
            }

            if(ap.isNotSortableExplicitly()) {
                throw new BadRequestException("Property '" + name + "' is not sortable!");
            }

            s.append(name);

            if(!item.isAscending()) {
                s.append(" desc");
            }
        }

        query.orderBy(s.toString());
    }

    private void applyFilters(CriteriaQuery query, Params params, String expr, Map<String, Object> fields) {
        StringBuilder where = new StringBuilder();
        List<Object> args = new ArrayList<>();

        //fields
        if(null != fields && !fields.isEmpty()) {
            int i=0;
            for(Map.Entry<String,Object> entry : fields.entrySet()) {
                if(i > 0) {
                    where.append(" and ");
                }
                i++;
                where.append(entry.getKey()).append(" = ?");
                args.add(entry.getValue());
            }
        }

        for (String name : params.names()) {

            MApiProperty ap = am.tryGetProperty(name);
            if (null != ap) {

                if (ap.isNotFilterableExplicitly()) {
                    throw new BadRequestException("Property '" + name + "' is not filterable!");
                }

                if(!args.isEmpty()) {
                    where.append(" and ");
                }

                FieldMapping fm = em.tryGetFieldMapping(name);
                if (null != fm) {
                    where.append(name).append(" = ?");
                    args.add(Converts.convert(params.get(name), fm.getJavaType()));
                    continue;
                }

                //todo :
                RelationProperty rp = em.tryGetRelationProperty(name);
                if (null != rp) {
                    if(rp.isOne()) {
                        //many-to-one

                    }else{

                    }

                    continue;
                }

                throw new IllegalStateException("No field or relational property '" + name + "' in entity '" + em.getEntityName() + "'");
            }
        }

        //expr
        if(!Strings.isEmpty(expr)) {
            Filters filters = FiltersParser.parse(expr);

            FiltersParser.Node[] nodes = filters.nodes();
            if(nodes.length > 0) {

                boolean and = !args.isEmpty();
                if(and) {
                    where.append(" and (");
                }

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

                    if(ap.isNotFilterableExplicitly()) {
                        throw new BadRequestException("Property '" + name + "' is not filterable!");
                    }

                    FieldMapping fm = em.getFieldMapping(name);

                    where.append(name).append(' ').append(toSqlOperator(op)).append(" ?");
                    args.add(Converts.convert(value, fm.getJavaType()));
                }

                if(and) {
                    where.append(")");
                }
            }
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