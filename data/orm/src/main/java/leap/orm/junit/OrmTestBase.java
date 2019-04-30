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
package leap.orm.junit;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import leap.core.AppContext;
import leap.core.BeanFactory;
import leap.core.junit.AppTestBase;
import leap.db.Db;
import leap.db.DbFactory;
import leap.junit.contexual.ContextualProvider;
import leap.junit.contexual.ContextualRule;
import leap.lang.Confirm;
import leap.lang.Maps;
import leap.orm.Orm;
import leap.orm.OrmContext;
import leap.orm.OrmMetadata;
import leap.orm.dao.Dao;
import leap.orm.df.DataFactory;
import leap.orm.dmo.Dmo;
import leap.orm.domain.Domains;
import leap.orm.model.ModelRegistry;

import org.junit.Rule;
import org.junit.runner.Description;

public abstract class OrmTestBase extends AppTestBase {

	protected static final BeanFactory			  beanFactory;
	protected static final Map<String,DataSource> dataSources;
	protected static final Map<String,Dao>        daos;
	protected static final Dao					  defaultDao;
	protected static final Map<String,Dmo>        dmos;
	protected static final Dmo					  defaultDmo;
	protected static final Db				      defaultDb;
	protected static final Map<String,Db>         dbs;
	
	protected static final Set<String>			  contextualDatasources;
	
	static {
		beanFactory = AppContext.factory();
		
		dataSources = beanFactory.getNamedBeans(DataSource.class);
		
		DataSource defaultDs = beanFactory.getBean(DataSource.class);
		String defaultDbName = Maps.getFirstKey(dataSources,defaultDs);
		
		daos = new ConcurrentHashMap<String, Dao>(dataSources.size());
		for(String name : dataSources.keySet()){
			daos.put(name, Orm.dao(name));
		}
		defaultDao = null == defaultDbName ? beanFactory.getBean(Dao.class) : daos.get(defaultDbName);
		
		dmos = new ConcurrentHashMap<String, Dmo>(dataSources.size());
		for(String name : dataSources.keySet()){
			dmos.put(name, Orm.dmo(name));
		}
		defaultDmo = null == defaultDbName ? beanFactory.getBean(Dmo.class) : dmos.get(defaultDbName);
		
		dbs = new ConcurrentHashMap<String, Db>(dataSources.size());
		for(String name : dataSources.keySet()){
			dbs.put(name,DbFactory.getInstance(name, dataSources.get(name)));
		}
		defaultDb = defaultDao.getOrmContext().getDb();
		
		contextualDatasources = new HashSet<String>(dataSources.keySet());
	}
	
	protected OrmContext  context;
	protected OrmMetadata metadata;
	protected Domains	  domains;
	protected Dao         dao;
	protected Dmo         dmo;
	protected Db       	  db;
	protected DataFactory df;
	
	protected static void removeContextualDataSource(String name) {
		contextualDatasources.remove(name);
	}
	
	@Rule
	public final ContextualRule contextualRule = new ContextualRule(new ContextualProvider() {
		@Override
        public Iterable<String> names(Description description) {
	        return contextualDatasources;
        }

		@Override
        public void beforeTest(Description description, String name) throws Exception {
			dao = daos.get(name);
			dmo = dmos.get(name);
			db  = dbs.get(name);
			
			ModelRegistry.setThreadLocalContext(dao.getOrmContext());
        }

		@Override
        public void afterTest(Description description, String name) throws Exception {
	        ModelRegistry.removeThreadLocalContext();
        }

		@Override
        public void finishTests(Description description) throws Exception {
	        
        }
	});
	
	@Override
    protected final void setUp() throws Exception {
		if(null == dao){
			dao = defaultDao;
		}
		if(null == dmo){
			dmo = defaultDmo;
		}
		if(null == db){
			db = defaultDb;
		}
		
		this.context  = dao.getOrmContext();
		this.metadata = context.getMetadata();
		this.domains  = metadata.domains();
		this.df		  = dmo.getDataFactory();
		
		this.doSetUp();
    }
	
	protected void doSetUp() throws Exception {
		
	}
	
	protected void deleteAll(final Class<?> entityClass){
		Confirm.execute(() -> dao.deleteAll(entityClass));
	}
	
	protected static void upgradeSchemas(boolean enableDropTableObjects){
		for(Dmo dmo : dmos.values()){
			dmo.cmdUpgradeSchema().setDropTableObjectsEnabled(enableDropTableObjects).execute();
		}
		if(!dmos.containsValue(defaultDmo)){
			defaultDmo.cmdUpgradeSchema().setDropTableObjectsEnabled(enableDropTableObjects).execute();
		}
	}
}