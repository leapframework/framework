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
package leap.core.transaction;

import leap.core.AppContext;
import leap.core.BeanFactory;
import leap.core.ioc.BeanDefinition;
import leap.lang.Args;
import leap.lang.Exceptions;
import leap.lang.Strings;
import leap.lang.exception.NestedSQLException;
import leap.lang.jdbc.ConnectionCallback;
import leap.lang.jdbc.ConnectionCallbackWithResult;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

/**
 * Transaction management utils.
 */
public class Transactions {
	
	private static final Map<DataSource,TransactionProvider> managers = Collections.synchronizedMap(new WeakHashMap<>());
	
	public static void execute(DataSource dataSource, ConnectionCallback callback) throws NestedSQLException {
		Args.notNull(dataSource,"dataSource");
		Args.notNull(callback,"callback");
		
		TransactionProvider tm = getTransactionManager(dataSource);
		
		Connection connection = null;
		try{
			connection = tm.getConnection();
			callback.execute(connection);
		}catch(SQLException e){
			Exceptions.wrapAndThrow(e);
		}finally{
			tm.closeConnection(connection);
		}
	}
	
	public static <T> T execute(DataSource dataSource, ConnectionCallbackWithResult<T> callback) throws NestedSQLException {
		Args.notNull(dataSource,"dataSource");
		Args.notNull(callback,"callback");
		
		TransactionProvider tm = getTransactionManager(dataSource);
		
		Connection connection = null;
		try{
			connection = tm.getConnection();
			return callback.execute(connection);
		}catch(SQLException e){
			Exceptions.wrapAndThrow(e);
			return null;
		}finally{
			tm.closeConnection(connection);
		}
	}
	
	/**
	 * <font color="red">
	 * Be careful calling this method, the returned connection must be closed by calling {@link #closeConnection(Connection, DataSource)}.
	 * 
	 * <p>
	 * Recommends to use the {@link #execute(DataSource, ConnectionCallback)} and {@link #execute(DataSource, ConnectionCallbackWithResult)} methods.
	 * 
	 * </font>
	 */
	public static Connection getConnection(DataSource dataSource) throws NestedSQLException {
		Args.notNull(dataSource,"dataSource");
		return getTransactionManager(dataSource).getConnection();
	}
	
	public static boolean closeConnection(Connection connection,DataSource dataSource) {
		Args.notNull(connection,"connection");
		Args.notNull(dataSource,"dataSource");
		return getTransactionManager(dataSource).closeConnection(connection);
	}
	
	public static TransactionProvider getTransactionManager(DataSource dataSource) {
		TransactionProvider manager = managers.get(dataSource);
		
		if(null == manager){
			synchronized (managers) {
				BeanFactory factory = AppContext.factory();
				
				manager = tryGetTransactionManager(dataSource, factory);
				
				if(null == manager){
					manager = createTransactionManager(dataSource, factory);
				}
				
				managers.put(dataSource, manager);
	        }
		}
		
		return manager;
	}
	
	protected static TransactionProvider tryGetTransactionManager(DataSource dataSource, BeanFactory beanFactory) {
		Map<DataSource, BeanDefinition> dataSources = beanFactory.getBeansWithDefinition(DataSource.class);
		
		for(Entry<DataSource,BeanDefinition> entry : dataSources.entrySet()){
			if(entry.getKey() == dataSource || entry.getKey().equals(dataSource)){
				if(entry.getValue().isPrimary()){
					TransactionProvider tm = beanFactory.tryGetBean(TransactionProvider.class);
					if(null != tm){
						return tm;
					}
				}
				String name = entry.getValue().getName();
				if(!Strings.isEmpty(name)){
					return beanFactory.tryGetBean(TransactionProvider.class,name);
				}
				return null;
			}
		}
		
		return null;
	}
	
	protected static TransactionProvider createTransactionManager(DataSource dataSource, BeanFactory beanFactory){
		return beanFactory.getBean(TransactionProviderFactory.class).getTransactionProvider(dataSource);
	}

	protected Transactions(){
		
	}
}