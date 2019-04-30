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

import leap.core.jdbc.*;
import leap.db.*;
import leap.db.command.*;
import leap.db.model.*;
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

import javax.sql.DataSource;
import java.sql.*;

public class GenericDb extends DbBase {

    private static final ThreadLocal<DataSource> CONTEXT_DATA_SOURCE = new ThreadLocal<>();

    private boolean useContextDataSource;
	
	public GenericDb(String name, DataSource dataSource, DatabaseMetaData md,
                     DbPlatform platform,  DbMetadata metadata, GenericDbDialect dialect, DbComparator comparator) {
	    super(name, dataSource, md, platform, metadata, dialect, comparator);
    }
	
	public Log log(){
		return log;
	}

	@Override
    public GenericDbDialect getDialect() {
	    return (GenericDbDialect)super.getDialect();
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
    public CreateSchema cmdCreateSchema(DbSchema schema) {
        return new GenericDbCommands.GenericCreateSchema(this, schema);
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
	public void withDataSource(DataSource dataSource, Runnable func) {
        try {
            useContextDataSource = true;
            CONTEXT_DATA_SOURCE.set(dataSource);
            func.run();
        }finally {
            useContextDataSource = false;
            CONTEXT_DATA_SOURCE.remove();
        }
	}

	protected DataSource getContextDataSource() {
        return useContextDataSource ? CONTEXT_DATA_SOURCE.get() : null;
    }

	@Override
    public void execute(ConnectionCallback callback) throws NestedSQLException {
		try {
		    DataSource contextDataSource = getContextDataSource();

			if(null == contextDataSource && null != tp) {
                tp.execute(callback);
            }else{
                Connection conn = null;
                try {
                    conn = null != contextDataSource ? contextDataSource.getConnection() : dataSource.getConnection();

                    callback.execute(conn);
                }catch(SQLException e) {
                    throw new NestedSQLException(e, metadata.getProductName());
                }finally{
                    JDBC.closeConnection(conn);
                }
            }
		} finally {
			SqlExecutionContext.clean();
		}
	}

	@Override
    public <T> T executeWithResult(ConnectionCallbackWithResult<T> callback) throws NestedSQLException {
		try {
            DataSource contextDataSource = getContextDataSource();

			if(null == contextDataSource && null != tp) {
                return tp.executeWithResult(callback);
            }else{
                Connection conn = null;
                try {
                    conn = null != contextDataSource ? contextDataSource.getConnection() : dataSource.getConnection();

                    return callback.execute(conn);
                }catch(SQLException e) {
                    throw new NestedSQLException(e, metadata.getProductName());
                }finally{
                    JDBC.closeConnection(conn);
                }
            }
		} finally {
			SqlExecutionContext.clean();
		}
	}
	
	@Override
    public int executeUpdate(String sql) throws NestedSQLException {
	    return executeUpdate(sql,Arrays2.EMPTY_OBJECT_ARRAY);
    }

	@Override
    public int executeUpdate(final String sql, final Object[] args) throws NestedSQLException {
		SqlExecutionContext.setup(SqlExcutionType.Write, sql);
        int re = executeWithResult((conn) -> executeUpdate(conn, sql, args));
        return re;
    }
	
	@Override
    public int executeUpdate(String sql, Object[] args, int[] types) throws NestedSQLException {
		SqlExecutionContext.setup(SqlExcutionType.Write, sql);
        int re = executeWithResult((conn) -> executeUpdate(conn, sql, args, types));
        return  re;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int executeUpdate(String sql, Object[] args, int[] types, PreparedStatementHandler<?> handler) throws NestedSQLException {
		SqlExecutionContext.setup(SqlExcutionType.Write, sql);
        int re = executeWithResult((conn) -> executeUpdate(conn, sql, args, types,(PreparedStatementHandler<Db>)handler));
        return re;
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

		SqlExecutionContext.setup(SqlExcutionType.Write, Strings.join(sqls, ";"));

        return executeWithResult((conn) -> {
            Statement stmt = conn.createStatement();

            for(String sql : sqls) {
                if(Strings.isEmpty(sql)) {
                    throw  new IllegalArgumentException("Sql content must not be empty in the sql array");
                }
                stmt.addBatch(sql);
            }

            return stmt.executeBatch();
        });

    }

	@Override
    public int[] executeBatchUpdate(String sql, Object[][] batchArgs) throws NestedSQLException {
		SqlExecutionContext.setup(SqlExcutionType.Write, sql);
        int[] re = executeWithResult((conn) -> doExecuteBatchUpdate(conn, sql, batchArgs, null, null));
        return re;
    }

	@Override
    public int[] executeBatchUpdate(String sql, Object[][] batchArgs, int[] types) throws NestedSQLException {
		SqlExecutionContext.setup(SqlExcutionType.Write, sql);
		int[] re = executeWithResult((conn) -> doExecuteBatchUpdate(conn, sql, batchArgs, types, null));
		return re;
    }
	
    @Override
    @SuppressWarnings("unchecked")
    public int[] executeBatchUpdate(String sql, Object[][] batchArgs, int[] types, BatchPreparedStatementHandler<?> handler) throws NestedSQLException {
		SqlExecutionContext.setup(SqlExcutionType.Write, sql);
		int[] re = executeWithResult((conn) -> doExecuteBatchUpdate(conn, sql, batchArgs, types, (BatchPreparedStatementHandler<Db>)handler));
		return re;
    }

	@Override
    public <T> T executeQuery(String sql, ResultSetReader<T> reader) throws NestedSQLException {
		SqlExecutionContext.setup(SqlExcutionType.Read, sql);
		T t = executeWithResult((conn) -> executeQuery(conn, sql, Arrays2.EMPTY_OBJECT_ARRAY, reader));
		return t;

    }

	@Override
    public <T> T executeQuery(String sql, Object[] args, ResultSetReader<T> reader) throws NestedSQLException {
		SqlExecutionContext.setup(SqlExcutionType.Read, sql);
        T t = executeWithResult((conn) -> executeQuery(conn, sql, args, Arrays2.EMPTY_INT_ARRAY, reader));
        return t;
    }

	@Override
    public <T> T executeQuery(String sql, Object[] args, int[] types, ResultSetReader<T> reader) throws NestedSQLException {
		SqlExecutionContext.setup(SqlExcutionType.Read, sql);
        T t = executeWithResult((conn) -> executeQuery(conn, sql, args, types, reader));
        return t;
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
			throw new NestedSQLException(e, metadata.getProductName());
		}finally{
			JDBC.closeStatementOnly(ps);
		}
	}
	
	protected <T> T doExecuteQuery(Connection connection, String sql, Object[] args, int[] types,ResultSetReader<T> reader) throws NestedSQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			if(log.isDebugEnabled()){
				log.debug("Executing Sql Query -> \n\n SQL  : {}\n ARGS : {}\n",sql,getDisplayString(args, types));
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
			throw new NestedSQLException(e, metadata.getProductName());
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
						types = new int[args.length];
						for(int i=0;i<args.length;i++){
							types[i] = dialect.setParameter(ps, i+1, args[i]);
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
			throw new NestedSQLException(e, metadata.getProductName());
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
			sb.append(dialect.toDisplayString(types != null && types.length > 0 ? types[i] : JdbcTypes.UNKNOWN_TYPE_CODE, args[i]));
		}
		
		sb.append(']');
		return sb.toString();
	}
	
	protected String getDisplayString(Object[][] batchArgs, int[] types){
		if(batchArgs.length == 0){
			return "[]";
		}
		
		StringBuilder sb = new StringBuilder();

        for(int i=0;i<batchArgs.length;i++) {
            Object[] args = batchArgs[i];

            if(i == 10) {
                sb.append("[... " + (batchArgs.length - i) + " more]");
                break;
            }else{
                sb.append(getDisplayString(args, types)).append("\n        ");
            }

        }

		return sb.toString();
	}
}