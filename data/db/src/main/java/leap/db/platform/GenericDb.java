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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import leap.core.jdbc.BatchPreparedStatementHandler;
import leap.core.jdbc.PreparedStatementHandler;
import leap.core.jdbc.ResultSetReader;
import leap.core.transaction.Transactions;
import leap.db.Db;
import leap.db.DbBase;
import leap.db.DbComparator;
import leap.db.DbExecution;
import leap.db.DbMetadata;
import leap.db.DbPlatform;
import leap.db.command.AlterTable;
import leap.db.command.CreateColumn;
import leap.db.command.CreateForeignKey;
import leap.db.command.CreateIndex;
import leap.db.command.CreatePrimaryKey;
import leap.db.command.CreateSequence;
import leap.db.command.CreateTable;
import leap.db.command.DropColumn;
import leap.db.command.DropForeignKey;
import leap.db.command.DropIndex;
import leap.db.command.DropPrimaryKey;
import leap.db.command.DropSchema;
import leap.db.command.DropSequence;
import leap.db.command.DropTable;
import leap.db.command.RenameColumn;
import leap.db.model.DbColumn;
import leap.db.model.DbForeignKey;
import leap.db.model.DbIndex;
import leap.db.model.DbPrimaryKey;
import leap.db.model.DbSchemaObjectName;
import leap.db.model.DbSequence;
import leap.db.model.DbTable;
import leap.lang.Args;
import leap.lang.Arrays2;
import leap.lang.Exceptions;
import leap.lang.Strings;
import leap.lang.exception.NestedSQLException;
import leap.lang.jdbc.ConnectionCallback;
import leap.lang.jdbc.ConnectionCallbackWithResult;
import leap.lang.jdbc.JDBC;
import leap.lang.jdbc.JdbcTypes;
import leap.lang.logging.Log;
import leap.lang.time.StopWatch;

public class GenericDb extends DbBase {
	
	public GenericDb(String name,DbPlatform platform, DataSource dataSource, DbMetadata metadata, GenericDbDialect dialect, DbComparator comparator) {
	    super(name, platform, dataSource, metadata, dialect, comparator);
    }
	
	public Log log(){
		return log;
	}

	@Override
    public GenericDbDialect getDialect() {
	    return (GenericDbDialect)super.getDialect();
    }

    protected Connection getConnection() throws NestedSQLException {
    	return Transactions.getConnection(dataSource);
    }
    
    protected void closeConnection(Connection connection) {
    	if(null != connection){
    		Transactions.closeConnection(connection, dataSource);	
    	}
    }
	
	@Override
    public boolean checkTableExists(String tableName) throws NestedSQLException {
	    return metadata.tryGetTable(tableName) != null;
    }

	@Override
    public boolean checkTableExists(DbSchemaObjectName tableName) throws NestedSQLException {
	    return metadata.tryGetTable(tableName) != null;
    }
	
	@Override
    public boolean checkSequenceExists(String sequenceName) throws NestedSQLException {
	    return metadata.tryGetSequence(sequenceName) != null;
    }

	@Override
    public boolean checkSequenceExists(DbSchemaObjectName sequenceName) throws NestedSQLException {
	    return metadata.tryGetSequence(sequenceName) != null;
    }

	@Override
    public CreateTable cmdCreateTable(DbTable table) {
	    return new GenericDbCommands.GenericCreateTable(this, table);
    }

	@Override
    public AlterTable cmdAlterTable(DbSchemaObjectName tableName) {
	    return new GenericDbCommands.GenericAlterTable(this, tableName);
    }

	@Override
    public DropTable cmdDropTable(DbSchemaObjectName tableName) {
	    return new GenericDbCommands.GenericDropTable(this, tableName);
    }
	
	@Override
    public CreateColumn cmdCreateColumn(DbSchemaObjectName tableName, DbColumn column)  {
	    return new GenericDbCommands.GenericCreateColumn(this,tableName,column);
    }
	
	@Override
    public CreatePrimaryKey cmdCreatePrimaryKey(DbSchemaObjectName tableName, DbPrimaryKey pk) {
	    return new GenericDbCommands.GenericCreatePrimaryKey(this, tableName, pk);
	}

	@Override
    public DropPrimaryKey cmdDropPrimaryKey(DbSchemaObjectName tableName) {
	    return new GenericDbCommands.GenericDropPrimaryKey(this, tableName);
    }

	@Override
    public DropColumn cmdDropColumn(DbSchemaObjectName tableName, String columnName) {
	    return new GenericDbCommands.GenericDropColumn(this, tableName, columnName);
    }

	@Override
    public CreateForeignKey cmdCreateForeignKey(DbSchemaObjectName tableName, DbForeignKey fk) {
	    return new GenericDbCommands.GenericCreateForeignKey(this, tableName, fk);
    }

	@Override
    public DropForeignKey cmdDropForeignKey(DbSchemaObjectName tableName, String fkName) {
	    return new GenericDbCommands.GenericDropForeignKey(this, tableName, fkName);
    }

	@Override
    public CreateIndex cmdCreateIndex(DbSchemaObjectName tableName, DbIndex index) {
	    return new GenericDbCommands.GenericCreateIndex(this, tableName, index);
    }

	@Override
    public DropIndex cmdDropIndex(DbSchemaObjectName tableName, String ixName) {
	    return new GenericDbCommands.GenericDropIndex(this, tableName, ixName);
    }
	
	@Override
    public CreateSequence cmdCreateSequence(DbSequence sequence) {
	    return new GenericDbCommands.GenericCreateSequence(this, sequence);
    }

	@Override
    public DropSequence cmdDropSequence(DbSchemaObjectName sequenceName) {
	    return new GenericDbCommands.GenericDropSequence(this, sequenceName);
    }
	
	@Override
    public DropSchema cmdDropSchema(String schemaName) {
	    return new GenericDbCommands.GenericDropSchema(this, metadata.getSchema(schemaName));
    }

    public RenameColumn cmdRenameColumn(DbSchemaObjectName tableName, String columnName, String renameTo) {
	    return new GenericDbCommands.GenericRenameColumn(this, tableName, columnName, renameTo);
    }

	@Override
    public DbExecution createExecution() {
	    return new GenericDbExecution(this);
    }
	
	@Override
    public void execute(ConnectionCallback callback) throws NestedSQLException {
		Transactions.execute(dataSource, callback);
    }

	@Override
    public <T> T execute(ConnectionCallbackWithResult<T> callback) throws NestedSQLException {
		return Transactions.execute(dataSource, callback);
    }
	
	@Override
    public int executeUpdate(String sql) throws NestedSQLException {
	    return executeUpdate(sql,Arrays2.EMPTY_OBJECT_ARRAY);
    }

	@Override
    public int executeUpdate(String sql, Object[] args) throws NestedSQLException {
		Connection connection = null;
		try{
			connection = getConnection();
			return executeUpdate(connection, sql, args);
		}finally{
			closeConnection(connection);
		}
    }
	
	@Override
    public int executeUpdate(String sql, Object[] args, int[] types) throws NestedSQLException {
		Connection connection = null;
		try{
			connection = getConnection();
			return executeUpdate(connection, sql, args, types);
		}finally{
			closeConnection(connection);
		}
    }
	
    @Override
    @SuppressWarnings("unchecked")
    public int executeUpdate(String sql, Object[] args, int[] types, PreparedStatementHandler<?> handler) throws NestedSQLException {
		Connection connection = null;
		try{
			connection = getConnection();
			return executeUpdate(connection, sql, args, types,(PreparedStatementHandler<Db>)handler);
		}finally{
			closeConnection(connection);
		}
    }

	@Override
    public int executeUpdate(Connection connection, String sql, Object[] args) throws NestedSQLException {
		Args.notNull(connection, "connection");
		Args.notEmpty(sql,"sql");
		return doExecuteUpdate(connection, sql, args, null, null);
    }

	@Override
    public int executeUpdate(Connection connection, String sql, Object[] args, int[] types) throws NestedSQLException {
		Args.notNull(connection, "connection");
		Args.notEmpty(sql,"sql");
		Args.notNull(args);
		Args.notNull(types);
		Args.assertTrue(args.length == types.length || types.length == 0,"args length must equals to types length");
		
		return doExecuteUpdate(connection, sql, args, types, null);
    }
	
	@Override
    public int executeUpdate(Connection connection, String sql, Object[] args, int[] types, PreparedStatementHandler<Db> handler) throws NestedSQLException {
		Args.notNull(connection, "connection");
		Args.notEmpty(sql,"sql");
		Args.notNull(args);
		Args.notNull(types);
		Args.assertTrue(args.length == types.length || types.length == 0,"args length must equals to types length");
		
		return doExecuteUpdate(connection, sql, args, types, handler);
    }
	
	@Override
    public int[] executeBatchUpdate(String... sqls) throws NestedSQLException {
		if(null == sqls || sqls.length == 0) {
			return Arrays2.EMPTY_INT_ARRAY;
		}

		Connection connection = null;
		try{
			connection = getConnection();

			Statement stmt = connection.createStatement();
			
			for(String sql : sqls) {
				if(Strings.isEmpty(sql)) {
					throw  new IllegalArgumentException("Sql content must not be empty in the sql array");	
				}
				stmt.addBatch(sql);
			}
			
			return stmt.executeBatch();
		}catch(SQLException e){
			throw Exceptions.wrap(e);
		}finally{
			closeConnection(connection);
		}
    }

	@Override
    public int[] executeBatchUpdate(String sql, Object[][] batchArgs) throws NestedSQLException {
		Connection connection = null;
		try{
			connection = getConnection();
			return doExecuteBatchUpdate(connection, sql, batchArgs, null, null);	
		}finally{
			closeConnection(connection);
		}
    }

	@Override
    public int[] executeBatchUpdate(String sql, Object[][] batchArgs, int[] types) throws NestedSQLException {
		Connection connection = null;
		try{
			connection = getConnection();
			return doExecuteBatchUpdate(connection, sql, batchArgs, types, null);	
		}finally{
			closeConnection(connection);
		}
    }
	
    @Override
    @SuppressWarnings("unchecked")
    public int[] executeBatchUpdate(String sql, Object[][] batchArgs, int[] types, BatchPreparedStatementHandler<?> handler) throws NestedSQLException {
		Connection connection = null;
		try{
			connection = getConnection();
			return doExecuteBatchUpdate(connection, sql, batchArgs, types, (BatchPreparedStatementHandler<Db>)handler);	
		}finally{
			closeConnection(connection);
		}
    }

	@Override
    public <T> T executeQuery(String sql, ResultSetReader<T> reader) throws NestedSQLException {
		Connection connection = null;
		try{
			connection = getConnection();
			return executeQuery(connection, sql, Arrays2.EMPTY_OBJECT_ARRAY, reader);
		}finally{
			closeConnection(connection);
		}
    }

	@Override
    public <T> T executeQuery(String sql, Object[] args, ResultSetReader<T> reader) throws NestedSQLException {
		Connection connection = null;
		try{
			connection = getConnection();
			return executeQuery(connection, sql, args, Arrays2.EMPTY_INT_ARRAY, reader);
		}finally{
			closeConnection(connection);
		}
    }

	@Override
    public <T> T executeQuery(String sql, Object[] args, int[] types, ResultSetReader<T> reader) throws NestedSQLException {
		Connection connection = null;
		try{
			connection = getConnection();
			return executeQuery(connection, sql, args, types, reader);
		}finally{
			closeConnection(connection);
		}
    }

	@Override
    public <T> T executeQuery(Connection connection, String sql, Object[] args, ResultSetReader<T> reader) throws NestedSQLException {
		Args.notNull(connection,"connection");
		Args.notEmpty(sql,"sql");
		Args.notNull(reader);
		return doExecuteQuery(connection, sql, args, Arrays2.EMPTY_INT_ARRAY, reader);
    }

	@Override
    public <T> T executeQuery(Connection connection, String sql, Object[] args, int[] types, ResultSetReader<T> reader) throws NestedSQLException {
		Args.notNull(connection,"connection");
		Args.notEmpty(sql,"sql");
		Args.notNull(args,"args");
		Args.notNull(types,"types");
		Args.assertTrue(args.length == types.length || types.length == 0,"args length must equals to types length");
		Args.notNull(reader);
		return doExecuteQuery(connection, sql, args, types, reader);
    }

	protected int doExecuteUpdate(Connection connection, String sql, Object[] args, int[] types, PreparedStatementHandler<Db> handler) throws NestedSQLException {
		PreparedStatement ps = null;
		
		try{
			if(log.isDebugEnabled()){
				log.debug("Executing Sql Update -> \n\n SQL  : {}\n ARGS : {}\n",sql,getDisplayString(args, types));
			}
			
			StopWatch sw = StopWatch.startNew();
			
			if(null != handler){
				ps = handler.preparedStatement(this, connection, sql);
			}
			
			if(null == ps){
				ps = dialect.createPreparedStatement(connection, sql);
			}

			if(null != args && args.length > 0) {
				if(null == handler || !handler.setParameters(this, connection, ps, args, types)) {
					if(null != types && types.length > 0){
						for(int i=0;i<args.length;i++){
							dialect.setParameter(ps, i+1, args[i],types[i]);
						}
					}else{
						for(int i=0;i<args.length;i++){
							dialect.setParameter(ps, i+1, args[i]);
						}
					}
				}
			}
			
			if(null != handler){
				handler.preExecuteUpdate(this, connection, ps);				
			}
			
			int result = ps.executeUpdate();
			
			log.debug("Sql Executed in {}ms, {} row(s) affected",sw.getElapsedMilliseconds(),result); 
			
			if(null != handler){
				handler.postExecuteUpdate(this, connection, ps, result);
			}
			
			return result;
		}catch(SQLException e){
			throw new NestedSQLException(e);
		}finally{
			JDBC.closeStatementOnly(ps);
		}
	}
	
	protected <T> T doExecuteQuery(Connection connection, String sql, Object[] args, int[] types,ResultSetReader<T> reader) throws NestedSQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			if(log.isDebugEnabled()){
				log.debug("Executing Sql Update -> \n\n SQL  : {}\n ARGS : {}\n",sql,getDisplayString(args, types));
			}
			
			StopWatch sw = StopWatch.startNew();
			
			ps = dialect.createPreparedStatement(connection, sql);

			if(null != args){
				if(null != types && types.length > 0){
					for(int i=0;i<args.length;i++){
						dialect.setParameter(ps, i+1, args[i],types[i]);
					}
				}else{
					for(int i=0;i<args.length;i++){
						dialect.setParameter(ps, i+1, args[i]);
					}
				}
			}
			
			rs = ps.executeQuery();
			
			log.debug("Sql Executed in {}ms",sw.getElapsedMilliseconds());
			
			return reader.read(rs);
		}catch(SQLException e){
			throw new NestedSQLException(e);
		}finally{
			JDBC.closeResultSetOnly(rs);
			JDBC.closeStatementOnly(ps);
		}
	}

	protected int[] doExecuteBatchUpdate(Connection connection, String sql, Object[][] batchArgs, int[] types, BatchPreparedStatementHandler<Db> handler) throws NestedSQLException {
		PreparedStatement ps = null;
		
		try{
			StopWatch sw = StopWatch.startNew();
			
			if(log.isDebugEnabled()) {
				log.debug("Executing Batch Sql Update -> \n\n SQL  : {}\n ARGS : {}",sql,getDisplayString(batchArgs, types));
			}
			
			if(null != handler){
				ps = handler.preparedStatement(this, connection, sql, batchArgs, types);
			}
			
			if(null == ps){
				ps = dialect.createPreparedStatement(connection, sql);
			}
			
			for(int j=0;j<batchArgs.length;j++){
				Object[] args  = batchArgs[j];
				
				if(null == handler || !handler.setBatchParameters(this, connection, ps, args, types, j)) {
					if(null != types && types.length > 0){
						for(int i=0;i<args.length;i++){
							dialect.setParameter(ps, i+1, args[i],types[i]);
						}
					}else{
						for(int i=0;i<args.length;i++){
							dialect.setParameter(ps, i+1, args[i]);
						}
					}
				}
				
				ps.addBatch();
			}
			
			if(null != handler){
				handler.preExecuteBatchUpdate(this, connection, ps);				
			}
			
			int[] result = ps.executeBatch();
			
			log.debug("Sql Batch Executed in {}ms",sw.getElapsedMilliseconds()); 
			
			if(null != handler){
				handler.ostExecuteBatchUpdate(this, connection, ps, result);
			}
			
			return result;
		}catch(SQLException e){
			throw new NestedSQLException(e);
		}finally{
			JDBC.closeStatementOnly(ps);
		}
	}
	
	protected GenericSchemaChanges createSchemaChanges() {
		return new GenericSchemaChanges(this);
	}
	
	protected String getDisplayString(Object[] args, int[] types){
		if(args.length == 0){
			return "[]";
		}
		
		StringBuilder sb = new StringBuilder().append('[');
		
		for(int i=0;i<args.length;i++){
			if(i > 0){
				sb.append(',');
			}
			sb.append(dialect.toDisplayString(types != null && types.length > 0 ? types[i] : JdbcTypes.UNKNOW_TYPE_CODE, args[i]));
		}
		
		sb.append(']');
		return sb.toString();
	}
	
	protected String getDisplayString(Object[][] batchArgs, int[] types){
		if(batchArgs.length == 0){
			return "[]";
		}
		
		StringBuilder sb = new StringBuilder();
		
		for(Object[] args : batchArgs){
			sb.append(getDisplayString(args, types)).append("\n        ");
		}
		
		return sb.toString();
	}
}