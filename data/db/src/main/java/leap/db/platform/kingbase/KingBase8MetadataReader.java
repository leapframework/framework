/*
 * Copyright 2021 the original author or authors.
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
package leap.db.platform.kingbase;

import leap.db.platform.GenericDbMetadataReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class KingBase8MetadataReader extends GenericDbMetadataReader {

    public KingBase8MetadataReader() {

    }

    /**
     * ct.relname = '%' -> ct.relname LIKE '%'
     */
    @Override
    protected ResultSet getPrimaryKeys(Connection connection, DatabaseMetaData dm, MetadataParameters params) throws SQLException {
        String sql = "SELECT NULL AS TABLE_CAT, n.nspname AS TABLE_SCHEM, ct.relname AS TABLE_NAME, " +
                "a.attname AS COLUMN_NAME, (i.keys).n AS KEY_SEQ, ci.relname AS PK_NAME " +
                "FROM pg_catalog.pg_class ct " +
                "JOIN pg_catalog.pg_attribute a ON (ct.oid = a.attrelid) " +
                "JOIN pg_catalog.pg_namespace n ON (ct.relnamespace = n.oid) " +
                "JOIN (SELECT i.indexrelid, i.indrelid, i.indisprimary, information_schema._pg_expandarray(i.indkey) AS keys FROM pg_catalog.pg_index i) i ON (a.attnum = (i.keys).x AND a.attrelid = i.indrelid) " +
                "JOIN pg_catalog.pg_class ci ON (ci.oid = i.indexrelid) " +
                "WHERE true AND n.nspname = ? AND ct.relname LIKE '%' AND i.indisprimary  ORDER BY table_name, pk_name, key_seq";
        return executeSchemaQuery(connection, params, sql);
    }
}