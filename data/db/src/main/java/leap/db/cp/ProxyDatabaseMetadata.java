/*
 * Copyright 2015 the original author or authors.
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
package leap.db.cp;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import leap.lang.jdbc.DatabaseMetaDataWrapper;

public class ProxyDatabaseMetadata extends DatabaseMetaDataWrapper {

	private final PooledConnection conn;
	
	ProxyDatabaseMetadata(PooledConnection conn, DatabaseMetaData md) {
		super(md);
		this.conn = conn;
	}

	@Override
    public Connection getConnection() throws SQLException {
		return conn;
	}
	
	protected ResultSet proxy(ResultSet rs) throws SQLException {
		Statement stmt = rs.getStatement();
		if(null == stmt){
			return new ProxyResultSet(null, rs);
		}else{
			return new ProxyResultSet(conn.createProxy(stmt), rs); 
		}
	}
	
	@Override
	public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException {
		return proxy(md.getProcedures(catalog, schemaPattern, procedureNamePattern));
	}

	@Override
	public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern)
	        throws SQLException {
		return proxy(md.getProcedureColumns(catalog, schemaPattern, procedureNamePattern, columnNamePattern));
	}

	@Override
	public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
		return proxy(md.getTables(catalog, schemaPattern, tableNamePattern, types));
	}

	@Override
	public ResultSet getSchemas() throws SQLException {
		return proxy(md.getSchemas());
	}

	@Override
	public ResultSet getCatalogs() throws SQLException {
		return proxy(md.getCatalogs());
	}

	@Override
	public ResultSet getTableTypes() throws SQLException {
		return proxy(md.getTableTypes());
	}

	@Override
	public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
		return proxy(md.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern));
	}

	@Override
	public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException {
		return proxy(md.getColumnPrivileges(catalog, schema, table, columnNamePattern));
	}

	@Override
	public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
		return proxy(md.getTablePrivileges(catalog, schemaPattern, tableNamePattern));
	}

	@Override
	public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException {
		return proxy(md.getBestRowIdentifier(catalog, schema, table, scope, nullable));
	}

	@Override
	public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
		return proxy(md.getVersionColumns(catalog, schema, table));
	}

	@Override
	public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
		return proxy(md.getPrimaryKeys(catalog, schema, table));
	}

	@Override
	public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
		return proxy(md.getImportedKeys(catalog, schema, table));
	}

	@Override
	public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
		return proxy(md.getExportedKeys(catalog, schema, table));
	}

	@Override
	public ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable, String foreignCatalog, String foreignSchema,
	        String foreignTable) throws SQLException {
		return proxy(md.getCrossReference(parentCatalog, parentSchema, parentTable, foreignCatalog, foreignSchema, foreignTable));
	}

	@Override
	public ResultSet getTypeInfo() throws SQLException {
		return proxy(md.getTypeInfo());
	}

	@Override
	public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException {
		return proxy(md.getIndexInfo(catalog, schema, table, unique, approximate));
	}
	
	@Override
	public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException {
		return proxy(md.getUDTs(catalog, schemaPattern, typeNamePattern, types));
	}
	
	@Override
	public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException {
		return proxy(md.getSuperTypes(catalog, schemaPattern, typeNamePattern));
	}

	@Override
	public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
		return proxy(md.getSuperTables(catalog, schemaPattern, tableNamePattern));
	}

	@Override
	public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) throws SQLException {
		return proxy(md.getAttributes(catalog, schemaPattern, typeNamePattern, attributeNamePattern));
	}
	
	@Override
	public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
		return proxy(md.getSchemas(catalog,schemaPattern));
	}
	
	@Override
	public ResultSet getClientInfoProperties() throws SQLException {
		return proxy(md.getClientInfoProperties());
	}

	@Override
	public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException {
		return proxy(md.getFunctions(catalog, schemaPattern, functionNamePattern));
	}

	@Override
	public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern)
	        throws SQLException {
		return proxy(md.getFunctionColumns(catalog, schemaPattern, functionNamePattern, columnNamePattern));
	}

	@Override
	public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
		return proxy(md.getPseudoColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern));
	}
}
