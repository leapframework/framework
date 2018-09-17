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
package leap.db.platform;

import leap.core.jdbc.PreparedStatementHandlerAdapter;
import leap.db.Db;
import leap.lang.jdbc.JDBC;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

public class GenericDbPreparedStatementHandlers {

	protected static class AutoIncrementIdHandler extends PreparedStatementHandlerAdapter<Db> {
		
		protected final Consumer<Object> generatedIdCallback;
		protected String sql;
		
		protected AutoIncrementIdHandler(Consumer<Object> generatedIdCallback){
			this.generatedIdCallback = generatedIdCallback;
		}
		
		@Override
        public PreparedStatement preparedStatement(Db db, Connection connection, String sql) throws SQLException {
			this.sql = sql;
            return db.getDialect().createPreparedStatement(connection, sql, PreparedStatement.RETURN_GENERATED_KEYS);
        }

		@Override
        public void postExecuteUpdate(Db db, Connection connection, PreparedStatement ps, int updatedResult) throws SQLException {
			ResultSet rs = null;
			try{
				rs = ps.getGeneratedKeys();
				if(rs.next()){
					generatedIdCallback.accept(db.getDialect().getColumnValue(rs, 1));
				}else{
					throw new IllegalStateException("No generated key returned after the execution of sql : " + sql);
				}
			}finally{
				JDBC.closeResultSetOnly(rs);
			}
        }
	}
	
	protected static class InsertedSequenceValueHandler extends PreparedStatementHandlerAdapter<Db> {
		
		private static final Log log = LogFactory.get(GenericDbPreparedStatementHandlers.InsertedSequenceValueHandler.class);
		
		protected final String           sequenceName;
		protected final Consumer<Object> generatedIdCallback;
		
		protected boolean autoCommit;
		
		protected InsertedSequenceValueHandler(String sequenceName,Consumer<Object> generatedIdCallback){
			this.sequenceName        = sequenceName;
			this.generatedIdCallback = generatedIdCallback;
		}

		@Override
        public void preExecuteUpdate(Db db, Connection connection, PreparedStatement ps) throws SQLException {
			if(connection.getAutoCommit()){
				this.autoCommit = true;
				connection.setAutoCommit(false);
			}
		}

		@Override
        public void postExecuteUpdate(Db db, Connection connection, PreparedStatement ps, int updatedResult) throws SQLException {
			PreparedStatement ps1 = null;
			ResultSet rs = null;
			
			try{
				ps1 = connection.prepareStatement(db.getDialect().getSelectCurrentSequenceValueSql(sequenceName));
				
				rs = ps1.executeQuery();
				
				if(rs.next()){
					generatedIdCallback.accept(db.getDialect().getColumnValue(rs, 1));
				}else{
					throw new IllegalStateException("No current value of sequence '" + sequenceName + "' returned");
				}
			}finally{
				JDBC.closeResultSetOnly(rs);
				JDBC.closeStatementOnly(ps1);
				
				try {
	                if(this.autoCommit){
	                	connection.setAutoCommit(true);
	                }
                } catch (Exception e) {
                	log.warn("Error restoring the auto comment property of connection : " + e.getMessage(),e);
                }
			}
		}
	}
	
	protected GenericDbPreparedStatementHandlers(){
		
	}
}
