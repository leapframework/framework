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
package leap.db;

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
import leap.lang.Assert;
import leap.lang.Lazy;

public class DbInit implements AppContextInitializable {
	
    private @Inject @M BeanFactory       factory;
    private @Inject @M DataSourceManager dsm;
	
	@Override
    public void postInit(AppContext context) throws Throwable {
		DataSource       defaultDataSource = dsm.tryGetDefaultDataSource();
		Map<String,DataSource> datasources = dsm.getAllDataSources();
		
		boolean foundPrimary = false;
		
		for(Entry<String,DataSource> entry : datasources.entrySet()){
			
			boolean primary = false;
			
			if(null != defaultDataSource && entry.getValue() == defaultDataSource){
				primary = true;
				foundPrimary = true;
			}
			
			initBeans(entry.getKey(), entry.getValue(), primary);
		}
		
		if(!foundPrimary && null != defaultDataSource){
			initBeans(DataSourceManager.DEFAULT_DATASOURCE_NAME, defaultDataSource, true);
		}
    }
	
	protected void initBeans(String name,DataSource ds,boolean primary) {
        if(ds instanceof Lazy) {
            return;
        }

		Db db = factory.tryGetBean(Db.class,name);
		if(null == db){
			db = DbFactory.getInstance(name,ds);
			factory.addBean(Db.class,db,name,primary);
		}
		Assert.isTrue(db == factory.getBean(Db.class,name));
	}

}