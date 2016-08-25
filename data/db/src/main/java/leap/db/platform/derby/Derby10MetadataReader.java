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
package leap.db.platform.derby;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import leap.db.model.DbIndexBuilder;
import leap.db.model.DbTableBuilder;
import leap.db.platform.GenericDbMetadataReader;
import leap.lang.jdbc.JDBC;

public class Derby10MetadataReader extends GenericDbMetadataReader {
	
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
    protected ResultSet getSequences(Connection connection, DatabaseMetaData dm, MetadataParameters params) throws SQLException {
		String sql = "SELECT " + 
					 "'' SEQ_CAT, " + 
					 "SC.SCHEMANAME SEQ_SCHEM," + 
					 "SEQUENCENAME SEQ_NAME," + 
					 "INCREMENT SEQ_INCREMENT," + 
					 "0 SEQ_CACHE," + 
					 "STARTVALUE SEQ_START," + 
					 "CASE WHEN SE.CYCLEOPTION = 'Y' THEN 1 ELSE 0 END SEQ_CYCLE," + 
					 "MINIMUMVALUE SEQ_MINVALUE," + 
					 "MAXIMUMVALUE SEQ_MAXVALUE " + 
					 "FROM SYS.SYSSEQUENCES SE INNER JOIN SYS.SYSSCHEMAS SC ON SE.SCHEMAID = SC.SCHEMAID " + 
					 "WHERE SC.SCHEMANAME = ?";

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
    protected boolean isInternalIndex(DbTableBuilder table, DbIndexBuilder ix, ResultSet rs) throws SQLException {
		return isInternalIndexName(ix.getName());
    }
	
	protected boolean isInternalIndexName(String name) {
		//SQL140908223119600
		if(name.startsWith("SQL") && name.length() > 3){
			String nkey = name.substring(3);
			if(nkey.length() == "140908223119600".length()){
				try {
	                Long.parseLong(nkey);
	                return true;
                } catch (NumberFormatException e) {
                	return false;
                }
			}
		}
		return false;
	}
	
	
}