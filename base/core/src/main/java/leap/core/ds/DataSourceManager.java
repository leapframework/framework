/*
 * Copyright 2014 the original author or authors.
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
package leap.core.ds;

import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import leap.lang.Listenable;
import leap.lang.exception.NestedClassNotFoundException;
import leap.lang.exception.ObjectExistsException;
import leap.lang.exception.ObjectNotFoundException;

public interface DataSourceManager extends Listenable<DataSourceListener> {
	
	String DEFAULT_DATASOURCE_NAME = "default";
	
	/**
	 * Returns <code>true</code> if there are managed datasource exists.
	 */
	boolean hasDataSources();
	
	/**
	 * Returns the default datasource.
	 * 
	 * @throws ObjectNotFoundException if no default datasource.
	 */
	DataSource getDefaultDataSource() throws ObjectNotFoundException;

	/**
	 * Returns the default datasource bean name.
	 * @throws ObjectNotFoundException
	 */
	String getDefaultDatasourceBeanName() throws ObjectNotFoundException;

	/**
	 * Returns the default datasource.
	 * 
	 * <p>
	 * Returns <code>null</code> if no default datasource.
	 */
	DataSource tryGetDefaultDataSource();
	
	/**
	 * Returns the {@link DataSource} with the given name in this manager.
	 * 
	 * @throws ObjectNotFoundException if the given name not found.
	 */
	DataSource getDataSource(String name) throws ObjectNotFoundException;
	
	/**
	 * Returns the {@link DataSource} with the given name in this manager.
	 * 
	 * <p>
	 * Returns <code>null</code> if the given name not exists.
	 */
	DataSource tryGetDataSource(String name);
	
	/**
	 * Returns an immutable {@link Map} contains all the defined {@link DataSource}.
	 * 
	 * <p>
	 * The key of the returned map is datasource's name.
	 */
	Map<String, DataSource> getAllDataSources();
	
	/**
	 * Creates a managed {@link DataSource} as default {@link DataSource} in this manager.
	 * 
	 * @throws ObjectExistsException if a default {@link DataSource} aleady exists in this manager.
	 * @throws UnsupportedOperationException if this manager does not supports the {@link DataSourceConfig}.
	 * @throws NestedClassNotFoundException if the given 'driverClassName' property not found.
	 */
	DataSource createDefaultDataSource(DataSourceConfig props) 
			throws ObjectExistsException,UnsupportedOperationException,NestedClassNotFoundException,SQLException;
	
	/**
	 * Creates a managed {@link DataSource} as a named {@link DataSource} in this manager.
	 * 
	 * @throws ObjectExistsException if a {@link DataSource} with the same name aleady exists in this manager.
	 * @throws UnsupportedOperationException if this manager does not supports the {@link DataSourceConfig}.
	 * @throws NestedClassNotFoundException if the given 'driverClassName' property not found.
	 */
	DataSource createDataSource(String name,DataSourceConfig props) 
			throws ObjectExistsException,UnsupportedOperationException,NestedClassNotFoundException,SQLException;
	
	/**
	 * Creates a non managed {@link DataSource} of the given properties.
	 * 
	 * @throws UnsupportedOperationException if this manager does not supports the given properties.
	 * @throws NestedClassNotFoundException if the 'driverClassName' is invalid.
	 */
	DataSource createDataSource(DataSourceConfig props) throws UnsupportedOperationException,SQLException;
	
	/**
	 * Try creates a non managed {@link DataSource} of the given properties.
	 * 
	 * <p>
	 * Returns <code>null</code> if this manager does not supports the given properties.
	 * 
	 * @throws NestedClassNotFoundException if the given 'driverClassName' property not found.
	 */
	DataSource tryCreateDataSource(DataSourceConfig props) throws NestedClassNotFoundException,SQLException;
	
	/**
	 * Destroy the given {@link DataSource}, that means close all the connections and release other resources created by the {@link DataSource}.
	 * 
	 * <p>
	 * If the given {@link DataSource} is managed by this manager, it will be removed.
	 * 
	 * @throws UnsupportedOperationException if this manager does not supports the {@link DataSource}.
	 */
	void destroyDataSource(DataSource ds) throws UnsupportedOperationException;
	
	/**
	 * Destroy the given {@link DataSource}, that means close all the connections and other resources created by the {@link DataSource}.
	 * 
	 * <p>
	 * If the given {@link DataSource} is managed by this manager, it will be removed.
	 * 
	 * <p>
	 * Returns <code>true</code> if the given {@link DataSource} success destroy, returns <code>false</code> otherwise.
	 */
	boolean tryDestroyDataSource(DataSource ds);
	
	/**
	 * Validates the given datasource is valid.
	 * 
	 * @throws SQLException if validation error.
	 */
	void validateDataSource(DataSource ds) throws SQLException;
	
	/**
	 * Validates the given datasource is valid.
	 * 
	 * <p>
	 * Returns <code>true</code> if valid, returns <code>false</code> otherwise.
	 */
	boolean tryValidateDataSource(DataSource ds);
}