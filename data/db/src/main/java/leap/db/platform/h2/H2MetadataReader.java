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
package leap.db.platform.h2;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import leap.db.model.DbColumnBuilder;
import leap.db.model.DbForeignKeyBuilder;
import leap.db.model.DbIndexBuilder;
import leap.db.model.DbSequenceBuilder;
import leap.db.model.DbTableBuilder1;
import leap.db.platform.GenericDbMetadataReader;
import leap.lang.Strings;
import leap.lang.jdbc.JDBC;

public class H2MetadataReader extends GenericDbMetadataReader {

	protected H2MetadataReader(){
		
	}
	
	@Override
    protected ResultSet getPrimaryKeys(Connection connection, DatabaseMetaData metadata, MetadataParameters params) throws SQLException {
		String sql = "SELECT " + 
					 "NULL TABLE_CAT, " + 
					 "TABLE_SCHEMA TABLE_SCHEM, " + 
					 "TABLE_NAME, " + 
					 "COLUMN_NAME, " + 
					 "ORDINAL_POSITION KEY_SEQ, " + 
					 "IFNULL(CONSTRAINT_NAME, INDEX_NAME) PK_NAME " + 
					 "FROM INFORMATION_SCHEMA.INDEXES " + 
					 "WHERE TABLE_SCHEMA = ? " + 
					 "AND PRIMARY_KEY = TRUE ";
		
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
	
			ps.setString(1, params.schema);
			
			return ps.executeQuery();
		} catch(SQLException e) {
			JDBC.closeStatementOnly(ps);
			throw e;
		}
    }
	
	@Override
    protected ResultSet getForeignKeys(Connection connection, DatabaseMetaData metadata, MetadataParameters params) throws SQLException {
		String sql = "SELECT " + 
					 "NULL PKTABLE_CAT, " + 
					 "PKTABLE_SCHEMA PKTABLE_SCHEM, " + 
					 "PKTABLE_NAME PKTABLE_NAME, " + 
					 "PKCOLUMN_NAME, " + 
					 "NULL FKTABLE_CAT, " + 
					 "FKTABLE_SCHEMA FKTABLE_SCHEM, " + 
					 "FKTABLE_NAME, " + 
					 "FKCOLUMN_NAME, " + 
					 "ORDINAL_POSITION KEY_SEQ, " + 
					 "UPDATE_RULE, " + 
					 "DELETE_RULE, " + 
					 "FK_NAME, " + 
					 "PK_NAME, " + 
					 "DEFERRABILITY " + 
					 "FROM INFORMATION_SCHEMA.CROSS_REFERENCES " + 
					 "WHERE FKTABLE_SCHEMA = ? ";
	
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
	
			ps.setString(1, params.schema);
			
			return ps.executeQuery();
		} catch(SQLException e) {
			JDBC.closeStatementOnly(ps);
			throw e;
		}
    }

	@Override
    protected ResultSet getIndexes(Connection connection, DatabaseMetaData metadata, MetadataParameters params) throws SQLException {
		String sql = "SELECT " + 
					 "NULL TABLE_CAT, " + 
					 "TABLE_SCHEMA TABLE_SCHEM, " + 
					 "TABLE_NAME, " + 
					 "NON_UNIQUE, " + 
					 "TABLE_CATALOG INDEX_QUALIFIER, " + 
					 "INDEX_NAME, " + 
					 "INDEX_TYPE TYPE, " + 
					 "ORDINAL_POSITION, " + 
					 "COLUMN_NAME, " + 
					 "ASC_OR_DESC, " + 
					 // TODO meta data for number of unique values in an index
					 "CARDINALITY " + 
					 "FROM INFORMATION_SCHEMA.INDEXES " + 
					 "WHERE TABLE_SCHEMA = ? ";
	
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
	
			ps.setString(1, params.schema);
			
			return ps.executeQuery();
		} catch(SQLException e) {
			JDBC.closeStatementOnly(ps);
			throw e;
		}
    }

	@Override
    protected ResultSet getSequences(Connection connection, DatabaseMetaData dm, MetadataParameters params) throws SQLException {
		String sql = "SELECT " + 
					 "NULL SEQ_CAT, " + 
					 "SEQUENCE_SCHEMA SEQ_SCHEM," + 
					 "SEQUENCE_NAME SEQ_NAME," + 
					 "INCREMENT SEQ_INCREMENT," + 
					 "CACHE SEQ_CACHE," + 
					 "NULL SEQ_START," + 
					 "NULL SEQ_CYCLE," + 
					 "NULL SEQ_MINVALUE," + 
					 "NULL SEQ_MAXVALUE " + 
					 "FROM INFORMATION_SCHEMA.SEQUENCES " + 
					 "WHERE SEQUENCE_SCHEMA = ?";

		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
	
			ps.setString(1, params.schema);
			
			return ps.executeQuery();
		} catch(SQLException e) {
			JDBC.closeStatementOnly(ps);
			throw e;
		}
    }
	
	@Override
    protected boolean isInternalDefaultValue(DbTableBuilder1 table, DbColumnBuilder column, ResultSet rs, String defaultValue) throws SQLException {
		return Strings.containsIgnoreCase(defaultValue,"SYSTEM_SEQUENCE_");
    }

	@Override
    protected boolean isInternalSequence(DbSequenceBuilder seq, ResultSet rs) throws SQLException {
		return Strings.startsWithIgnoreCase(seq.getName(), "SYSTEM_SEQUENCE_");
	}
	
	@Override
    protected boolean isInternalIndex(DbTableBuilder1 table, DbIndexBuilder ix, ResultSet rs) throws SQLException {
		String name = ix.getName();
		
	    if(Strings.upperCase(name).startsWith("PRIMARY_KEY_")){
	    	return true;
	    }
	    
	    if(Strings.upperCase(name).startsWith("CONSTRAINT_INDEX_")){
	    	return true;
	    }
	    
	    //i.e. FK_TEST_ADDFK_CHANGE_INDEX_7, FK_TEST_ADDFK_CHANGE is foreign key's name
	    for(DbForeignKeyBuilder fk : table.getForeignKeys()) {
	    	if(name.toUpperCase().startsWith(fk.getName().toUpperCase() + "_INDEX_")){
	    		return true;
	    	}
	    }
	    
	    return false;
	}
}
