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

import leap.db.model.DbColumnBuilder;
import leap.db.model.DbIndexBuilder;
import leap.db.model.DbTableBuilder;
import leap.db.platform.GenericDbMetadataReader;
import leap.lang.Strings;
import leap.lang.jdbc.JDBC;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

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
					 "null table_schem," +
					 "o.object_name table_name," + 
					 "o.object_type table_type," +
					 "c.comments remarks " + 
					 "from user_objects o join user_tab_comments c on o.OBJECT_NAME = c.TABLE_NAME " +
					 "where o.OBJECT_TYPE in ('TABLE','VIEW') and o.object_name not like 'BIN$%$%' and o.object_name not like 'XDB$%' "  +  
					 "and o.status = 'VALID' and o.object_name like ?";

        return executeTablePatternQuery(connection, params, sql);
    }
	
	@Override
    protected ResultSet getPrimaryKeys(Connection connection, DatabaseMetaData metadata, MetadataParameters params) throws SQLException {
	    String sql = "SELECT NULL AS table_cat,null AS table_schem," +
		   			 "c.table_name,c.column_name,c.position AS key_seq,c.constraint_name AS pk_name " +
		   			 "FROM user_cons_columns c, user_constraints k " +
		   			 "WHERE k.constraint_type = 'P' " +
		   			 "AND k.constraint_name = c.constraint_name AND k.table_name = c.table_name order by c.position asc";

        return executeQuery(connection, params, sql);
    }
	
	@Override
    protected ResultSet getForeignKeys(Connection connection, DatabaseMetaData metadata, MetadataParameters params) throws SQLException {
		String sql = "SELECT NULL AS pktable_cat,null as pktable_schem,p.table_name as pktable_name,pc.column_name as pkcolumn_name," +
					 "NULL as fktable_cat,null as fktable_schem,f.table_name as fktable_name,fc.column_name as fkcolumn_name," +
					 "fc.position as key_seq,NULL as update_rule,decode (f.delete_rule, 'CASCADE', 0, 'SET NULL', 2, 1) as delete_rule," +
					 "f.constraint_name as fk_name,p.constraint_name as pk_name " +
					 "FROM user_cons_columns pc, user_constraints p,user_cons_columns fc, user_constraints f " +
					 "WHERE f.constraint_type = 'R' " +
	                 "AND p.constraint_name = f.r_constraint_name " +
	                 "AND p.constraint_type = 'P' " + 
	                 "AND pc.constraint_name = p.constraint_name " +
	                 "AND pc.table_name = p.table_name " + 
	                 "AND fc.constraint_name = f.constraint_name " +
	                 "AND fc.table_name = f.table_name " + 
	                 "AND fc.position = pc.position ";// + "ORDER BY pktable_schem, pktable_name, key_seq";

        return executeQuery(connection, params, sql);
    }
	
	@Override
    protected ResultSet getIndexes(Connection connection, DatabaseMetaData metadata, MetadataParameters params) throws SQLException {
		String sql = "select null as table_cat,null as table_schem,i.table_name,decode(i.uniqueness, 'UNIQUE', 0, 1) as NON_UNIQUE," +
					 "null as index_qualifier,i.index_name,1 as type,c.column_position as ordinal_position,c.column_name," + 
					 "null as asc_or_desc,i.distinct_keys as cardinality,i.leaf_blocks as pages,null as filter_condition " + 
					 "from user_indexes i, user_ind_columns c " +
					 "where i.index_name = c.index_name " +
					 "and i.table_name = c.table_name " +
					 "order by index_name,ordinal_position";

        return executeQuery(connection, params, sql);
    }

	@Override
	protected ResultSet getSequences(Connection connection, DatabaseMetaData dm,
									 MetadataParameters params) throws SQLException {

        String sql = "SELECT ? AS SEQ_CAT, " +
				"null AS SEQ_SCHEM, " +
				"SEQUENCE_NAME AS SEQ_NAME, " +
				"NULL AS SEQ_START, " +
				"MIN_VALUE AS SEQ_MINVALUE, " +
				"MAX_VALUE AS SEQ_MAXVALUE, " +
				"INCREMENT_BY AS SEQ_INCREMENT, " +
				"CASE CYCLE_FLAG WHEN 'N' THEN 0 ELSE 1 END SEQ_CYCLE, " +
				"ORDER_FLAG, " +
				"CACHE_SIZE AS SEQ_CACHE, " +
				"LAST_NUMBER " +
				"FROM user_sequences";

        return executeCatalogQuery(connection, params, sql);
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

	@Override
	protected boolean isInternalIndex(DbTableBuilder table, DbIndexBuilder ix, ResultSet rs) throws SQLException {
		if(ix.getName().startsWith("SYS_")){
			return true;
		}
		return super.isInternalIndex(table, ix, rs);
	}
}
