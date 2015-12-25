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
package leap.lang.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;

public class DatabaseMetaDataWrapper implements DatabaseMetaData {
	
	protected final DatabaseMetaData md;
	
	public DatabaseMetaDataWrapper(DatabaseMetaData md) {
		this.md = md;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return md.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return md.isWrapperFor(iface);
	}

	@Override
	public boolean allProceduresAreCallable() throws SQLException {
		return md.allProceduresAreCallable();
	}

	@Override
	public boolean allTablesAreSelectable() throws SQLException {
		return md.allTablesAreSelectable();
	}

	@Override
	public String getURL() throws SQLException {
		return md.getURL();
	}

	@Override
	public String getUserName() throws SQLException {
		return md.getUserName();
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return md.isReadOnly();
	}

	@Override
	public boolean nullsAreSortedHigh() throws SQLException {
		return md.nullsAreSortedHigh();
	}

	@Override
	public boolean nullsAreSortedLow() throws SQLException {
		return md.nullsAreSortedLow();
	}

	@Override
	public boolean nullsAreSortedAtStart() throws SQLException {
		return md.nullsAreSortedAtStart();
	}

	@Override
	public boolean nullsAreSortedAtEnd() throws SQLException {
		return md.nullsAreSortedAtEnd();
	}

	@Override
	public String getDatabaseProductName() throws SQLException {
		return md.getDatabaseProductName();
	}

	@Override
	public String getDatabaseProductVersion() throws SQLException {
		return md.getDatabaseProductVersion();
	}

	@Override
	public String getDriverName() throws SQLException {
		return md.getDriverName();
	}

	@Override
	public String getDriverVersion() throws SQLException {
		return md.getDriverVersion();
	}

	@Override
	public int getDriverMajorVersion() {
		return md.getDriverMajorVersion();
	}

	@Override
	public int getDriverMinorVersion() {
		return md.getDriverMinorVersion();
	}

	@Override
	public boolean usesLocalFiles() throws SQLException {
		return md.usesLocalFiles();
	}

	@Override
	public boolean usesLocalFilePerTable() throws SQLException {
		return md.usesLocalFilePerTable();
	}

	@Override
	public boolean supportsMixedCaseIdentifiers() throws SQLException {
		return md.supportsMixedCaseIdentifiers();
	}

	@Override
	public boolean storesUpperCaseIdentifiers() throws SQLException {
		return md.storesUpperCaseIdentifiers();
	}

	@Override
	public boolean storesLowerCaseIdentifiers() throws SQLException {
		return md.storesLowerCaseIdentifiers();
	}

	@Override
	public boolean storesMixedCaseIdentifiers() throws SQLException {
		return md.storesMixedCaseIdentifiers();
	}

	@Override
	public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
		return md.supportsMixedCaseQuotedIdentifiers();
	}

	@Override
	public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
		return md.storesUpperCaseQuotedIdentifiers();
	}

	@Override
	public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
		return md.storesLowerCaseQuotedIdentifiers();
	}

	@Override
	public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
		return md.storesMixedCaseQuotedIdentifiers();
	}

	@Override
	public String getIdentifierQuoteString() throws SQLException {
		return md.getIdentifierQuoteString();
	}

	@Override
	public String getSQLKeywords() throws SQLException {
		return md.getSQLKeywords();
	}

	@Override
	public String getNumericFunctions() throws SQLException {
		return md.getNumericFunctions();
	}

	@Override
	public String getStringFunctions() throws SQLException {
		return md.getStringFunctions();
	}

	@Override
	public String getSystemFunctions() throws SQLException {
		return md.getSystemFunctions();
	}

	@Override
	public String getTimeDateFunctions() throws SQLException {
		return md.getTimeDateFunctions();
	}

	@Override
	public String getSearchStringEscape() throws SQLException {
		return md.getSearchStringEscape();
	}

	@Override
	public String getExtraNameCharacters() throws SQLException {
		return md.getExtraNameCharacters();
	}

	@Override
	public boolean supportsAlterTableWithAddColumn() throws SQLException {
		return md.supportsAlterTableWithAddColumn();
	}

	@Override
	public boolean supportsAlterTableWithDropColumn() throws SQLException {
		return md.supportsAlterTableWithDropColumn();
	}

	@Override
	public boolean supportsColumnAliasing() throws SQLException {
		return md.supportsColumnAliasing();
	}

	@Override
	public boolean nullPlusNonNullIsNull() throws SQLException {
		return md.nullPlusNonNullIsNull();
	}

	@Override
	public boolean supportsConvert() throws SQLException {
		return md.supportsConvert();
	}

	@Override
	public boolean supportsConvert(int fromType, int toType) throws SQLException {
		return md.supportsConvert(fromType, toType);
	}

	@Override
	public boolean supportsTableCorrelationNames() throws SQLException {
		return md.supportsTableCorrelationNames();
	}

	@Override
	public boolean supportsDifferentTableCorrelationNames() throws SQLException {
		return md.supportsDifferentTableCorrelationNames();
	}

	@Override
	public boolean supportsExpressionsInOrderBy() throws SQLException {
		return md.supportsExpressionsInOrderBy();
	}

	@Override
	public boolean supportsOrderByUnrelated() throws SQLException {
		return md.supportsOrderByUnrelated();
	}

	@Override
	public boolean supportsGroupBy() throws SQLException {
		return md.supportsGroupBy();
	}

	@Override
	public boolean supportsGroupByUnrelated() throws SQLException {
		return md.supportsGroupByUnrelated();
	}

	@Override
	public boolean supportsGroupByBeyondSelect() throws SQLException {
		return md.supportsGroupByBeyondSelect();
	}

	@Override
	public boolean supportsLikeEscapeClause() throws SQLException {
		return md.supportsLikeEscapeClause();
	}

	@Override
	public boolean supportsMultipleResultSets() throws SQLException {
		return md.supportsMultipleResultSets();
	}

	@Override
	public boolean supportsMultipleTransactions() throws SQLException {
		return md.supportsMultipleTransactions();
	}

	@Override
	public boolean supportsNonNullableColumns() throws SQLException {
		return md.supportsNonNullableColumns();
	}

	@Override
	public boolean supportsMinimumSQLGrammar() throws SQLException {
		return md.supportsMinimumSQLGrammar();
	}

	@Override
	public boolean supportsCoreSQLGrammar() throws SQLException {
		return md.supportsCoreSQLGrammar();
	}

	@Override
	public boolean supportsExtendedSQLGrammar() throws SQLException {
		return md.supportsExtendedSQLGrammar();
	}

	@Override
	public boolean supportsANSI92EntryLevelSQL() throws SQLException {
		return md.supportsANSI92EntryLevelSQL();
	}

	@Override
	public boolean supportsANSI92IntermediateSQL() throws SQLException {
		return md.supportsANSI92IntermediateSQL();
	}

	@Override
	public boolean supportsANSI92FullSQL() throws SQLException {
		return md.supportsANSI92FullSQL();
	}

	@Override
	public boolean supportsIntegrityEnhancementFacility() throws SQLException {
		return md.supportsIntegrityEnhancementFacility();
	}

	@Override
	public boolean supportsOuterJoins() throws SQLException {
		return md.supportsOuterJoins();
	}

	@Override
	public boolean supportsFullOuterJoins() throws SQLException {
		return md.supportsFullOuterJoins();
	}

	@Override
	public boolean supportsLimitedOuterJoins() throws SQLException {
		return md.supportsLimitedOuterJoins();
	}

	@Override
	public String getSchemaTerm() throws SQLException {
		return md.getSchemaTerm();
	}

	@Override
	public String getProcedureTerm() throws SQLException {
		return md.getProcedureTerm();
	}

	@Override
	public String getCatalogTerm() throws SQLException {
		return md.getCatalogTerm();
	}

	@Override
	public boolean isCatalogAtStart() throws SQLException {
		return md.isCatalogAtStart();
	}

	@Override
	public String getCatalogSeparator() throws SQLException {
		return md.getCatalogSeparator();
	}

	@Override
	public boolean supportsSchemasInDataManipulation() throws SQLException {
		return md.supportsSchemasInDataManipulation();
	}

	@Override
	public boolean supportsSchemasInProcedureCalls() throws SQLException {
		return md.supportsSchemasInProcedureCalls();
	}

	@Override
	public boolean supportsSchemasInTableDefinitions() throws SQLException {
		return md.supportsSchemasInTableDefinitions();
	}

	@Override
	public boolean supportsSchemasInIndexDefinitions() throws SQLException {
		return md.supportsSchemasInIndexDefinitions();
	}

	@Override
	public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
		return md.supportsSchemasInPrivilegeDefinitions();
	}

	@Override
	public boolean supportsCatalogsInDataManipulation() throws SQLException {
		return md.supportsCatalogsInDataManipulation();
	}

	@Override
	public boolean supportsCatalogsInProcedureCalls() throws SQLException {
		return md.supportsCatalogsInProcedureCalls();
	}

	@Override
	public boolean supportsCatalogsInTableDefinitions() throws SQLException {
		return md.supportsCatalogsInTableDefinitions();
	}

	@Override
	public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
		return md.supportsCatalogsInIndexDefinitions();
	}

	@Override
	public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
		return md.supportsCatalogsInPrivilegeDefinitions();
	}

	@Override
	public boolean supportsPositionedDelete() throws SQLException {
		return md.supportsPositionedDelete();
	}

	@Override
	public boolean supportsPositionedUpdate() throws SQLException {
		return md.supportsPositionedUpdate();
	}

	@Override
	public boolean supportsSelectForUpdate() throws SQLException {
		return md.supportsSelectForUpdate();
	}

	@Override
	public boolean supportsStoredProcedures() throws SQLException {
		return md.supportsStoredProcedures();
	}

	@Override
	public boolean supportsSubqueriesInComparisons() throws SQLException {
		return md.supportsSubqueriesInComparisons();
	}

	@Override
	public boolean supportsSubqueriesInExists() throws SQLException {
		return md.supportsSubqueriesInExists();
	}

	@Override
	public boolean supportsSubqueriesInIns() throws SQLException {
		return md.supportsSubqueriesInIns();
	}

	@Override
	public boolean supportsSubqueriesInQuantifieds() throws SQLException {
		return md.supportsSubqueriesInQuantifieds();
	}

	@Override
	public boolean supportsCorrelatedSubqueries() throws SQLException {
		return md.supportsCorrelatedSubqueries();
	}

	@Override
	public boolean supportsUnion() throws SQLException {
		return md.supportsUnion();
	}

	@Override
	public boolean supportsUnionAll() throws SQLException {
		return md.supportsUnionAll();
	}

	@Override
	public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
		return md.supportsOpenCursorsAcrossCommit();
	}

	@Override
	public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
		return md.supportsOpenCursorsAcrossRollback();
	}

	@Override
	public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
		return md.supportsOpenStatementsAcrossCommit();
	}

	@Override
	public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
		return md.supportsOpenStatementsAcrossRollback();
	}

	@Override
	public int getMaxBinaryLiteralLength() throws SQLException {
		return md.getMaxBinaryLiteralLength();
	}

	@Override
	public int getMaxCharLiteralLength() throws SQLException {
		return md.getMaxCharLiteralLength();
	}

	@Override
	public int getMaxColumnNameLength() throws SQLException {
		return md.getMaxColumnNameLength();
	}

	@Override
	public int getMaxColumnsInGroupBy() throws SQLException {
		return md.getMaxColumnsInGroupBy();
	}

	@Override
	public int getMaxColumnsInIndex() throws SQLException {
		return md.getMaxColumnsInIndex();
	}

	@Override
	public int getMaxColumnsInOrderBy() throws SQLException {
		return md.getMaxColumnsInOrderBy();
	}

	@Override
	public int getMaxColumnsInSelect() throws SQLException {
		return md.getMaxColumnsInSelect();
	}

	@Override
	public int getMaxColumnsInTable() throws SQLException {
		return md.getMaxColumnsInTable();
	}

	@Override
	public int getMaxConnections() throws SQLException {
		return md.getMaxConnections();
	}

	@Override
	public int getMaxCursorNameLength() throws SQLException {
		return md.getMaxCursorNameLength();
	}

	@Override
	public int getMaxIndexLength() throws SQLException {
		return md.getMaxIndexLength();
	}

	@Override
	public int getMaxSchemaNameLength() throws SQLException {
		return md.getMaxSchemaNameLength();
	}

	@Override
	public int getMaxProcedureNameLength() throws SQLException {
		return md.getMaxProcedureNameLength();
	}

	@Override
	public int getMaxCatalogNameLength() throws SQLException {
		return md.getMaxCatalogNameLength();
	}

	@Override
	public int getMaxRowSize() throws SQLException {
		return md.getMaxRowSize();
	}

	@Override
	public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
		return md.doesMaxRowSizeIncludeBlobs();
	}

	@Override
	public int getMaxStatementLength() throws SQLException {
		return md.getMaxStatementLength();
	}

	@Override
	public int getMaxStatements() throws SQLException {
		return md.getMaxStatements();
	}

	@Override
	public int getMaxTableNameLength() throws SQLException {
		return md.getMaxTableNameLength();
	}

	@Override
	public int getMaxTablesInSelect() throws SQLException {
		return md.getMaxTablesInSelect();
	}

	@Override
	public int getMaxUserNameLength() throws SQLException {
		return md.getMaxUserNameLength();
	}

	@Override
	public int getDefaultTransactionIsolation() throws SQLException {
		return md.getDefaultTransactionIsolation();
	}

	@Override
	public boolean supportsTransactions() throws SQLException {
		return md.supportsTransactions();
	}

	@Override
	public boolean supportsTransactionIsolationLevel(int level) throws SQLException {
		return md.supportsTransactionIsolationLevel(level);
	}

	@Override
	public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
		return md.supportsDataDefinitionAndDataManipulationTransactions();
	}

	@Override
	public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
		return md.supportsDataManipulationTransactionsOnly();
	}

	@Override
	public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
		return md.dataDefinitionCausesTransactionCommit();
	}

	@Override
	public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
		return md.dataDefinitionIgnoredInTransactions();
	}

	@Override
	public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException {
		return md.getProcedures(catalog, schemaPattern, procedureNamePattern);
	}

	@Override
	public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern)
	        throws SQLException {
		return md.getProcedureColumns(catalog, schemaPattern, procedureNamePattern, columnNamePattern);
	}

	@Override
	public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
		return md.getTables(catalog, schemaPattern, tableNamePattern, types);
	}

	@Override
	public ResultSet getSchemas() throws SQLException {
		return md.getSchemas();
	}

	@Override
	public ResultSet getCatalogs() throws SQLException {
		return md.getCatalogs();
	}

	@Override
	public ResultSet getTableTypes() throws SQLException {
		return md.getTableTypes();
	}

	@Override
	public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
		return md.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
	}

	@Override
	public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException {
		return md.getColumnPrivileges(catalog, schema, table, columnNamePattern);
	}

	@Override
	public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
		return md.getTablePrivileges(catalog, schemaPattern, tableNamePattern);
	}

	@Override
	public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException {
		return md.getBestRowIdentifier(catalog, schema, table, scope, nullable);
	}

	@Override
	public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException {
		return md.getVersionColumns(catalog, schema, table);
	}

	@Override
	public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException {
		return md.getPrimaryKeys(catalog, schema, table);
	}

	@Override
	public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
		return md.getImportedKeys(catalog, schema, table);
	}

	@Override
	public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
		return md.getExportedKeys(catalog, schema, table);
	}

	@Override
	public ResultSet getCrossReference(String parentCatalog, String parentSchema, String parentTable, String foreignCatalog, String foreignSchema,
	        String foreignTable) throws SQLException {
		return md.getCrossReference(parentCatalog, parentSchema, parentTable, foreignCatalog, foreignSchema, foreignTable);
	}

	@Override
	public ResultSet getTypeInfo() throws SQLException {
		return md.getTypeInfo();
	}

	@Override
	public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException {
		return md.getIndexInfo(catalog, schema, table, unique, approximate);
	}

	@Override
	public boolean supportsResultSetType(int type) throws SQLException {
		return md.supportsResultSetType(type);
	}

	@Override
	public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException {
		return md.supportsResultSetConcurrency(type, concurrency);
	}

	@Override
	public boolean ownUpdatesAreVisible(int type) throws SQLException {
		return md.ownUpdatesAreVisible(type);
	}

	@Override
	public boolean ownDeletesAreVisible(int type) throws SQLException {
		return md.ownDeletesAreVisible(type);
	}

	@Override
	public boolean ownInsertsAreVisible(int type) throws SQLException {
		return md.ownInsertsAreVisible(type);
	}

	@Override
	public boolean othersUpdatesAreVisible(int type) throws SQLException {
		return md.othersUpdatesAreVisible(type);
	}

	@Override
	public boolean othersDeletesAreVisible(int type) throws SQLException {
		return md.othersDeletesAreVisible(type);
	}

	@Override
	public boolean othersInsertsAreVisible(int type) throws SQLException {
		return md.othersInsertsAreVisible(type);
	}

	@Override
	public boolean updatesAreDetected(int type) throws SQLException {
		return md.updatesAreDetected(type);
	}

	@Override
	public boolean deletesAreDetected(int type) throws SQLException {
		return md.deletesAreDetected(type);
	}

	@Override
	public boolean insertsAreDetected(int type) throws SQLException {
		return md.insertsAreDetected(type);
	}

	@Override
	public boolean supportsBatchUpdates() throws SQLException {
		return md.supportsBatchUpdates();
	}

	@Override
	public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException {
		return md.getUDTs(catalog, schemaPattern, typeNamePattern, types);
	}

	@Override
	public Connection getConnection() throws SQLException {
		return md.getConnection();
	}

	@Override
	public boolean supportsSavepoints() throws SQLException {
		return md.supportsSavepoints();
	}

	@Override
	public boolean supportsNamedParameters() throws SQLException {
		return md.supportsNamedParameters();
	}

	@Override
	public boolean supportsMultipleOpenResults() throws SQLException {
		return md.supportsMultipleOpenResults();
	}

	@Override
	public boolean supportsGetGeneratedKeys() throws SQLException {
		return md.supportsGetGeneratedKeys();
	}

	@Override
	public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException {
		return md.getSuperTypes(catalog, schemaPattern, typeNamePattern);
	}

	@Override
	public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
		return md.getSuperTables(catalog, schemaPattern, tableNamePattern);
	}

	@Override
	public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) throws SQLException {
		return md.getAttributes(catalog, schemaPattern, typeNamePattern, attributeNamePattern);
	}

	@Override
	public boolean supportsResultSetHoldability(int holdability) throws SQLException {
		return md.supportsResultSetHoldability(holdability);
	}

	@Override
	public int getResultSetHoldability() throws SQLException {
		return md.getResultSetHoldability();
	}

	@Override
	public int getDatabaseMajorVersion() throws SQLException {
		return md.getDatabaseMajorVersion();
	}

	@Override
	public int getDatabaseMinorVersion() throws SQLException {
		return md.getDatabaseMinorVersion();
	}

	@Override
	public int getJDBCMajorVersion() throws SQLException {
		return md.getJDBCMajorVersion();
	}

	@Override
	public int getJDBCMinorVersion() throws SQLException {
		return md.getJDBCMinorVersion();
	}

	@Override
	public int getSQLStateType() throws SQLException {
		return md.getSQLStateType();
	}

	@Override
	public boolean locatorsUpdateCopy() throws SQLException {
		return md.locatorsUpdateCopy();
	}

	@Override
	public boolean supportsStatementPooling() throws SQLException {
		return md.supportsStatementPooling();
	}

	@Override
	public RowIdLifetime getRowIdLifetime() throws SQLException {
		return md.getRowIdLifetime();
	}

	@Override
	public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
		return md.getSchemas(catalog,schemaPattern);
	}

	@Override
	public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
		return md.supportsStoredFunctionsUsingCallSyntax();
	}

	@Override
	public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
		return md.autoCommitFailureClosesAllResultSets();
	}

	@Override
	public ResultSet getClientInfoProperties() throws SQLException {
		return md.getClientInfoProperties();
	}

	@Override
	public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException {
		return md.getFunctions(catalog, schemaPattern, functionNamePattern);
	}

	@Override
	public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern)
	        throws SQLException {
		return md.getFunctionColumns(catalog, schemaPattern, functionNamePattern, columnNamePattern);
	}

	@Override
	public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
		return md.getPseudoColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
	}

	@Override
	public boolean generatedKeyAlwaysReturned() throws SQLException {
		return md.generatedKeyAlwaysReturned();
	}

}
