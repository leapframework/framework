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
package leap.db.platform.postgresql;

import leap.db.model.DbIndexBuilder;
import leap.db.model.DbTableBuilder;
import leap.db.platform.GenericDbMetadataReader;
import leap.lang.jdbc.JDBC;

import java.sql.*;

public class PostgreSQL9MetadataReader extends GenericDbMetadataReader {

	public PostgreSQL9MetadataReader() {
		super();
	}
	
	@Override
    protected ResultSet getPrimaryKeys(Connection connection, DatabaseMetaData metadata, MetadataParameters params) throws SQLException {
		String sql = "select null table_cat," + 
					 "c.table_schema table_schem," + 
					 "c.table_name," + 
					 "c.constraint_name pk_name," + 
					 "cu.column_name," + 
					 "cu.ordinal_position key_seq " +  
					 "from information_schema.table_constraints c " +  
					 "join information_schema.key_column_usage cu " +  
					 "on cu.table_catalog = c.table_catalog " + 
					 "and cu.table_schema = c.table_schema " + 
					 "and cu.table_name = c.table_name " + 
					 "and cu.constraint_name = c.constraint_name " +  
					 "where c.table_schema = ? and c.constraint_type = 'PRIMARY KEY'";

		return executeSchemaQuery(connection, params, sql);
    }
	
	@Override
    protected ResultSet getIndexes(Connection connection, DatabaseMetaData dm, MetadataParameters params) throws SQLException {
		String sql = "select " + 
					 "null as table_cat, " + 
			    	 "n.nspname as table_schem, " + 
			    	 "t.relname as table_name, " + 
			    	 "i.relname as index_name, " + 
			    	 "a.attname as column_name," + 
			    	 "not ix.indisunique as non_unique," + 
			    	 "a.attnum as oridinal_position," + 
			    	 "3 as TYPE " + 
			    	 "from pg_catalog.pg_index ix " +  
			    	 "join pg_catalog.pg_class t on t.oid = ix.indrelid " + 
			    	 "join pg_catalog.pg_class i on i.oid = ix.indexrelid and t.relkind = 'r' " + 
			    	 "join pg_catalog.pg_namespace n on n.oid = t.relnamespace " +  
			    	 "join pg_catalog.pg_attribute a on a.attrelid = t.oid and a.attnum = ANY(ix.indkey) " + 
			    	 "where n.nspname = ?";

        return executeSchemaQuery(connection, params, sql);
	}
	
	@Override
    protected boolean isInternalIndex(DbTableBuilder table, DbIndexBuilder ix, ResultSet rs) throws SQLException {
		String name = ix.getName().toLowerCase();
		String tableName = table.getName().toLowerCase();
		
		//primary key
		if(name.equals(tableName + "_pkey")){
			return true;
		}
		
		//auto generatated unique index
		if(name.startsWith(tableName + "_") && name.endsWith("_key")){
			return true;
		}
		
		return false;
	}

	@Override
    protected ResultSet getSequences(Connection connection, DatabaseMetaData dm, MetadataParameters params) throws SQLException {
		String sql = "SELECT NULL SEQ_CAT," + 
					 "SEQUENCE_SCHEMA SEQ_SCHEM," + 
					 "SEQUENCE_NAME SEQ_NAME," + 
					 "START_VALUE SEQ_START," + 
					 "MINIMUM_VALUE SEQ_MINVALUE," + 
					 "MAXIMUM_VALUE SEQ_MAXVALUE," + 
					 "INCREMENT SEQ_INCREMENT," + 
					 "NULL SEQ_CACHE, " + 
					 "CASE CYCLE_OPTION WHEN 'NO' THEN 0 ELSE 1 END SEQ_CYCLE " + 
					 "FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA = ?";

        return executeSchemaQuery(connection, params, sql);
    }
	
	@Override
    protected String getTableCatalog(ResultSet rs) throws SQLException {
	    return super.getTableCatalog(rs);
    }

	@Override
    protected String getSchemaCatalog(ResultSet rs) throws SQLException {
	    return null;
    }
}
