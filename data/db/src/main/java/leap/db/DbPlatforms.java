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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import leap.core.AppContext;
import leap.lang.exception.ObjectNotFoundException;

public class DbPlatforms {
	
	private static final String CONTEXT_ATTRIBUTE_NAME = DbPlatforms.class.getName();
	
	protected static final List<DbPlatform> platforms = new CopyOnWriteArrayList<DbPlatform>();
	
    public static final String ORACLE     = "Oracle";
    public static final String SQLSERVER  = "Microsoft SQL Server";
    public static final String MARIADB	  = "MariaDB";
    public static final String MYSQL      = "MySql";
    public static final String POSTGRESQL = "PostgreSQL";
    public static final String H2         = "H2";
    public static final String DERBY	  = "Derby";
    public static final String DB2		  = "DB2";
    public static final String DM         = "DM";
    
    /**
     * Returns all the registered {@link DbPlatform} objects.
     */
    public static Iterable<DbPlatform> all(){
    	return currentPlatforms().values();
    }
    
    /**
     * Returns the {@link DbPlatform} matched the given db type name.
     * 
     * <p>
     * The type name is case sensitive.
     * 
     * @throws ObjectNotFoundException if the given type name not exists.
     */
    public static DbPlatform get(String name) throws ObjectNotFoundException {
    	DbPlatform p = tryGet(name);
    	if(null == p){
    		throw new ObjectNotFoundException("No db platform '" + name + "' exists");	
    	}
    	return p;
    }
    
    /**
     * Returns the {@link DbPlatform} matched the given db type name.
     * 
     * <p>
     * Returns <code>null</code> if the given db type name not found.
     * 
     * <p>
     * The type name is case sensitive.
     */
    public static DbPlatform tryGet(String name) throws ObjectNotFoundException {
    	if(null == name){
    		return null;
    	}
    	return currentPlatforms().get(name.toLowerCase());
    }

    /**
     * Checks is the db type aleady registered. the name compares ignore case.
     */
    public static boolean exists(String name){
    	return tryGet(name) != null;
    }

    private static Map<String,DbPlatform> currentPlatforms(){
    	return currentPlatforms(AppContext.current());
    }
    
    @SuppressWarnings("unchecked")
    private static Map<String,DbPlatform> currentPlatforms(AppContext context){
    	Map<String, DbPlatform> platforms = (Map<String, DbPlatform>)context.getAttribute(CONTEXT_ATTRIBUTE_NAME);
    	
    	if(null == platforms) {
    		platforms = new ConcurrentHashMap<String, DbPlatform>();
    		
    		for(DbPlatform p : context.getBeanFactory().getBeans(DbPlatform.class)) {
    			platforms.put(p.getName().toLowerCase(), p);
    		}
    		
    		context.setAttribute(CONTEXT_ATTRIBUTE_NAME, platforms);
    	}
    	
    	return platforms;
    }
    
    protected DbPlatforms(){
    	
    }
}