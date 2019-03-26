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

import leap.core.jdbc.ResultSetReader;
import leap.core.value.Record;
import leap.lang.params.Params;
import leap.orm.query.Query;
import leap.orm.sql.SqlCommand;

import java.util.Map;

/**
 * Wraps a sql statement and the {@link Dao} object for executing the sql.
 */
public interface DaoCommand {

    /**
     * Is the command exists?
     */
    default boolean exists() {
        return true;
    }

    /**
     * Returns the {@link Dao} for executing this command.
     */
    Dao dao();

    /**
     * Executes sql update without any params.
     *
     * <p/>
     * Returns the affected row(s).
     *
     * @see java.sql.Statement#executeUpdate(String)
     */
    int executeUpdate();

    /**
     * Executes sql update with the given indexed args (for jdbc placeholder '?' parameters).
     *
     * <p/>
     * Returns the affected row(s).
     *
     * @see java.sql.Statement#executeUpdate(String)
     */
    int executeUpdate(Object[] args);

    /**
     * Executes sql update with the given named params (for named parameters ':param').
     *
     * <p/>
     * Returns the affected row(s).
     *
     * @see java.sql.Statement#executeUpdate(String)
     */
    int executeUpdate(Map<String, Object> params);

    /**
     * Executes sql update with the given {@link Params}.
     *
     * <p/>
     * Returns the affected row(s).
     *
     * @see java.sql.Statement#executeUpdate(String)
     */
    int executeUpdate(Params params);

    /**
     * Executes the query with the given {@link ResultSetReader}.
     *
     * <p/>
     * Returns the result returned by the given reader.
     */
    <T> T executeQuery(ResultSetReader<T> reader);

    /**
     * Executes the query with the given {@link ResultSetReader} and the given index args (for jdbc placeholder '?' parameters).
     *
     * <p/>
     * Returns the result returned by the given reader.
     */
    <T> T executeQuery(ResultSetReader<T> reader, Object[] args);

    /**
     * Executes the query with the given {@link ResultSetReader} and the named params (for named parameters ':param').
     *
     * <p/>
     * Returns the result returned by the given reader.
     */
    <T> T executeQuery(ResultSetReader<T> reader, Map<String, Object> params);

    /**
     * Executes the query with the given {@link ResultSetReader} and the {@link Params}.
     *
     * <p/>
     * Returns the result returned by the given reader.
     */
    <T> T executeQuery(ResultSetReader<T> reader, Params params);

    /**
     * Creates a new {@link Query} of this command.
     *
     * @see {@link Dao#createQuery(Class, SqlCommand)}.
     */
    <T> Query<T> createQuery(Class<T> resultClass);

    /**
     * Creates a new {@link Query} of this command with the given indexed args (from jdbc placeholder '?' parameters).
     *
     * @see {@link Dao#createQuery(Class, SqlCommand)}.
     */
    <T> Query<T> createQuery(Class<T> resultClass, Object[] args);

    /**
     * Creates a new {@link Query} of this command with the given named params (from named parameters ':param').
     *
     * @see {@link Dao#createQuery(Class, SqlCommand)}.
     */
    <T> Query<T> createQuery(Class<T> resultClass, Map<String, Object> params);

    /**
     * Creates a new {@link Query} of this command with the given {@link Params}.
     *
     * @see {@link Dao#createQuery(Class, SqlCommand)}.
     */
    <T> Query<T> createQuery(Class<T> resultClass, Params params);

    /**
     * Creates a new {@link Query} of this command.
     *
     * @see {@link Dao#createQuery(SqlCommand)}.
     */
    Query<Record> createQuery();

    /**
     * Creates a new {@link Query} of this command with the given indexed args (from jdbc placeholder '?' parameters)..
     *
     * @see {@link Dao#createQuery(SqlCommand)}.
     */
    Query<Record> createQuery(Object[] args);

    /**
     * Creates a new {@link Query} of this command with the given named params (from named parameters ':param').
     *
     * @see {@link Dao#createQuery(SqlCommand)}.
     */
    Query<Record> createQuery(Map<String,Object> params);

    /**
     * Creates a new {@link Query} of this command with the given {@link Params}.
     *
     * @see {@link Dao#createQuery(SqlCommand)}.
     */
    Query<Record> createQuery(Params params);
}