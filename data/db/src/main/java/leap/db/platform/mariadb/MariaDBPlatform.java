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
package leap.db.platform.mariadb;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.function.Function;

import leap.db.DbPlatforms;
import leap.db.platform.GenericDbDialect;
import leap.db.platform.GenericDbMetadataReader;
import leap.db.platform.GenericDbPlatform;
import leap.lang.Exceptions;
import leap.lang.Strings;

public class MariaDBPlatform extends GenericDbPlatform {
	
	private static final Function<DatabaseMetaData,Boolean> matcher = new Function<DatabaseMetaData,Boolean>() {
		@Override
		public Boolean apply(DatabaseMetaData dm) {
			try {
	           if(Strings.containsIgnoreCase(dm.getDatabaseProductName(), "mysql") && 
	              Strings.endsWith(dm.getDatabaseProductVersion(),"-MariaDB")) {
	        	   return true;
	           }
	           
	           if(Strings.containsIgnoreCase(dm.getDatabaseProductName(), "MariaDB")){
	        	   return true;
	           }
	           
	           return false;
            } catch (SQLException e) {
            	throw Exceptions.wrap(e);
            }
		}
	};
	
	public MariaDBPlatform() {
		super(DbPlatforms.MARIADB,matcher);
	}
	
	public MariaDBPlatform(String type) {
		super(type,matcher);
	}

	@Override
    protected GenericDbDialect createDialect(DatabaseMetaData jdbcMetadata) throws SQLException {
	    return new MariaDB10Dialect();
    }

	@Override
    protected GenericDbMetadataReader createMetadataReader(DatabaseMetaData jdbcMetadata) throws SQLException {
	    return new MariaDB10MetadataReader();
    }
}