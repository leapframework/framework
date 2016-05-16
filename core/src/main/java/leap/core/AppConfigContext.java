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
package leap.core;

import leap.core.ds.DataSourceConfig;
import leap.lang.accessor.PropertyAccessor;
import leap.lang.reflect.Reflection;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;

import java.util.Map;

/**
 * A context object used in {@link AppConfigProcessor}.
 */
public interface AppConfigContext extends PropertyAccessor {

    /**
     * Returns current profile's name.
     */
	String getProfile();

    /**
     * Returns true if the config property is overrided by default.
     */
	boolean isDefaultOverrided();

    /**
     * Returns the config extension or null if not exists.
     */
	<T> T getExtension(Class<T> type);

    /**
     * Returns the config extension of creates a new one if not exists.
     */
	default <T> T getOrCreateExtension(Class<T> type) {
		T e = getExtension(type);
		if(null == e) {
			e = Reflection.newInstance(type);
			setExtension(e);
		}
		return e;
	}

    /**
     * Sets the config extension.
     */
	<T> void setExtension(T extension);

    /**
     * Sets the config extension.
     */
	<T> void setExtension(Class<T> type, T extension);

    /**
     * Adds a managed resource.
     */
	void addResource(Resource r);

    /**
     * Adds a managed resource set.
     */
	void addResources(ResourceSet rs);

    /**
     * Returns true if the app already has a default configuration of data source.
     */
	boolean hasDefaultDataSourceConfig();

    /**
     * Returns true if the app already has a named configuration of data source.
     */
	boolean hasDataSourceConfig(String name);

    /**
     * Sets the configuration of data source.
     */
	void setDataSourceConfig(String name,DataSourceConfig conf);

    /**
     * Returns all the data source configurations.
     */
	Map<String,DataSourceConfig> getDataSourceConfigs();
}