/*
 * Copyright 2015 the original author or authors.
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

import javax.naming.NamingException;
import javax.sql.DataSource;

import leap.core.AppConfigException;
import leap.lang.Strings;
import leap.lang.exception.NestedClassNotFoundException;
import leap.lang.jndi.JndiLocator;

public class JndiDataSourceFactory extends AbstractDataSourceFactory {
	
	@Override
    public DataSource tryCreateDataSource(DataSourceProps conf) throws NestedClassNotFoundException {
		String jndiName = conf.getDataSourceJndiName();
		
		if(Strings.isEmpty(jndiName)) {
			return null;
		}
		
		boolean resourceRef = null == conf.getDataSourceJndiResourceRef() ? false : conf.getDataSourceJndiResourceRef();

		try {
	        return new JndiLocator(resourceRef).lookup(jndiName, DataSource.class);
        } catch (NamingException e) {
        	throw new AppConfigException("Cannot loolup jndi DataSource '" + jndiName + "', " + e.getMessage(), e);
        }
    }

	@Override
    public boolean tryDestroyDataSource(DataSource ds) {
		//Do not need to destroy jndi datasource.
	    return false;
    }

}