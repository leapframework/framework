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
package leap.db.platform.db2;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import leap.db.platform.GenericDbMetadataReader;
import leap.lang.jdbc.JDBC;

public class DB29MetadataReader extends GenericDbMetadataReader {

	protected DB29MetadataReader(){
		
	}
	
	@Override
	protected ResultSet getPrimaryKeys(Connection connection, DatabaseMetaData dm, MetadataParameters params) throws SQLException {
		String sql = "SELECT " +
				 "'' AS TABLE_CAT," + 
				 "K.TABSCHEMA AS TABLE_SCHEM," + 
				 "K.TABNAME AS TABLE_NAME," + 
				 "K.COLNAME AS COLUMN_NAME," + 
				 "K.COLSEQ AS KEY_SEQ," + 
				 "K.CONSTNAME AS PK_NAME " + 
				 "FROM SYSCAT.KEYCOLUSE K " + 
				 "JOIN SYSCAT.COLUMNS C ON K.TABSCHEMA = C.TABSCHEMA AND K.TABNAME = C.TABNAME AND K.COLNAME = C.COLNAME " + 
				 "WHERE C.KEYSEQ IS NOT NULL AND K.TABSCHEMA = ? ORDER BY K.CONSTNAME,K.COLSEQ";

		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
	
			ps.setString(1, params.schema);
	
			return ps.executeQuery();
		} catch (SQLException e) {
			JDBC.closeStatementOnly(ps);
			throw e;
		}
	}

	@Override
	protected ResultSet getForeignKeys(Connection connection, DatabaseMetaData dm, MetadataParameters params) throws SQLException {
		String sql = "SELECT " +
				 "'' AS PKTABLE_CAT," + 
				 "PK.TABSCHEMA AS PKTABLE_SCHEM," + 
				 "PK.TABNAME AS PKTABLE_NAME," + 
				 "PK.COLNAME AS PKCOLUMN_NAME," + 
				 "PK.CONSTNAME AS PK_NAME," + 
				 "'' AS FKTABLE_CAT," + 
				 "FK.TABSCHEMA AS FKTABLE_SCHEM," +     
				 "FK.TABNAME AS FKTABLE_NAME," + 
				 "FK.COLNAME AS FKCOLUMN_NAME," +      
				 "FK.COLSEQ AS KEY_SEQ," + 
				 "FK.CONSTNAME AS FK_NAME," + 
				 "CASE C.UPDATE_RULE WHEN 'NO ACTION' THEN 3 WHEN 'SET NULL' THEN 2 WHEN 'SET DEFAULT' THEN 4 WHEN 'CASCADE' THEN 0 ELSE 1 END AS UPDATE_RULE," +  
				 "CASE C.DELETE_RULE WHEN 'NO ACTION' THEN 3 WHEN 'SET NULL' THEN 2 WHEN 'SET DEFAULT' THEN 4 WHEN 'CASCADE' THEN 0 ELSE 1 END AS DELETE_RULE " + 
				 "FROM SYSIBM.REFERENTIAL_CONSTRAINTS C " +  
				 "  JOIN SYSCAT.KEYCOLUSE FK ON C.CONSTRAINT_SCHEMA = FK.TABSCHEMA AND C.CONSTRAINT_NAME = FK.CONSTNAME " + 
				 "  JOIN SYSCAT.KEYCOLUSE PK ON C.UNIQUE_CONSTRAINT_SCHEMA = PK.TABSCHEMA AND C.UNIQUE_CONSTRAINT_NAME = PK.CONSTNAME " +  
				 "WHERE C.CONSTRAINT_SCHEMA = ? ORDER BY FK_NAME,KEY_SEQ";
	
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
	
			ps.setString(1, params.schema);
	
			return ps.executeQuery();
		} catch (SQLException e) {
			JDBC.closeStatementOnly(ps);
			throw e;
		}	
	}

	@Override
	protected ResultSet getIndexes(Connection connection, DatabaseMetaData dm, MetadataParameters params) throws SQLException {
		String sql = "SELECT " +
				 "'' AS TABLE_CAT," + 
				 "I.TABSCHEMA AS TABLE_SCHEM," + 
				 "I.TABNAME AS TABLE_NAME," + 
				 "C.INDNAME AS INDEX_NAME," + 
				 "1 AS TYPE," + 
				 "C.COLNAME AS COLUMN_NAME," + 
				 "C.COLSEQ AS ORDINAL_POSITION," + 
				 "CASE I.UNIQUERULE WHEN 'U' THEN 0 ELSE 1 END AS NON_UNIQUE " + 
				 "FROM SYSCAT.INDEXCOLUSE C " + 
				 "  JOIN SYSCAT.INDEXES I ON C.INDSCHEMA = I.INDSCHEMA AND C.INDNAME = I.INDNAME " + 
				 "WHERE I.UNIQUERULE != 'P' AND I.TABSCHEMA = ? ORDER BY INDEX_NAME,ORDINAL_POSITION";
	
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
	
			ps.setString(1, params.schema);
	
			return ps.executeQuery();
		} catch (SQLException e) {
			JDBC.closeStatementOnly(ps);
			throw e;
		}
	}

	@Override
    protected ResultSet getSequences(Connection connection, DatabaseMetaData dm, MetadataParameters params) throws SQLException {
		String sql = "SELECT " + 
					 "'' SEQ_CAT, " + 
					 "SEQSCHEMA SEQ_SCHEM," + 
					 "SEQNAME SEQ_NAME," + 
					 "INCREMENT SEQ_INCREMENT," + 
					 "CACHE SEQ_CACHE," + 
					 "START SEQ_START," + 
					 "CASE WHEN CYCLE = 'Y' THEN 1 ELSE 0 END SEQ_CYCLE," + 
					 "MINVALUE SEQ_MINVALUE," + 
					 "MAXVALUE SEQ_MAXVALUE " + 
					 "FROM SYSCAT.SEQUENCES S " + 
					 "WHERE S.SEQSCHEMA = ?";

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
	
}
