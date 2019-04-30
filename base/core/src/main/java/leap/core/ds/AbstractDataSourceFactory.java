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

import leap.lang.Classes;
import leap.lang.Strings;
import leap.lang.exception.NestedClassNotFoundException;

public abstract class AbstractDataSourceFactory implements DataSourceFactory {

	protected Class<?> tryGetDriverClass(DataSourceProps conf) throws NestedClassNotFoundException {
		if(!Strings.isEmpty(conf.getDriverClassName())){
			return Classes.forName(conf.getDriverClassName());
		}
		return null;
	}
	
	protected Class<?> ensureGetDriverClass(DataSourceProps conf) throws NestedClassNotFoundException,IllegalArgumentException {
		Class<?> c = tryGetDriverClass(conf);
		if(null == c){
			throw new IllegalArgumentException("DataSource property '" + DataSourceProps.DRIVER_CLASS_NAME + "' must not be empty");
		}
		return c;
	}
	
	protected String ensureGetProperty(DataSourceProps conf, String name) throws IllegalArgumentException {
		String v = conf.getProperty(name);
		if(Strings.isEmpty(v)){
			throw new IllegalArgumentException("DataSource property '" + name + "' must not be empty");
		}
		return v;
	}
	
}