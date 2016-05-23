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
import leap.orm.model.ModelRegistry;
import leap.orm.query.Query;
import leap.orm.query.QueryContext;
import leap.orm.sql.SqlCommand;
import leap.orm.sql.SqlContext;

import java.util.Map;

public class SimpleDaoCommand implements DaoCommand,SqlContext,QueryContext {

    private final Dao        dao_;
    private final SqlCommand cmd;

    public SimpleDaoCommand(Dao dao, SqlCommand cmd) {
        this.dao_ = dao;
        this.cmd = cmd;
    }

    @Override
    public OrmContext getOrmContext() {
        return dao().getOrmContext();
    }

    @Override
    public JdbcExecutor getJdbcExecutor() {
        return dao().getJdbcExecutor();
    }

    @Override
    public final EntityMapping getPrimaryEntityMapping() {
        return null;
    }

    @Override
    public final Limit getLimit() {
        return null;
    }

    @Override
    public final String getOrderBy() {
        return null;
    }

    @Override
    public Dao dao() {
        OrmContext tlOrmContext = ModelRegistry.getThreadLocalContext();

        if(null != tlOrmContext){
            return tlOrmContext.getAppContext().getBeanFactory().getBean(Dao.class,tlOrmContext.getName());
        }

        return dao_;
    }

    @Override
    public boolean isQuery() {
        return cmd.isQuery();
    }

    @Override
    public int executeUpdate() {
        return cmd.executeUpdate(this,null);
    }

    @Override
    public int executeUpdate(Object[] args) {
        return cmd.executeUpdate(this, params(args));
    }

    @Override
    public int executeUpdate(Map<String, Object> map) {
        return cmd.executeUpdate(this, params(map));
    }

    @Override
    public int executeUpdate(Params params) {
        return cmd.executeUpdate(this, params);
    }

    @Override
    public <T> T executeQuery(ResultSetReader<T> reader) {
        return cmd.executeQuery(this, null, reader);
    }

    @Override
    public <T> T executeQuery(ResultSetReader<T> reader, Object[] args) {
        return cmd.executeQuery(this, args, reader);
    }

    @Override
    public <T> T executeQuery(ResultSetReader<T> reader, Map<String, Object> map) {
        return cmd.executeQuery(this, params(map), reader);
    }

    @Override
    public <T> T executeQuery(ResultSetReader<T> reader, Params params) {
        return cmd.executeQuery(this, params, reader);
    }

    @Override
    public <T> Query<T> createQuery(Class<T> resultClass) {
        return dao().createQuery(resultClass, cmd);
    }

    @Override
    public <T> Query<T> createQuery(Class<T> resultClass, Object[] args) {
        return dao().createQuery(resultClass, cmd).params(args);
    }

    @Override
    public <T> Query<T> createQuery(Class<T> resultClass, Map<String, Object> params) {
        return dao().createQuery(resultClass, cmd).params(params);
    }

    @Override
    public <T> Query<T> createQuery(Class<T> resultClass, Params params) {
        return dao().createQuery(resultClass, cmd).params(params);
    }

    @Override
    public Query<Record> createQuery() {
        return dao().createQuery(cmd);
    }

    @Override
    public Query<Record> createQuery(Object[] args) {
        return dao().createQuery(cmd).params(args);
    }

    @Override
    public Query<Record> createQuery(Map<String, Object> params) {
        return dao().createQuery(cmd).params(params);
    }

    @Override
    public Query<Record> createQuery(Params params) {
        return dao().createQuery(cmd).params(params);
    }

    protected Params params(Object[] args) {
        return null == args ? null : new ArrayParams(args);
    }

    protected Params params(Map<String,Object> map) {
        return null == map ? null : new MapParams(map);
    }
}
