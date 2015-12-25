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

import java.util.Map;

import leap.core.ds.DataSourceConfig;
import leap.lang.accessor.PropertyAccessor;
import leap.lang.reflect.Reflection;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;

/**
 * A context object used in {@link AppConfigProcessor}.
 */
public interface AppConfigContext extends PropertyAccessor {
	
	String getProfile();
	
	boolean isDefaultOverried();
	
	Object getExternalContext();
	
	<T> T getExtension(Class<T> type);
	
	default <T> T getOrCreateExtension(Class<T> type) {
		T e = getExtension(type);
		if(null == e) {
			e = Reflection.newInstance(type);
			setExtension(e);
		}
		return e;
	}
	
	<T> void setExtension(T extension);
	
	<T> void setExtension(Class<T> type, T extension);
	
	void addResource(Resource r);
	
	void addResources(ResourceSet rs);
	
	boolean hasDefaultDataSourceConfig();
	
	boolean hasDataSourceConfig(String name);
	
	void setDataSourceConfig(String name,DataSourceConfig conf);
	
	Map<String,DataSourceConfig> getDataSourceConfigs();
}