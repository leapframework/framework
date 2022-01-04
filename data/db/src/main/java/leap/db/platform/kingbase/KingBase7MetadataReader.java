/*
 * Copyright 2022 the original author or authors.
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

import leap.db.exception.DbSchemaException;
import leap.db.model.DbColumnBuilder;
import leap.db.model.DbTableBuilder;
import leap.db.platform.GenericDbMetadataReader;
import leap.lang.Strings;
import leap.lang.jdbc.JDBC;
import java.sql.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class KingBase7MetadataReader extends GenericDbMetadataReader {

    /**
     * ct.relname = '%' -> ct.relname like '%'
     */
    @Override
    protected ResultSet getPrimaryKeys(Connection connection, DatabaseMetaData dm, MetadataParameters params) throws SQLException {
        String sql = "SELECT NULL AS TABLE_CAT, n.nspname AS TABLE_SCHEM, ct.relname AS TABLE_NAME, " +
                "ia.attname AS COLUMN_NAME, ia.attnum AS KEY_SEQ, con.conname AS PK_NAME " +
                "from sys_catalog.sys_namespace n, sys_catalog.sys_class ct, sys_catalog.sys_attribute ia, " +
                "sys_catalog.sys_attribute ta, sys_catalog.sys_index i,sys_constraint con " +
                "WHERE ct.oid = i.indrelid AND i.indisprimary and con.contype = 'p' and ct.oid = con.conrelid " +
                "and ia.attrelid = i.indrelid and ta.attrelid = i.indrelid and ia.attnum = i.indkey[ta.attnum-1] " +
                "AND ct.relnamespace = n.oid and ct.relname like '%' and ct.relnamespace = n.oid and n.nspname = ?";
        return executeSchemaQuery(connection, params, sql);
    }

    @Override
    protected void readAllColumns(Connection connection, DatabaseMetaData dm, MetadataParameters params, List<DbTableBuilder> tables, AtomicInteger counter) throws SQLException {
        ResultSet rs = null;
        try {
            rs = getColumns(connection, dm, params);

            if (null != rs) {
                while (rs.next()) {
                    counter.incrementAndGet();

                    String schemaName = getColumnSchema(params, rs);
                    String tableName  = rs.getString(TABLE_NAME);

                    if (Strings.equalsIgnoreCase(params.schema, schemaName)) {
                        for (DbTableBuilder table : tables) {
                            if (Strings.equalsIgnoreCase(tableName, table.getName())) {
                                DbColumnBuilder column = new DbColumnBuilder();

                                if (readColumnProperties(table, column, rs)) {
                                    if (table.findColumn(column.getName()) != null) {
                                        throw new DbSchemaException(
                                                Strings.format("Found duplicate column '{0}' in table '{1}'",
                                                        column.getName(), table.getName()));
                                    }

                                    table.addColumn(column);
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            JDBC.closeResultSetOnly(rs);
        }
    }

}