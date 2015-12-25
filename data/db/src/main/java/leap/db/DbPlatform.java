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
import java.util.List;

import javax.sql.DataSource;

import leap.lang.meta.MNamedWithDesc;

public interface DbPlatform extends MNamedWithDesc {
	
	/**
	 * Returns an array contains all {@link DbDriver} of this db platform. 
	 */
	DbDriver[] getDrivers();
	
	/**
	 * Returns a new created {@link List} contains all the {@link DbDriver} of this db platform.
	 * 
	 * <p>
	 * If the given parameter avaiable is <code>true</code>, it will returns the avaiable drivers only.
	 * 
	 * @see DbDriver#isAvailable()
	 */
	List<DbDriver> getDrivers(boolean available);
	
	/**
	 * Try create {@link Db} instance by {@link Connection} and {@link DatabaseMetaData}.
	 * 
	 * @return create a new {@link Db} instance if supports, returns <code>null</code> if not supported.
	 */
	Db tryCreateDbInstance(String name,DataSource dataSource,Connection connection,DatabaseMetaData meta) throws SQLException;

}