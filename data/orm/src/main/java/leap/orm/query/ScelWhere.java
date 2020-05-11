/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.orm.query;

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

import java.util.List;

public class ScelWhere {

    private final ScelExpr   filters;
    private final ScelNode[] nodes;

    public ScelWhere(ScelExpr filters) {
        this.filters = filters;
        this.nodes = filters.nodes();
    }

    public <T> CriteriaQuery<T> apply(CriteriaQuery<T> query) {
        final EntityMapping      em    = query.getEntityMapping();
        final SimpleWhereBuilder where = new SimpleWhereBuilder();
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

                ScelName nameNode    = (ScelName) nodes[i];
                String   name        = nameNode.literal();
                String   filtersExpr = em.getFiltersExprs().get(name);
                if (!Strings.isEmpty(filtersExpr)) {
                    expr.append(filtersExpr);
                } else {
                    String    alias = nameNode.alias();
                    ScelToken op    = nodes[++i].token();
                    String    value = nodes[++i].literal();

                    if (null == op && Strings.isEmpty(value)) {
                        throw new IllegalStateException("Invalid filter expr in '" + name + "'");
                    }

                    final FieldMapping field = em.getFieldMapping(name);

                    String sqlOperator = toSqlOperator(op);

                    if (op == ScelToken.IS || op == ScelToken.IS_NOT) {
                        expr.append(alias).append('.').append(name).append(' ').append(sqlOperator);
                        continue;
                    }

                    if (op == ScelToken.IN || op == ScelToken.NOT_IN) {
                        applyFieldFilterIn(expr, alias, field, nodes[i].values(), sqlOperator);
                    } else {
                        String  envExpr = resolveEnvExpr(value);
                        boolean isEnv   = null != envExpr;
                        if (isEnv) {
                            value = envExpr;
                        }

                        if (Strings.equalsIgnoreCase(sqlOperator, ScelToken.LIKE.name())) {
                            if (op == ScelToken.SW) {
                                value = splicingValueOrExpr(isEnv, "%", value);
                            } else if (op == ScelToken.EW) {
                                value = splicingValueOrExpr(isEnv, value, "%");
                            } else if (op == ScelToken.CO) {
                                value = splicingValueOrExpr(isEnv, "%", value, "%");
                            }

                            if (!isEnv) {
                                applyFieldFilterOrArg(expr, alias, field, sqlOperator, "?", value);
                                continue;
                            }
                        } else if (!isEnv) {
                            applyFieldFilter(expr, alias, field, value, sqlOperator);
                            continue;
                        }
                        value = "#{" + value + "}";
                        applyFieldFilterOrArg(expr, alias, field, sqlOperator, value, null);
                    }
                }
            }
        });

        query.where(where.getWhere().toString(), where.getArgs().toArray());
        return query;
    }

    protected void applyFieldFilter(WhereBuilder.Expr expr, String alias, FieldMapping fm, Object value, String op) {
        applyFieldFilterOrArg(expr, alias, fm, op, "?", Converts.convert(value, fm.getJavaType()));
    }

    protected void applyFieldFilterIn(WhereBuilder.Expr expr, String alias, FieldMapping fm, List<ScelNode> values, String sqlOperator) {
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
        applyFieldFilterOrArg(expr, alias, fm, sqlOperator, "?", args);
    }

    protected void applyFieldFilterOrArg(WhereBuilder.Expr expr, String alias, FieldMapping fm, String op, String filterExpr, Object arg) {
        expr.append(alias).append('.').append(fm.getFieldName()).append(' ').append(op).append(" ").append(filterExpr);
        if (null != arg) {
            expr.arg(arg);
        }
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

    protected String resolveEnvExpr(String value) {
        if (value.endsWith("()") && value.length() > 2) {
            String envName = value.substring(0, value.length() - 2);
            //todo: check env is valid or allowed?
            return "env." + envName;
        } else if (value.startsWith("env.")) {
            //todo: check env is valid or allowed?
            return value;
        }
        return null;
    }

    protected String splicingValueOrExpr(boolean isEnv, String... items) {
        StringBuilder expr = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            if (isEnv) {
                if (i > 0) {
                    expr.append("+");
                }
                if (item.startsWith("env.")) {
                    expr.append(item);
                } else {
                    expr.append("'").append(item).append("'");
                }
                continue;
            }
            expr.append(item);
        }
        return expr.toString();
    }
}