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
package leap.core.ds.integration;

import javax.sql.DataSource;

import leap.core.BeanFactory;
import leap.core.ds.AbstractDataSourceFactory;
import leap.core.ds.DataSourceProps;
import leap.core.ioc.LoadableBean;
import leap.lang.Classes;
import leap.lang.exception.NestedClassNotFoundException;
import leap.lang.extension.ExProperties;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.time.StopWatch;

public class TomcatDataSourceFactory extends AbstractDataSourceFactory implements LoadableBean {
	private static final Log log = LogFactory.get(TomcatDataSourceFactory.class);
	
	private static final String TOMCAT_DATASOURCE_CLASSNAME = "org.apache.tomcat.jdbc.pool.DataSource";

	@Override
    public boolean load(BeanFactory factory) throws Exception {
		return Classes.isPresent(TOMCAT_DATASOURCE_CLASSNAME);
    }

	@Override
	public DataSource tryCreateDataSource(DataSourceProps props) throws NestedClassNotFoundException {
		StopWatch sw = StopWatch.startNew();
		
		ensureGetDriverClass(props);
		
		org.apache.tomcat.jdbc.pool.PoolProperties pp = new org.apache.tomcat.jdbc.pool.PoolProperties();
		
		pp.setDriverClassName(props.getDriverClassName());
		pp.setUrl(props.getJdbcUrl());
		pp.setUsername(props.getUsername());
		pp.setPassword(props.getPassword());
		pp.setDefaultAutoCommit(props.getDefaultAutoCommit());
		pp.setTestWhileIdle(true);
		pp.setTestOnBorrow(true);
		
		if(null != props.getDefaultTransactionIsolation()){
			pp.setDefaultTransactionIsolation(props.getDefaultTransactionIsolation().getValue());
		}
		
		//Injects other properties into the PoolProperties.
		props.inject(pp);
		
		if(!props.getExtPropertiesMap().isEmpty()){
			pp.setDbProperties(ExProperties.create(props.getExtPropertiesMap()));
		}
		
		org.apache.tomcat.jdbc.pool.DataSource ds = new org.apache.tomcat.jdbc.pool.DataSource();
		ds.setPoolProperties(pp);
		
		log.info("Create tomcat datasource for jdbc url : {} , used {}ms",pp.getUrl(),sw.getElapsedMilliseconds());
		
		return ds;
	}

	@Override
	public boolean tryDestroyDataSource(DataSource ds) {
		if(ds instanceof org.apache.tomcat.jdbc.pool.DataSource){
			org.apache.tomcat.jdbc.pool.DataSource tds = 
					((org.apache.tomcat.jdbc.pool.DataSource)ds);
			
			log.debug("Destroy tomcat datasource of url : " + tds.getUrl());
			
			try {
	            tds.close(true);
            } catch (Throwable e) {
            	log.warn("Error destroying tomcat jdbc pool, " + e.getMessage(), e);
            }
			
			return true;
		}
		
		return false;
	}

}