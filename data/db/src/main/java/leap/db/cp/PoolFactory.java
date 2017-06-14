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
package leap.db.cp;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import javax.naming.NamingException;
import javax.sql.DataSource;

import leap.lang.Beans;
import leap.lang.Classes;
import leap.lang.Props;
import leap.lang.Strings;
import leap.lang.beans.BeanType;
import leap.lang.jndi.JndiLocator;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.reflect.Reflection;

public class PoolFactory {
	private static final Log log = LogFactory.get(PoolFactory.class);
	
	private final PoolConfig poolConfig;
	private final DataSource dataSource;
	
	PoolFactory(PoolProperties props) {
		this.poolConfig = new PoolConfig(props);
		this.dataSource = createDataSource(props);
	}
	
	public PoolConfig getPoolConfig() {
		return poolConfig;
	}

	public DataSource getDataSource() {
		return dataSource;
	}
	
	public Connection getConnection() throws SQLException {
		log.debug("Getting underlying connection...");
		Connection connection = dataSource.getConnection();
        log.debug("Got a underlying connection '{}'", connection);
        return connection;
	}
	
	private DataSource createDataSource(PoolProperties props) {
		if(null != props.getDataSource()) {
			return props.getDataSource();
		}
		
		//DataSource class
		if(!Strings.isEmpty(props.getDataSourceClassName())) {
			return createDataSourceByClass(props);
		}
		
		//Jndi
		if(!Strings.isEmpty(props.getDataSourceJndiName())) {
			return createJndiDataSource(props);
		}
		
		//jdbc url
		return new DriverDataSource(props.getDriverClassName(), 
									props.getJdbcUrl(), 
									props.getUsername(), 
									props.getPassword(),
									props.getDataSourceProperties());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected DataSource createDataSourceByClass(PoolProperties props) {
		
		String className = props.getDataSourceClassName();
		
		DataSource ds = (DataSource)Reflection.newInstance(Classes.forName(className));
		
        Properties dataSourceProperties = props.getDataSourceProperties();
        if(null != dataSourceProperties && !dataSourceProperties.isEmpty()) {
        	Beans.setPropertiesNestable(BeanType.of(ds.getClass()), ds, (Map)Props.toMap(dataSourceProperties));
        }
		
		return ds;
	}
	
    protected DataSource createJndiDataSource(PoolProperties props) {
		try {
            DataSource ds =
            		new JndiLocator(props.isDataSourceJndiResourceRef())
            			.lookup(props.getDataSourceJndiName(), DataSource.class);

            return ds;
        } catch (NamingException e) {
        	throw new PoolException("Cannot create jndi datasource '" + props.getDataSourceJndiName() + "', " + e.getMessage() , e);
        }
	}
}