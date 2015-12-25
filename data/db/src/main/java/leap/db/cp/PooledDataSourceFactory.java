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

import java.util.Map.Entry;

import javax.sql.DataSource;

import leap.core.ds.AbstractDataSourceFactory;
import leap.core.ds.DataSourceConfig;
import leap.lang.beans.BeanType;
import leap.lang.exception.NestedClassNotFoundException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

public class PooledDataSourceFactory extends AbstractDataSourceFactory {
	
	private static final Log log = LogFactory.get(PooledDataSourceFactory.class);

	@Override
	public DataSource tryCreateDataSource(DataSourceConfig conf) throws NestedClassNotFoundException {
		if(!(conf.hasDataSourceClassName() || conf.hasDataSourceJndiName() || conf.hasDriverClassName())) {
			return null;
		}
		
		PoolProperties pp = new PoolProperties();
		
		pp.setDataSourceClassName(conf.getDataSourceClassName());
		pp.setDataSourceJndiName(conf.getDataSourceJndiName());
		
		if(conf.hasDataSourceJndiResourceRef()) {
			pp.setDataSourceJndiResourceRef(conf.getDataSourceJndiResourceRef());
		}
		
		pp.setDriverClassName(conf.getDriverClassName());
		pp.setJdbcUrl(conf.getJdbcUrl());
		pp.setUsername(conf.getUsername());
		pp.setPassword(conf.getPassword());
		
		if(conf.hasDefaultAutoCommit()) {
			pp.setDefaultAutoCommit(conf.getDefaultAutoCommit());
		}
		
		if(conf.hasDefaultReadOnly()) {
			pp.setDefaultReadonly(conf.getDefaultReadOnly());
		}
		
		if(conf.hasDefaultTransactionIsolation()) {
			pp.setDefaultTransactionIsolation(conf.getDefaultTransactionIsolation());
		}
		
		if(conf.hasDefaultCatalog()) {
			pp.setDefaultCatalog(conf.getDefaultCatalog());
		}
		
		if(conf.hasMaxActive()) {
			pp.setMaxActive(conf.getMaxActive());
		}
		
		if(conf.hasMaxIdle()) {
			pp.setMaxIdle(conf.getMaxIdle());
		}
		
		if(conf.hasMinIdle()) {
			pp.setMinIdle(conf.getMinIdle());
		}
		
		if(conf.hasMaxWait()) {
			pp.setMaxWait(conf.getMaxWait());
		}
		
		BeanType bt = BeanType.of(PoolProperties.class);
		
		for(Entry<String, String> entry : conf.getExtPropertiesMap().entrySet()) {
			String key = entry.getKey();
			String val = entry.getValue();
			
			if(bt.hasProperty(key)) {
				bt.setProperty(pp, key, val);
			}else{
				pp.setDataSourceProperty(key, val);
			}
		}
		
		return new PooledDataSource(pp);
	}

	@Override
	public boolean tryDestroyDataSource(DataSource ds) {
		if(ds instanceof PooledDataSource) {
			try {
	            ((PooledDataSource) ds).close();
            } catch (Exception e) {
            	log.error("Error close pooled datasource : {}",e.getMessage(), e);
            }
			return true;
		}
		return false;
	}

}
