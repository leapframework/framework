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

import leap.core.AppContext;
import leap.core.AppContextInitializable;
import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.ds.DataSourceListener;
import leap.core.ds.DataSourceManager;
import leap.lang.beans.BeanException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.orm.dao.Dao;
import leap.orm.dao.DefaultDao;
import leap.orm.dmo.DefaultDmo;
import leap.orm.dmo.Dmo;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class to initialize the default {@OrmContext}, {@link Dao} and {@link Dmo} beans.
 * 
 * <p>
 * 
 * This class must be configured in the beans configuration.
 */
public class OrmInit implements AppContextInitializable {

    private static final Log log = LogFactory.get(OrmInit.class);
	
    protected @Inject @M BeanFactory       beanFactory;
    protected @Inject @M DataSourceManager dataSourceManager;
    protected @Inject @M OrmRegistry       registry;

	@Override
    public void postInit(AppContext context) throws Exception {

        log.debug("Lookup all dataSource(s)...");

		DataSource       defaultDataSource = dataSourceManager.tryGetDefaultDataSource();
		Map<String,DataSource> dataSources = dataSourceManager.getAllDataSources();
		
		boolean foundPrimary = false;
		
		for(Entry<String,DataSource> entry : dataSources.entrySet()){
			
			boolean primary = false;
			
			if(null != defaultDataSource && entry.getValue() == defaultDataSource){
				primary = true;
				foundPrimary = true;
			}

            log.debug("Init orm beans for dataSource '{}'",  entry.getKey());

			initOrmBeans(beanFactory, entry.getValue(), entry.getKey(), primary);
		}
		
		if(!foundPrimary && null != defaultDataSource){
            log.debug("Init orm beans for default dataSource");
			initOrmBeans(beanFactory, defaultDataSource, Orm.DEFAULT_NAME, true);
		}

        //Force create default context first
        beanFactory.tryGetBean(OrmContext.class);

        //Register all contexts.
        beanFactory.getBeansWithDefinition(OrmContext.class).forEach((oc, bd) -> {
            registry.registerContext(oc, bd.isPrimary());
        });
    }
	
	private static void initOrmBeans(BeanFactory factory,DataSource dataSource,String name,boolean primary){
		OrmContext namedContext = factory.tryGetBean(OrmContext.class,name);
		if(null == namedContext){
			factory.addBean(OrmContext.class, primary, name, true, DefaultOrmContext.class, name, dataSource);
		}
	}

}