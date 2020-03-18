/*
 *  Copyright 2020 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package leap.web.api.orm;

import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.lang.jdbc.SimpleWhereBuilder;
import leap.lang.jdbc.WhereBuilder;
import leap.lang.text.scel.ScelExpr;
import leap.lang.text.scel.ScelName;
import leap.lang.text.scel.ScelNode;
import leap.lang.text.scel.ScelToken;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.web.exception.BadRequestException;
import leap.web.exception.NotFoundException;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import java.lang.reflect.Array;
import java.util.List;

public class DefaultModelExecutorHelper implements ModelExecutorHelper {

    @Override
    public SQLExpr toSQLExpr(QueryContext context, ScelExpr filters) throws BadRequestException {
        final EntityMapping      em    = context.getEntity();
        final SimpleWhereBuilder where = new SimpleWhereBuilder();

        ScelNode[] nodes = filters.nodes();
        if (nodes.length > 0) {
            where.and((expr) -> {
                for (int i = 0; i < nodes.length; i++) {
                    ScelNode node = nodes[i];

                    if (node.isParen()) {
                        expr.append(node.literal());
                        continue;
                    }

                    if (node.isAnd()) {
                        expr.append(" and ");
                        continue;
                    }

                    if (node.isOr()) {
                        expr.append(" or ");
                        continue;
                    }

                    ScelName nameNode = (ScelName) nodes[i];

                    String name        = nameNode.literal();
                    String filtersExpr = em.getFiltersExprs().get(name);
                    if (!Strings.isEmpty(filtersExpr)) {
                        expr.append(filtersExpr);
                    } else {
                        FieldMapping field = lookupField(context, name);
                        if(null == field) {
                            throw new BadRequestException("Property '" + name + "' not exists in '" + context.getEntity().getEntityName() + "'");
                        }
                        checkProperty(context, field);

                        String    alias = nameNode.alias();
                        ScelToken op    = nodes[++i].token();
                        String    value = nodes[++i].literal();

                        if (null == op && Strings.isEmpty(value)) {
                            throw new BadRequestException("Invalid filter expr in '" + name + "'");
                        }

                        if (null != alias) {
                            throw new BadRequestException("alias does not supported without joins");
                            //                            if (null == jms || !jms.contains(alias)) {
                            //                                throw new BadRequestException("Unknown alias '" + alias + "' at property '" + nameNode.toString() + "'");
                            //                            }
                        } else {
                            alias = context.getAlias();
                        }


                        String sqlOperator = toSqlOperator(op);

                        if (op == ScelToken.IS || op == ScelToken.IS_NOT) {
                            expr.append(alias).append('.').append(name).append(' ').append(sqlOperator);
                            continue;
                        }

                        if (op == ScelToken.SW) {
                            value = "%" + value;
                        } else if (op == ScelToken.EW) {
                            value = value + "%";
                        } else if (op == ScelToken.CO) {
                            value = "%" + value + "%";
                        }

                        //env
                        if (op == ScelToken.IN || op == ScelToken.NOT_IN) {
                            applyFieldFilterIn(expr, alias, field, nodes[i].values(), sqlOperator);
                        } else if (value.endsWith("()") && value.length() > 2) {
                            String envName = value.substring(0, value.length() - 2);
                            //todo: check env is valid or allowed?
                            String valueExpr = "#{env." + envName + "}";
                            applyFieldFilterExpr(expr, alias, field, valueExpr, sqlOperator);
                        } else if (value.startsWith("env.")) {
                            //todo: check env is valid or allowed?
                            String valueExpr = "#{" + value + "}";
                            applyFieldFilterExpr(expr, alias, field, valueExpr, sqlOperator);
                        } else {
                            applyFieldFilter(expr, alias, field, value, sqlOperator);
                        }
                    }
                }
            });
        }

        return new SQLExpr(where.getWhere().toString(), where.getArgs());
    }

    protected FieldMapping lookupField(QueryContext context, String propertyName) {
        return context.getEntity().tryGetFieldMapping(propertyName);
    }

    protected void checkProperty(QueryContext context, FieldMapping field) {
        if (!field.isFilterable()) {
            throw new BadRequestException("Property '" + field.getFieldName() + "' is not filterable in " + context.getEntity().getEntityName());
        }
    }

    protected void applyFieldFilter(WhereBuilder.Expr expr, String alias, FieldMapping fm, Object value, String op) {
        expr.append(alias).append('.').append(fm.getFieldName()).append(' ').append(op).append(" ?");
        expr.arg(Converts.convert(value, fm.getJavaType()));
    }

    protected void applyFieldFilterExpr(WhereBuilder.Expr expr, String alias, FieldMapping fm, String filterExpr, String op) {
        expr.append(alias).append('.').append(fm.getFieldName()).append(' ').append(op).append(" ").append(filterExpr);
    }

    protected void applyFieldFilterIn(WhereBuilder.Expr expr, String alias, FieldMapping fm, String[] values) {
        expr.append(alias).append('.').append(fm.getFieldName()).append(' ').append("in").append(" ?");
        expr.arg(Converts.convert(values, Array.newInstance(((FieldMapping) fm).getJavaType(), 0).getClass()));
    }

    protected void applyFieldFilterIn(WhereBuilder.Expr expr, String alias, FieldMapping fm, List<ScelNode> values, String sqlOperator) {
        expr.append(alias).append('.').append(fm.getFieldName()).append(' ').append(sqlOperator).append(" ?");

        final Class<?> type = ((FieldMapping) fm).getJavaType();

        Object[] args = new Object[values.size()];
        for (int i = 0; i < args.length; i++) {
            ScelNode value = values.get(i);
            if (ScelToken.NULL == value.token()) {
                args[i] = null;
            } else {
                args[i] = Converts.convert(value.literal(), type);
            }
        }

        expr.arg(args);
    }

    protected String toSqlOperator(ScelToken op) {

        if (op == ScelToken.EQ) {
            return "=";
        }

        if (op == ScelToken.GE) {
            return ">=";
        }

        if (op == ScelToken.LE) {
            return "<=";
        }

        if (op == ScelToken.GT) {
            return ">";
        }

        if (op == ScelToken.LT) {
            return "<";
        }

        if (op == ScelToken.NE) {
            return "<>";
        }

        if (op == ScelToken.NOT) {
            return "not";
        }

        if (op == ScelToken.IN) {
            return "in";
        }

        if (op == ScelToken.NOT_IN) {
            return "not in";
        }

        if (op == ScelToken.LIKE || op == ScelToken.CO || op == ScelToken.SW || op == ScelToken.EW) {
            return "like";
        }

        if (op == ScelToken.IS) {
            return "is null";
        }

        if (op == ScelToken.IS_NOT || op == ScelToken.PR) {
            return "is not null";
        }

        throw new IllegalStateException("Not supported operator '" + op + "'");
    }
}