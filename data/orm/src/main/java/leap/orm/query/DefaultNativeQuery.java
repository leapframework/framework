/*
 *  Copyright 2019 the original author or authors.
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

package leap.orm.query;

import leap.core.exception.EmptyRecordsException;
import leap.core.exception.TooManyRecordsException;
import leap.core.jdbc.JdbcExecutor;
import leap.core.jdbc.ResultSetReader;
import leap.core.jdbc.SimpleScalarReader;
import leap.core.jdbc.SimpleScalarsReader;
import leap.core.value.Scalar;
import leap.core.value.Scalars;
import leap.lang.Arrays2;
import leap.lang.value.Limit;
import leap.orm.OrmContext;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.orm.reader.ResultSetReaders;
import leap.orm.sql.Sql;
import leap.orm.sql.SqlCommand;

import java.util.List;

public class DefaultNativeQuery<T> implements NativeQuery<T>, QueryContext {

    private final Dao        dao;
    private final SqlCommand sqlCommand;
    private final Class<T>   targetType;
    private final Class<T>   resultClass;

    private Sql querySql;

    public DefaultNativeQuery(Dao dao, SqlCommand sqlCommand, Class<T> targetType) {
        this(dao, sqlCommand, targetType, targetType);
    }

    public DefaultNativeQuery(Dao dao, SqlCommand sqlCommand, Class<T> targetType, Class<T> resultClass) {
        this.dao = dao;
        this.sqlCommand = sqlCommand;
        this.targetType = targetType;
        this.resultClass = resultClass;
    }

    protected Object[] args = Arrays2.EMPTY_OBJECT_ARRAY;

    @Override
    public NativeQuery<T> args(Object[] args) {
        this.args = args;
        return this;
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
    public Sql getQuerySql() {
        return querySql;
    }

    @Override
    public void setQuerySql(Sql sql) {
        this.querySql = sql;
    }

    @Override
    public OrmContext getOrmContext() {
        return dao.getOrmContext();
    }

    @Override
    public JdbcExecutor getJdbcExecutor() {
        return dao.getJdbcExecutor();
    }

    @Override
    public EntityMapping getPrimaryEntityMapping() {
        return null;
    }

    @Override
    public T first() throws EmptyRecordsException {
        return result().first();
    }

    @Override
    public T firstOrNull() {
        return result().firstOrNull();
    }

    @Override
    public T single() throws EmptyRecordsException, TooManyRecordsException {
        return result().single();
    }

    @Override
    public T singleOrNull() throws TooManyRecordsException {
        return result().singleOrNull();
    }

    @Override
    public List<T> list() {
        return result().list();
    }

    @Override
    public Scalar scalar() throws EmptyRecordsException, TooManyRecordsException {
        Scalar scalar = scalarOrNull();

        if (null == scalar) {
            throw new EmptyRecordsException("No records, cannot return scalar value");
        }

        return scalar;
    }

    @Override
    public QueryResult<T> result() {
        ResultSetReader<List<T>> reader =
                ResultSetReaders.forNativeListRow(dao.getOrmContext(), targetType, resultClass, sqlCommand);

        return new DefaultQueryResult<T>(sqlCommand.toString(), sqlCommand.executeQuery(this, args, reader));
    }

    @Override
    public Scalar scalarOrNull() throws TooManyRecordsException {
        return sqlCommand.executeQuery(this, args, SimpleScalarReader.DEFAULT_INSTANCE);
    }

    @Override
    public Scalars scalars() {
        return sqlCommand.executeQuery(this, args, SimpleScalarsReader.DEFAULT_INSTANCE);
    }
}