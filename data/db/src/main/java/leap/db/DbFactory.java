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
package leap.db;

import leap.core.AppContext;
import leap.core.ds.DataSourceManager;
import leap.db.exception.UnsupportedDbPlatformException;
import leap.db.platform.ansi.AnsiDbPlatform;
import leap.lang.Args;
import leap.lang.exception.NestedSQLException;
import leap.lang.exception.ObjectNotFoundException;
import leap.lang.jdbc.JDBC;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.net.Urls;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

public class DbFactory {

	private static final Log log = LogFactory.get(DbFactory.class);
	
	private static final String DB_CACHED_KEY  = DbFactory.class.getName() + "$APP_CONTEXT_CACHE";
	private static final Object DB_CACHED_LOCK = new Object();
	
	private static final AnsiDbPlatform ansiDbPlatform = new AnsiDbPlatform();
	
	/**
	 * Returns the cached {@link Db} instance for the default {@link DataSource} managed by {@link DataSourceManager}.
	 *
	 * @see DataSourceManager#getDefaultDataSource()
	 */
	public static Db getInstance() {
		DataSourceManager dsm = AppContext.factory().getBean(DataSourceManager.class);
		return getInstance(DataSourceManager.DEFAULT_DATASOURCE_NAME, dsm.getDefaultDataSource());
	}

	/**
	 * Returns the cached {@link Db} instance for the {@link DataSource} managed by {@link DataSourceManager}.
	 *
	 * @see DataSourceManager#getDataSource(String)
	 */
	public static Db getInstance(String name) throws NestedSQLException,UnsupportedDbPlatformException,ObjectNotFoundException {
		DataSourceManager dsm = AppContext.factory().getBean(DataSourceManager.class);
		return getInstance(name, dsm.getDataSource(name));
	}

	/**
	 * Returns the cached {@link Db} instance created for the given {@link DataSource}.
	 * 
	 * <p>
	 * All the created instances will be cached in current {@link AppContext}.
	 * 
	 * <p>
	 * A new {@link Db} instance will be created and cached if not cached in current {@link AppContext}.
	 */
	@SuppressWarnings("unchecked")
    public static Db getInstance(String name,DataSource ds) throws NestedSQLException,UnsupportedDbPlatformException {
		AppContext context = AppContext.current();
		Map<DataSource,Db> dbs = (Map<DataSource,Db>)context.getAttribute(DB_CACHED_KEY);
		
		Db db = null;
		
		if(null == dbs){
			dbs = Collections.synchronizedMap(new IdentityHashMap<>(2));
			context.setAttribute(DB_CACHED_KEY, dbs);
		}else{
			db = dbs.get(ds);
		}
		
		if(null == db){
			synchronized (DB_CACHED_LOCK) {
	            db = dbs.get(ds);
	            
	            if(null == db){
	            	db = createInstance(name,ds);
	            	dbs.put(ds, db);
	            }
            }
		}
		
		return db;
	}
	
	public static Db createInstance(DataSource ds) throws NestedSQLException,UnsupportedDbPlatformException {
		return createInstance(DataSourceManager.DEFAULT_DATASOURCE_NAME, ds);
	}
	
	/**
	 * Creates a new {@link Db} instance from the given {@link DataSource}.
	 * 
	 * @throws NestedSQLException if a {@link SQLException} caught.
	 * @throws UnsupportedDbPlatformException if the db not supported.
	 */
	public static Db createInstance(String name,DataSource ds) throws NestedSQLException,UnsupportedDbPlatformException {
		Args.notNull(ds,"datasource");
		
		Connection connection = null;
		
		try{
            log.debug("Fetching connection from dataSource '{}'...", ds);
			connection = ds.getConnection();
			DatabaseMetaData dm = connection.getMetaData();
			
			if(log.isInfoEnabled()){
				String url = Urls.removeQueryString(dm.getURL());
				log.info("Create db instance '{}', type: {} {},  url: {},  username: {}",name, dm.getDatabaseProductName(),dm.getDatabaseProductVersion(),url,dm.getUserName());
			}	
			
			Db db = null;
			
			for(DbPlatform platform : DbPlatforms.all()){
				if((db = platform.tryCreateDbInstance(name,ds,connection,dm)) != null){
					break;
				}
			}
			
			if(null == db) {
				db = ansiDbPlatform.tryCreateDbInstance(name, ds, connection, dm);
			}
			
			if(null == db){
				throw new UnsupportedDbPlatformException("Db platform '" + dm.getDatabaseProductName() + " " + dm.getDatabaseProductVersion() + "' not supported");	
			}

			return db;
		}catch(SQLException e){
			throw new NestedSQLException("Error creating Db instance : " + e.getMessage(),e);
		}finally{
			JDBC.closeConnection(connection);
		}
	}
}
