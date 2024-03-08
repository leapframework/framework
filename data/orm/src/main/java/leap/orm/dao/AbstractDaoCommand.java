/*
 *
 *  * Copyright 2019 the original author or authors.
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

package leap.orm.dao;

import leap.core.jdbc.JdbcExecutor;
import leap.core.jdbc.ResultSetReader;
import leap.core.value.Record;
import leap.lang.params.ArrayParams;
import leap.lang.params.MapParams;
import leap.lang.params.Params;
import leap.lang.value.Limit;
import leap.orm.OrmContext;
import leap.orm.mapping.EntityMapping;
import leap.orm.query.Query;
import leap.orm.query.QueryContext;
import leap.orm.sql.Sql;
import leap.orm.sql.SqlCommand;
import leap.orm.sql.SqlContext;

import java.util.Map;

public abstract class AbstractDaoCommand implements DaoCommand {

    @Override
    public int executeUpdate() {
        return sql().executeUpdate(new Context(dao()), null);
    }

    @Override
    public int executeUpdate(Object[] args) {
        return sql().executeUpdate(new Context(dao()), params(args));
    }

    @Override
    public int executeUpdate(Map<String, Object> map) {
        return sql().executeUpdate(new Context(dao()), params(map));
    }

    @Override
    public int executeUpdate(Params params) {
        return sql().executeUpdate(new Context(dao()), params);
    }

    @Override
    public <T> T executeQuery(ResultSetReader<T> reader) {
        return sql().executeQuery(new Context(dao()), null, reader);
    }

    @Override
    public <T> T executeQuery(ResultSetReader<T> reader, Object[] args) {
        return sql().executeQuery(new Context(dao()), args, reader);
    }

    @Override
    public <T> T executeQuery(ResultSetReader<T> reader, Map<String, Object> map) {
        return sql().executeQuery(new Context(dao()), params(map), reader);
    }

    @Override
    public <T> T executeQuery(ResultSetReader<T> reader, Params params) {
        return sql().executeQuery(new Context(dao()), params, reader);
    }

    @Override
    public <T> Query<T> createQuery(Class<T> resultClass) {
        return dao().createQuery(resultClass, sql());
    }

    @Override
    public <T> Query<T> createQuery(Class<T> resultClass, Object[] args) {
        return dao().createQuery(resultClass, sql()).params(args);
    }

    @Override
    public <T> Query<T> createQuery(Class<T> resultClass, Map<String, Object> params) {
        return dao().createQuery(resultClass, sql()).params(params);
    }

    @Override
    public <T> Query<T> createQuery(Class<T> resultClass, Params params) {
        return dao().createQuery(resultClass, sql()).params(params);
    }

    @Override
    public Query<Record> createQuery() {
        return dao().createQuery(sql());
    }

    @Override
    public Query<Record> createQuery(Object[] args) {
        return dao().createQuery(sql()).params(args);
    }

    @Override
    public Query<Record> createQuery(Map<String, Object> params) {
        return dao().createQuery(sql()).params(params);
    }

    @Override
    public Query<Record> createQuery(Params params) {
        return dao().createQuery(sql()).params(params);
    }

    protected Params params(Object[] args) {
        return null == args ? null : new ArrayParams(args);
    }

    protected Params params(Map<String, Object> map) {
        return null == map ? null : new MapParams(map);
    }

    protected abstract SqlCommand sql();

    protected static class Context implements SqlContext, QueryContext {

        private final Dao dao;
        private       Sql sql;

        protected Context(Dao dao) {
            this.dao = dao;
        }

        @Override
        public Limit getLimit() {
            return null;
        }

        @Override
        public String getOrderBy() {
            return null;
        }

        @Override
        public String getGroupBy() {
            return null;
        }

        @Override
        public EntityMapping getPrimaryEntityMapping() {
            return null;
        }

        @Override
        public Sql getQuerySql() {
            return sql;
        }

        @Override
        public void setQuerySql(Sql sql) {
            this.sql = sql;
        }

        @Override
        public OrmContext getOrmContext() {
            return dao.getOrmContext();
        }

        @Override
        public JdbcExecutor getJdbcExecutor() {
            return dao.getJdbcExecutor();
        }
    }
}
