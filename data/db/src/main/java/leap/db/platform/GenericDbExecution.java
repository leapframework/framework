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

import leap.db.DbError;
import leap.db.DbErrors;
import leap.db.DbExecution;
import leap.lang.Args;
import leap.lang.Assert;
import leap.lang.exception.NestedSQLException;
import leap.lang.jdbc.JDBC;
import leap.lang.logging.Log;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GenericDbExecution implements DbExecution {
	
	protected final Log log;

	protected final GenericDb                db;
	protected final List<GenericDbStatement> statements = new ArrayList<>();
	private   final List<GenericDbStatement> statementsImmutableView = Collections.unmodifiableList(statements);
	
	protected boolean throwExceptionWhileExecuting = true;
	
	private boolean continueOnError = false;
	private int     nrOfSuccesses   = -1;
	private int     nrOfErrors      = -1;
	private int     nrOfExecuted    = -1;
	private Boolean success			= null; 
	private boolean	refreshSchema	= true;
	
	protected GenericDbExecution(GenericDb db){
		this.db  = db;
		this.log = db.log();
	}
	
	@Override
    public DbExecution setThrowExceptionOnExecuting(boolean throwExceptionWhileExecuting) {
		this.throwExceptionWhileExecuting = throwExceptionWhileExecuting;
	    return this;
    }

	@Override
    public DbExecution add(String sql) {
		Args.notEmpty(sql,"sql statement");
		statements.add(new GenericDbStatement(sql));
	    return this;
    }

	@Override
    public DbExecution addAll(String... sqls) {
		for(String sql : sqls){
			add(sql);
		}
	    return this;
    }

	@Override
    public DbExecution addAll(Collection<String> sqls) {
		for(String sql : sqls){
			add(sql);
		}
	    return this;
    }

	@Override
    public DbExecution setContinueOnError(boolean continueOnError) {
		this.continueOnError = continueOnError;
	    return this;
    }

	@Override
    public DbExecution setRefreshSchema(boolean refreshSchema) {
		this.refreshSchema = refreshSchema;
	    return this;
    }

	@Override
    public List<GenericDbStatement> statements() {
	    return statementsImmutableView;
    }
	
	@Override
    public boolean executed() {
	    return success != null;
    }
	
	@Override
    public boolean success() throws IllegalStateException {
		Assert.isTrue(null != success,"execution must be executed for getting the 'success' state");
	    return success;
    }

	@Override
    public List<DbError> errors() throws IllegalStateException {
		List<DbError> errors = new ArrayList<>();
		
		for(GenericDbStatement statement : statements){
			if(statement.isError()){
				errors.add(statement.error());
			}
		}
		
	    return errors;
    }
	
	@Override
    public List<String> sqls() {
		List<String> sqls = new ArrayList<String>();
		
		for(GenericDbStatement statement : statements){
			sqls.add(statement.sql());
		}
		
	    return sqls;
    }
	
	@Override
    public int numberOfStatements() {
	    return statements.size();
    }

	@Override
    public int numberOfSuccesses() {
	    return nrOfSuccesses;
    }

	@Override
    public int numberOfErrors() {
	    return nrOfErrors;
    }
	
	@Override
    public int numberOfExecuted() {
	    return nrOfExecuted;
    }
	
	@Override
    public boolean execute() {
		return db.executeWithResult(this::execute);
    }

    @Override
    @SuppressWarnings("resource")
    public synchronized boolean execute(Connection connection) {
		Args.notNull(connection,"connection");
		Assert.isTrue(nrOfExecuted == -1,"this execution already executed");
		Statement stmt = null;
		
		nrOfErrors    = 0;
		nrOfSuccesses = 0;
		nrOfExecuted  = 0;
		
		try{
			stmt = connection.createStatement();
            stmt.setEscapeProcessing(false);
			
			log.info("Executing {} statement(s)...",statements.size());
			
			for(int i=0;i<statements.size();i++){
				GenericDbStatement dbStatement = statements.get(i);
				
				String sql = dbStatement.sql();
				
				nrOfExecuted++;
				
				log.trace("Executing [{}] statement : \n\n{}\n ", nrOfExecuted, sql);

				try{
					int affected = stmt.executeUpdate(sql);
					
					log.trace("Statement [{}] success, {} rows(s) affected", nrOfExecuted, affected);
					
					nrOfSuccesses++;
					dbStatement.success(affected);
				}catch(SQLException e){
					nrOfErrors++;
					
					//TODO : resolve error
					dbStatement.error(new DbError(DbErrors.UNRESOLVED, e.getMessage(),e));
					
					if(throwExceptionWhileExecuting){
                        log.error("Statement [{}] failed, error : {}  \n  SQL -> \n {}",nrOfExecuted,e.getMessage(),sql, e);
						throw new NestedSQLException(e);
					}else{
                        log.warn("Statement [{}] failed, error : {}  \n  SQL -> \n {}",nrOfExecuted,e.getMessage(),sql, e);
                    }
					
					if(!continueOnError){
						break;
					}
				}
			}
			
			log.info("All executed : {} executed, {} successes, {} errors",nrOfExecuted,nrOfSuccesses,nrOfErrors);
		}catch(SQLException e){
			throw new NestedSQLException("Error executing this execution : " + e.getMessage(), e);
		}finally{
			JDBC.closeStatementOnly(stmt);
		}
		
		this.success = nrOfSuccesses == statements.size();
		
		if(refreshSchema && nrOfSuccesses > 0){
			log.debug("Refresh the metadata");
			db.getMetadata().refresh();
		}
		
	    return false;
    }
}