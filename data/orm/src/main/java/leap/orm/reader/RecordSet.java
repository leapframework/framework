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

package leap.orm.reader;

import leap.core.value.Record;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A {@link java.sql.ResultSet} replacement for wrapping the result row as {@link Record}.
 */
public interface RecordSet {

    /**
     * Returns the wrapped {@link ResultSet}.
     */
    ResultSet getResultSet();

    /**
     * Same as {@link ResultSet#next()}.
     */
    boolean next() throws SQLException;

    /**
     * Returns current record.
     */
    Record record() throws SQLException;

}