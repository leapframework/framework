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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.function.Function;

import javax.sql.DataSource;

import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.exception.NestedSQLException;
import leap.lang.meta.AbstractMNamedWithDesc;

public abstract class DbPlatformBase extends AbstractMNamedWithDesc implements DbPlatform {

	protected Function<DatabaseMetaData,Boolean> matcher;
	
	protected DbPlatformBase(String name){
		this(name,productNameContainsIgnorecaseMatcher(name));
	}
	
	protected DbPlatformBase(String name,Function<DatabaseMetaData,Boolean> matcher){
		Args.notEmpty(name,"name");
		Args.notNull(matcher,"matcher");
		
		this.name    = name;
		this.matcher = matcher;
	}
	
	@Override
    public Db tryCreateDbInstance(String name,DataSource ds,Connection connection, DatabaseMetaData meta) throws SQLException {
        if(matches(connection,meta)){
            return doTryCreateDbInstance(name,ds,connection,meta);
        }
        return null;
    }
	
    protected boolean matches(Connection connection,DatabaseMetaData metadata) throws SQLException {
    	try {
	        return matcher.apply(metadata);
        } catch (NestedSQLException e) {
        	throw e.getSQLException();
        }
    }
    
    protected abstract DbBase doTryCreateDbInstance(String name,DataSource ds,Connection connection,DatabaseMetaData metadata) throws SQLException;

	protected static Function<DatabaseMetaData,Boolean> productNameEqualsIgnorecaseMatcher(final String name){
		return new Function<DatabaseMetaData,Boolean>() {
			@Override
            public Boolean apply(DatabaseMetaData dm) {
	            try {
	                return dm.getDatabaseProductName().equalsIgnoreCase(name);
                } catch (SQLException e) {
	                throw new NestedSQLException(e);
                }
            }
		};
	}
	
	protected static Function<DatabaseMetaData,Boolean> productNameContainsIgnorecaseMatcher(final String name){
		return dm -> {
			try {
				return Strings.containsIgnoreCase(dm.getDatabaseProductName(), name);
			} catch (SQLException e) {
				throw new NestedSQLException(e);
			}
		};
	}
}
