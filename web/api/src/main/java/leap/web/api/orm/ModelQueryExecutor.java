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

import leap.core.value.Record;
import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.orm.dao.Dao;
import leap.orm.mapping.*;
import leap.orm.query.CriteriaQuery;
import leap.orm.query.PageResult;
import leap.web.Params;
import leap.web.api.config.ApiConfig;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.meta.model.MApiProperty;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.mvc.params.QueryOptionsBase;
import leap.web.api.query.*;
import leap.web.exception.BadRequestException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModelQueryExecutor {

    protected final ApiConfig     c;
    protected final MApiModel     am;
    protected final Dao           dao;
    protected final EntityMapping em;

    public ModelQueryExecutor(ApiConfig c, MApiModel am, Dao dao, EntityMapping em) {
        this.c = c;
        this.am = am;
        this.dao = dao;
        this.em = em;
    }

    public QueryOneResult queryOne(Object id, QueryOptionsBase options) {
        if(null == options) {
            return new QueryOneResult(dao.findOrNull(em, id));
        }




        return null;
    }

    public QueryListResult queryList(QueryOptions options, Map<String, Object> filters) {
        //todo : validates the query options.

        CriteriaQuery<Record> query = dao.createCriteriaQuery(em);

        long count = -1;
        List<Record> list;
        if(null == options) {
            list = query.limit(c.getMaxPageSize()).list();
        }else{
            if(!Strings.isEmpty(options.getOrderBy())) {
                applyOrderBy(query, options.getOrderBy());
            }

            if(!Strings.isEmpty(options.getSelect())) {
                applySelect(query, options.getSelect());
            }

            applyFilters(query, options.getParams(), options.getFilters(), filters);

            PageResult result = query.pageResult(options.getPage(c.getDefaultPageSize()));

            list = result.list();

            if(!list.isEmpty()) {
                Expand[] expands = ExpandParser.parse(options.getExpand());
                if(expands.length > 0) {

                    for(Record record : list) {

                        Object id = Mappings.getId(em, record);

                        for(Expand expand : expands) {
                            String name = expand.getName();

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

                            CriteriaQuery expandQuery =
                                    dao.createCriteriaQuery(rp.getTargetEntityName())
                                            .joinById(em.getEntityName(), rm.getInverseRelationName(), "t_" + em.getEntityName(), id);


                            if(!Strings.isEmpty(expand.getSelect())) {
                                applySelect(expandQuery, expand.getSelect());
                            }

                            if(rp.isMany()) {
                                //todo : limit
                                record.put(rp.getName(), expandQuery.list());
                            }else{
                                record.put(rp.getName(), expandQuery.firstOrNull());
                            }
                        }

                    }
                }
            }
        }

        return new QueryListResult(list, count);
    }

    protected void applyOrderBy(CriteriaQuery query, String expr) {
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

    protected void applySelect(CriteriaQuery query, String select) {
        if(Strings.equals("*", select)) {
            return;
        }

        EntityMapping em = query.getEntityMapping();

        String[] names = Strings.split(select, ',');

        List<String> fields = new ArrayList<>();

        for(String name : names) {

            FieldMapping p = em.tryGetFieldMapping(name);

            if(null == p) {
                throw new BadRequestException("Property '" + name + "' not exists, check the 'select' query param");
            }

            fields.add(p.getFieldName());
        }

        query.select(fields.toArray(new String[fields.size()]));
    }

    protected void applyFilters(CriteriaQuery query, Params params, String expr, Map<String, Object> fields) {
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
                where.append(query.alias()).append('.').append(entry.getKey()).append(" = ?");
                args.add(entry.getValue());
            }
        }

        int joins = 1;

        for (String name : params.names()) {

            MApiProperty ap = am.tryGetProperty(name);
            if (null != ap) {
                if (ap.isNotFilterableExplicitly()) {
                    throw new BadRequestException("Property '" + name + "' is not filterable!");
                }

                String value = params.get(name);
                if(Strings.isEmpty(value)) {
                    continue;
                }

                if(!args.isEmpty()) {
                    where.append(" and ");
                }

                FieldMapping fm = em.tryGetFieldMapping(name);
                if (null != fm) {
                    where.append(query.alias()).append('.').append(name).append(" = ?");
                    args.add(Converts.convert(value, fm.getJavaType()));
                    continue;
                }

                RelationProperty rp = em.tryGetRelationProperty(name);
                if (null != rp) {
                    String alias = "jn" + joins++;

                    query.joinWithWhere(rp.getTargetEntityName(), rp.getRelationName(), alias, where, (f) -> {

                        String[] a = params.getArray(name);
                        if(a.length == 1) {
                            a = Strings.split(a[0], ',');
                        }

                        args.add(Converts.convert(a, Array.newInstance(((FieldMapping)f).getJavaType(), 0).getClass()));

                    });

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

        if(where.length() > 0) {
            query.where(where.toString(), args.toArray());
        }
    }

    protected String toSqlOperator(FiltersParser.Token op) {

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
