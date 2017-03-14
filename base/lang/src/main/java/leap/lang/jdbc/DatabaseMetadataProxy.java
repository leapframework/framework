/*
 *
 *  * Copyright 2016 the original author or authors.
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

package leap.lang.jdbc;

import java.sql.*;

public class DatabaseMetadataProxy<T extends ConnectionProxy> extends DatabaseMetaDataWrapper {

    protected final T conn;

    public DatabaseMetadataProxy(T conn, DatabaseMetaData md) {
        super(md);
        this.conn = conn;
    }

    @Override
    public final Connection getConnection() throws SQLException {
        return conn;
    }

    @Override
    public final ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException {
        return proxyOfResultSet(md.getProcedures(catalog, schemaPattern, procedureNamePattern));
    }

    @Override
    public final ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern)
            throws SQLException {
        return proxyOfResultSet(md.getProcedureColumns(catalog, schemaPattern, procedureNamePattern, columnNamePattern));
    }

    @Override
    public final ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
        return proxyOfResultSet(md.getTables(catalog, schemaPattern, tableNamePattern, types));
    }

    @Override
    public final ResultSet getSchemas() throws SQLException {
        return proxyOfResultSet(md.getSchemas());
    }

    @Override
    public final ResultSet getCatalogs() throws SQLException {
        return proxyOfResultSet(md.getCatalogs());
    }

    @Override
    public final ResultSet getTableTypes() throws SQLException {
        return proxyOfResultSet(md.getTableTypes());
    }

    @Override
    public final ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        return proxyOfResultSet(md.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern));
    }

    @Override
    public final ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException {
        return proxyOfResultSet(md.getColumnPrivileges(catalog, schema, table, columnNamePattern));
    }

    @Override
    public final ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        return proxyOfResultSet(md.getTablePrivileges(catalog, schemaPattern, tableNamePattern));
    }

    @Override
    public final ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException {
        return proxyOfResultSet(md.getBestRowIdentifier(catalog, schema, table, scope, nullable));
    }

    @Override
    public final ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
        return proxyOfResultSet(md.getVersionColumns(catalog, schema, table));
    }

    @Override
    public final ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
        return proxyOfResultSet(md.getPrimaryKeys(catalog, schema, table));
    }

    @Override
    public final ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
        return proxyOfResultSet(md.getImportedKeys(catalog, schema, table));
    }

    @Override
    public final ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
        return proxyOfResultSet(md.getExportedKeys(catalog, schema, table));
    }

    @Override
    public final ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable, String foreignCatalog, String foreignSchema,
                                       String foreignTable) throws SQLException {
        return proxyOfResultSet(md.getCrossReference(parentCatalog, parentSchema, parentTable, foreignCatalog, foreignSchema, foreignTable));
    }

    @Override
    public final ResultSet getTypeInfo() throws SQLException {
        return proxyOfResultSet(md.getTypeInfo());
    }

    @Override
    public final ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException {
        return proxyOfResultSet(md.getIndexInfo(catalog, schema, table, unique, approximate));
    }

    @Override
    public final ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException {
        return proxyOfResultSet(md.getUDTs(catalog, schemaPattern, typeNamePattern, types));
    }

    @Override
    public final ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException {
        return proxyOfResultSet(md.getSuperTypes(catalog, schemaPattern, typeNamePattern));
    }

    @Override
    public final ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        return proxyOfResultSet(md.getSuperTables(catalog, schemaPattern, tableNamePattern));
    }

    @Override
    public final ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) throws SQLException {
        return proxyOfResultSet(md.getAttributes(catalog, schemaPattern, typeNamePattern, attributeNamePattern));
    }

    @Override
    public final ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
        return proxyOfResultSet(md.getSchemas(catalog,schemaPattern));
    }

    @Override
    public final ResultSet getClientInfoProperties() throws SQLException {
        return proxyOfResultSet(md.getClientInfoProperties());
    }

    @Override
    public final ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException {
        return proxyOfResultSet(md.getFunctions(catalog, schemaPattern, functionNamePattern));
    }

    @Override
    public final ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern)
            throws SQLException {
        return proxyOfResultSet(md.getFunctionColumns(catalog, schemaPattern, functionNamePattern, columnNamePattern));
    }

    @Override
    public final ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        return proxyOfResultSet(md.getPseudoColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern));
    }

    protected ResultSet proxyOfResultSet(ResultSet rs) throws SQLException {
        //don't proxy the statement from result set, some jdbc driver will cache the statement, such as sql server.
        return new ResultSetProxy(rs.getStatement(), rs);
    }
}
