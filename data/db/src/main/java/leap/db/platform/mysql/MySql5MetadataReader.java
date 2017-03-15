/*
 * Copyright 2013 the original author or authors.
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
package leap.db.platform.mysql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import leap.db.model.DbForeignKeyBuilder;
import leap.db.model.DbIndexBuilder;
import leap.db.model.DbTableBuilder;
import leap.db.platform.GenericDbMetadataReader;
import leap.lang.Strings;
import leap.lang.jdbc.JDBC;
import static java.sql.DatabaseMetaData.*;

public class MySql5MetadataReader extends GenericDbMetadataReader {

	protected MySql5MetadataReader() {
		
	}

	@Override
    protected ResultSet getSchemas(Connection connection, DatabaseMetaData dm) throws SQLException {
		return dm.getCatalogs();
    }

	@Override
    protected String getSchemaCatalog(ResultSet rs) throws SQLException {
		return null;
    }

	@Override
    protected String getSchemaName(ResultSet rs) throws SQLException {
	    return rs.getString(TABlE_CATALOG);
    }
	
	@Override
    protected String getColumnSchema(ResultSet rs) throws SQLException {
		return rs.getString(TABlE_CATALOG);
    }

	@Override
    protected MetadataParameters createMetadataParameters(Connection connection, DatabaseMetaData dm, String catalog, String schema) {
		MetadataParameters p = super.createMetadataParameters(connection, dm, catalog, schema);
		
	    if(!Strings.isEmpty(schema)){
	    	int index = schema.indexOf("@");
	    	if(index > 0){
	    		schema = schema.substring(0,index);
	    	}
	    	p.catalogPattern = schema;
	    	p.schemaPattern  = getDefaultSchemaPattern();
	    }
	    
	    return p;
    }

	@Override
    protected ResultSet getTables(Connection connection, DatabaseMetaData dm, MetadataParameters params) throws SQLException {
		String sql = "SELECT " +
					 "NULL AS TABLE_CAT," + 
					 "TABLE_SCHEMA AS TABLE_SCHEM," +
					 "TABLE_NAME," +
					 "CASE WHEN TABLE_TYPE='BASE TABLE' THEN 'TABLE' WHEN TABLE_TYPE='TEMPORARY' THEN 'LOCAL_TEMPORARY' ELSE TABLE_TYPE END AS TABLE_TYPE," + 
					 "TABLE_COMMENT AS REMARKS " + 
					 "FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME LIKE ?";

        return executeSchemaAndTablePatternQuery(connection, params, sql);
    }
	
	@Override
    protected ResultSet getPrimaryKeys(Connection connection, DatabaseMetaData metadata, MetadataParameters params) throws SQLException {
		String sql = "SELECT " + 
					 "NULL AS TABLE_CAT," + 
					 "A.TABLE_SCHEMA AS TABLE_SCHEM," + 
					 "A.TABLE_NAME," + 
					 "A.COLUMN_NAME," + 
					 "A.ORDINAL_POSITION AS KEY_SEQ," + 
					 "A.CONSTRAINT_NAME AS PK_NAME " + 
					 "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE A " + 
					 "JOIN INFORMATION_SCHEMA.TABLE_CONSTRAINTS B " + 
					 " ON IFNULL(A.CONSTRAINT_CATALOG,'') = IFNULL(B.CONSTRAINT_CATALOG,'') " + 
					 " AND A.CONSTRAINT_SCHEMA = B.CONSTRAINT_SCHEMA " +  
					 " AND A.CONSTRAINT_NAME = B.CONSTRAINT_NAME " + 
					 " AND A.TABLE_NAME = B.TABLE_NAME " + 
					 " WHERE B.CONSTRAINT_TYPE = 'PRIMARY KEY' " + 
					 " AND A.TABLE_SCHEMA = ? "; 
		
        return executeSchemaQuery(connection, params, sql);
    }
	
	@Override
    protected ResultSet getForeignKeys(Connection connection, DatabaseMetaData metadata, MetadataParameters params) throws SQLException {
		String sql = "SELECT " +
				     "NULL AS PKTABLE_CAT," + 
					 "A.REFERENCED_TABLE_SCHEMA AS PKTABLE_SCHEM," + 
				     "A.REFERENCED_TABLE_NAME AS PKTABLE_NAME," +
			         "A.REFERENCED_COLUMN_NAME AS PKCOLUMN_NAME," + 
				     "NULL AS FKTABLE_CAT," +
				     "A.TABLE_SCHEMA AS FKTABLE_SCHEM," + 
				     "A.TABLE_NAME AS FKTABLE_NAME," +  
				     "A.COLUMN_NAME AS FKCOLUMN_NAME," + 
				     "A.ORDINAL_POSITION AS KEY_SEQ,"  + 
				      generateUpdateRuleClause() + " AS UPDATE_RULE," + 
				      generateDeleteRuleClause() + " AS DELETE_RULE," + 
				     "A.CONSTRAINT_NAME AS FK_NAME," + 
				     "IFNULL(R.UNIQUE_CONSTRAINT_NAME,'PRIMARY') AS PK_NAME " + 
				     "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE A " + 
				     "JOIN INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS R " + 
				     " ON IFNULL(A.CONSTRAINT_CATALOG,'') = IFNULL(R.CONSTRAINT_CATALOG,'') " +
				     " AND A.CONSTRAINT_SCHEMA = R.CONSTRAINT_SCHEMA " +
				     " AND A.CONSTRAINT_NAME = R.CONSTRAINT_NAME " +
				     " AND A.TABLE_NAME = R.TABLE_NAME " + 
				     "WHERE A.TABLE_SCHEMA = ? ";

        return executeSchemaQuery(connection, params, sql);
    }
	
	@Override
    protected ResultSet getIndexes(Connection connection, DatabaseMetaData metadata, MetadataParameters params) throws SQLException {
		String sql = "SELECT " +
				     "NULL AS TABLE_CAT, " + 
				     "TABLE_SCHEMA AS TABLE_SCHEM,"+
				     "TABLE_NAME," + 
				     "NON_UNIQUE," + 
				     "TABLE_SCHEMA AS INDEX_QUALIFIER," + 
				     "INDEX_NAME," +
				     tableIndexOther + " AS TYPE," + 
				     "SEQ_IN_INDEX AS ORDINAL_POSITION," + 
				     "COLUMN_NAME," + 
				     "COLLATION AS ASC_OR_DESC," + 
				     "CARDINALITY," + 
				     "NULL AS PAGES," + 
				     "NULL AS FILTER_CONDITION " + 
					 "FROM INFORMATION_SCHEMA.STATISTICS " +
					 "WHERE INDEX_NAME != 'PRIMARY' AND TABLE_SCHEMA = ? ";

        return executeSchemaQuery(connection, params, sql);
    }

	@Override
    protected boolean isInternalIndex(DbTableBuilder table, DbIndexBuilder ix, ResultSet rs) throws SQLException {
		//primary key index
		if(ix.getName().equals("PRIMARY")){
			return true;
		}
		
		//auto generated unique column's index
		if(ix.isUnique()){
			return table.findColumn(ix.getName()) != null;
		}
		
		//auto generated index of foriegn key
		for(DbForeignKeyBuilder fk : table.getForeignKeys()) {
		    if(fk.getName().equals(ix.getName())) {
		        return true;
		    }
		}
		
		return false;
	}
}
