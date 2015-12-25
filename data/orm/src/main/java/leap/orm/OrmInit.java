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
package leap.orm;

import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import leap.core.AppContext;
import leap.core.AppContextInitializable;
import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.ds.DataSourceListener;
import leap.core.ds.DataSourceManager;
import leap.orm.dao.Dao;
import leap.orm.dao.DefaultDao;
import leap.orm.dmo.DefaultDmo;
import leap.orm.dmo.Dmo;

/**
 * Class to initialize the default {@OrmContext}, {@link Dao} and {@link Dmo} beans.
 * 
 * <p>
 * 
 * This class must be configured in the beans configuration.
 */
public class OrmInit implements AppContextInitializable,DataSourceListener {
	
    protected @Inject @M BeanFactory       beanFactory;
    protected @Inject @M DataSourceManager dataSourceManager;
	
	@Override
    public void onDataSourceCreated(String name, DataSource ds) {
	    boolean primary = dataSourceManager.getDefaultDataSource() == ds;
	    if(primary){
	    	initOrmBeans(beanFactory, ds, Orm.DEFAULT_NAME, true);
	    }else{
	    	initOrmBeans(beanFactory, ds, name, false);
	    }
	    
		beanFactory.tryGetBean(OrmContext.class,name);
		beanFactory.tryGetBean(Dao.class,name);
		beanFactory.tryGetBean(Dmo.class,name);
    }

	@Override
    public void onDataSourceDestroyed(String name, DataSource ds) {
		//TODO : destroy orm beans
    }

	@Override
    public void postInit(AppContext context) throws Exception {
		dataSourceManager.addListener(this);
		
		DataSource       defaultDataSource = dataSourceManager.tryGetDefaultDataSource();
		Map<String,DataSource> datasources = dataSourceManager.getAllDataSources();
		
		boolean foundPrimary = false;
		
		for(Entry<String,DataSource> entry : datasources.entrySet()){
			
			boolean primary = false;
			
			if(null != defaultDataSource && entry.getValue() == defaultDataSource){
				primary = true;
				foundPrimary = true;
			}
			
			initOrmBeans(beanFactory, entry.getValue(), entry.getKey(), primary);
		}
		
		if(!foundPrimary && null != defaultDataSource){
			initOrmBeans(beanFactory, defaultDataSource, Orm.DEFAULT_NAME, true);
		}
		
		//Force create default beans first
		beanFactory.tryGetBean(OrmContext.class);
		beanFactory.tryGetBean(Dao.class);
		beanFactory.tryGetBean(Dmo.class);
		
		//Force create other beans later
		beanFactory.getBeans(OrmContext.class);
		beanFactory.getBeans(Dao.class);
		beanFactory.getBeans(Dmo.class);
    }
	
	private static void initOrmBeans(BeanFactory factory,DataSource dataSource,String name,boolean primary){
		OrmContext namedContext = factory.tryGetBean(OrmContext.class,name);
		if(null == namedContext){
			factory.addBean(OrmContext.class, primary, name, true, DefaultOrmContext.class, name, dataSource);
		}

		Dao namedDao = factory.tryGetBean(Dao.class,name);
		if(null == namedDao){
			factory.addBean(Dao.class, primary, name, true, DefaultDao.class, name);
		}
		
		Dmo namedDmo = factory.tryGetBean(Dmo.class,name);
		if(null == namedDmo){
			factory.addBean(Dmo.class, primary, name, true, DefaultDmo.class, name);
		}
	}
}