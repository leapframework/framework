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

import leap.lang.text.scel.ScelExpr;
import leap.orm.mapping.EntityMapping;
import leap.web.api.query.FiltersParser;
import leap.web.exception.BadRequestException;

import java.util.List;

public interface ModelExecutorHelper {

    class QueryContext {
        private final EntityMapping entity;
        private final String        alias;

        public QueryContext(EntityMapping entity, String alias) {
            this.entity = entity;
            this.alias = alias;
        }

        public EntityMapping getEntity() {
            return entity;
        }

        public String getAlias() {
            return alias;
        }
    }

    class SQLExpr {
        protected final String       sql;
        protected final List<Object> args;

        public SQLExpr(String sql, List<Object> args) {
            this.sql = sql;
            this.args = args;
        }

        public String getSql() {
            return sql;
        }

        public List<Object> getArgs() {
            return args;
        }

        public boolean isEmpty() {
            return null == sql || sql.length() == 0;
        }
    }

    /**
     * Converts the filters expr to Standard SQL expr.
     *
     * @throws BadRequestException if the given filters is invalid.
     */
    default SQLExpr toSQLExpr(EntityMapping entity, String alias, String filters) throws BadRequestException {
        return toSQLExpr(new QueryContext(entity, alias), FiltersParser.parse(filters));
    }

    /**
     * Converts the filters expr to Standard SQL expr.
     *
     * @throws BadRequestException if the given filters is invalid.
     */
    default SQLExpr toSQLExpr(EntityMapping entity, String alias, ScelExpr filters) throws BadRequestException {
        return toSQLExpr(new QueryContext(entity, alias), filters);
    }

    /**
     * Converts the filters expr to Standard SQL expr.
     *
     * @throws BadRequestException if the given filters is invalid.
     */
    default SQLExpr toSQLExpr(QueryContext context, String filters) throws BadRequestException {
        return toSQLExpr(context, FiltersParser.parse(filters));
    }

    /**
     * Converts the filters expr to Standard SQL expr.
     *
     * @throws BadRequestException if the given filters is invalid.
     */
    SQLExpr toSQLExpr(QueryContext context, ScelExpr filters) throws BadRequestException;
}