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
package leap.db.platform.oracle;

import java.sql.*;

import leap.db.model.DbColumnBuilder;
import leap.db.model.DbTableBuilder;
import leap.db.platform.GenericDbMetadataReader;
import leap.lang.Strings;
import leap.lang.jdbc.JDBC;
import leap.lang.jdbc.JdbcType;
import leap.lang.jdbc.JdbcTypes;

public class Oracle10MetadataReader extends GenericDbMetadataReader {

	public Oracle10MetadataReader() {
	}

	@Override
    protected MetadataParameters createMetadataParameters(Connection connection, DatabaseMetaData dm, String catalog, String schema) {
		return super.createMetadataParameters(connection, dm, catalog, Strings.upperCase(schema));
    }

	@Override
    protected ResultSet getTables(Connection connection, DatabaseMetaData dm, MetadataParameters params) throws SQLException {
		String sql = "select " +  
					 "null table_cat," + 
					 "o.owner table_schem," + 
					 "o.object_name table_name," + 
					 "o.object_type table_type," +
					 "c.comments remarks " + 
					 "from all_objects o join all_tab_comments c on o.owner = c.owner and o.OBJECT_NAME = c.TABLE_NAME " + 
					 "where o.OBJECT_TYPE in ('TABLE','VIEW') and o.object_name not like 'BIN$%$%' and o.object_name not like 'XDB$%' "  +  
					 "and o.owner = ? and o.status = 'VALID' and o.object_name like ?";

		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
	
			ps.setString(1, params.schema);
			ps.setString(2, params.tablePattern);
			
			return ps.executeQuery();
		}catch(SQLException e){
			JDBC.closeStatementOnly(ps);
			throw e;
		}
    }
	
	@Override
    protected ResultSet getPrimaryKeys(Connection connection, DatabaseMetaData metadata, MetadataParameters params) throws SQLException {
	    String sql = "SELECT NULL AS table_cat,c.owner AS table_schem," +
		   			 "c.table_name,c.column_name,c.position AS key_seq,c.constraint_name AS pk_name " +
		   			 "FROM all_cons_columns c, all_constraints k " + 
		   			 "WHERE k.constraint_type = 'P' " +
					 "AND k.owner = ? " +
		   			 "AND k.constraint_name = c.constraint_name AND k.table_name = c.table_name AND k.owner = c.owner ";
		
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);

			ps.setString(1, params.schema);
			
			return ps.executeQuery();
		}catch(SQLException e){
			JDBC.closeStatementOnly(ps);
			throw e;
		}
    }
	
	@Override
    protected ResultSet getForeignKeys(Connection connection, DatabaseMetaData metadata, MetadataParameters params) throws SQLException {
		String sql = "SELECT NULL AS pktable_cat,p.owner as pktable_schem,p.table_name as pktable_name,pc.column_name as pkcolumn_name," +
					 "NULL as fktable_cat,f.owner as fktable_schem,f.table_name as fktable_name,fc.column_name as fkcolumn_name," +
					 "fc.position as key_seq,NULL as update_rule,decode (f.delete_rule, 'CASCADE', 0, 'SET NULL', 2, 1) as delete_rule," +
					 "f.constraint_name as fk_name,p.constraint_name as pk_name " +
					 "FROM all_cons_columns pc, all_constraints p,all_cons_columns fc, all_constraints f " +
					 "WHERE p.owner = ? " +
	                 "AND f.constraint_type = 'R' " +
	                 "AND p.owner = f.r_owner " + 
	                 "AND p.constraint_name = f.r_constraint_name " + 
	                 "AND p.constraint_type = 'P' " + 
	                 "AND pc.owner = p.owner " + 
	                 "AND pc.constraint_name = p.constraint_name " + 
	                 "AND pc.table_name = p.table_name " + 
	                 "AND fc.owner = f.owner " + 
	                 "AND fc.constraint_name = f.constraint_name " + 
	                 "AND fc.table_name = f.table_name " + 
	                 "AND fc.position = pc.position " + 
	                 "ORDER BY pktable_schem, pktable_name, key_seq";
		
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);

			ps.setString(1, params.schema);
			
			return ps.executeQuery();
		}catch(SQLException e){
			JDBC.closeStatementOnly(ps);
			throw e;
		}
    }
	
	@Override
    protected ResultSet getIndexes(Connection connection, DatabaseMetaData metadata, MetadataParameters params) throws SQLException {
		String sql = "select null as table_cat,i.owner as table_schem,i.table_name,decode(i.uniqueness, 'UNIQUE', 0, 1) as NON_UNIQUE," +
					 "null as index_qualifier,i.index_name,1 as type,c.column_position as ordinal_position,c.column_name," + 
					 "null as asc_or_desc,i.distinct_keys as cardinality,i.leaf_blocks as pages,null as filter_condition " + 
					 "from all_indexes i, all_ind_columns c " + 
					 "where i.OWNER = ? " +
					 "and i.index_name = c.index_name " + 
					 "and i.table_owner = c.table_owner " + 
					 "and i.table_name = c.table_name " + 
					 "and i.owner = c.index_owner " + 
					 "order by index_name,ordinal_position";	
		
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
	protected ResultSet getSequences(Connection connection, DatabaseMetaData dm,
									 MetadataParameters params) throws SQLException {
		// TODO query sequences
		String sql = "SELECT ? AS SEQ_CAT, " +
				"SEQUENCE_OWNER AS SEQ_SCHEM, " +
				"SEQUENCE_NAME AS SEQ_NAME, " +
				"MIN_VALUE AS SEQ_START, " +
				"MIN_VALUE AS SEQ_MINVALUE, " +
				"MAX_VALUE AS SEQ_MAXVALUE, " +
				"INCREMENT_BY AS SEQ_INCREMENT, " +
				"CASE CYCLE_FLAG WHEN 'N' THEN 0 ELSE 1 END SEQ_CYCLE, " +
				"ORDER_FLAG, " +
				"CACHE_SIZE AS SEQ_CACHE, " +
				"LAST_NUMBER " +
				"FROM all_sequences " +
				"WHERE SEQUENCE_OWNER = ?";
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setString(1, params.catalog);
			ps.setString(2, params.schema);
			return ps.executeQuery();
		} catch(SQLException e) {
			JDBC.closeStatementOnly(ps);
			throw e;
		}
	}

	@Override
	protected boolean readColumnProperties(DbTableBuilder table, DbColumnBuilder column,
										   ResultSet rs) throws SQLException {
		boolean res = super.readColumnProperties(table, column, rs);
		if(column.getTypeCode() == Types.DECIMAL){
			column.setPrecision(rs.getInt(COLUMN_SIZE));
		}
		return res;
	}
}
