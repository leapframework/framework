/*
 * Copyright 2017 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.db.platform.sqlserver;

import leap.db.model.DbIndexBuilder;
import leap.db.model.DbTableBuilder;
import leap.db.platform.GenericDbMetadataReader;
import leap.lang.jdbc.ResultSetWrapper;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 12 means 2012
 */
public class SqlServer12MetadataReader extends GenericDbMetadataReader {

    @Override
    protected boolean supportsReadAllPrimaryKeys() {
        return false;
    }

    @Override
    protected boolean supportsReadAllForeignKeys() {
        return false;
    }

    @Override
    protected boolean supportsReadAllIndexes() {
        return false;
    }

    @Override
    protected MetadataParameters createMetadataParameters(Connection connection, DatabaseMetaData dm, String catalog, String schema) {
        MetadataParameters p = super.createMetadataParameters(connection, dm, catalog, schema);
        
        p.schema = "dbo";
        p.schemaPattern = "%";
        
        return p;
    }

    @Override
    protected ResultSet getColumns(Connection connection,DatabaseMetaData dm,MetadataParameters params) throws SQLException {
        return dm.getColumns(params.catalog, params.schema, params.tablePattern, getDefaultColumnPattern());
    }

    @Override
    protected ResultSet getPrimaryKeys(Connection connection, DatabaseMetaData dm, MetadataParameters params, String table) throws SQLException {
        return dm.getPrimaryKeys(params.catalog, params.schema, table);
    }

    @Override
    protected ResultSet getForeignKeys(Connection connection, DatabaseMetaData dm, MetadataParameters params, String table) throws SQLException {
        return dm.getImportedKeys(params.catalog, params.schema, table);
    }

    @Override
    protected ResultSet getIndexes(Connection connection,DatabaseMetaData dm,MetadataParameters params, String table) throws SQLException {
        return dm.getIndexInfo(params.catalog, params.schema, table, false, false);
    }

    @Override
    protected boolean isInternalIndex(DbTableBuilder table, DbIndexBuilder ix, ResultSet rs) throws SQLException {
        return ix.getName().startsWith("PK__") || ix.getName().startsWith("AK_");
    }
}