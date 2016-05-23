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
package leap.db;

import leap.core.AppContext;
import leap.core.ds.DataSourceManager;
import leap.core.junit.AppTestBase;
import leap.junit.contexual.ContextualProvider;
import leap.junit.contexual.ContextualRule;
import leap.lang.Confirm;
import leap.lang.Maps;
import org.junit.Rule;
import org.junit.runner.Description;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class DbTestCase extends AppTestBase {
	
	protected static final Map<String,DataSource> dataSources;
	protected static final Map<String,Db>         dbs;
	protected static final Db					  defaultDb;
	
	static {
	    DataSourceManager dsm = AppContext.factory().getBean(DataSourceManager.class);
	    
		dataSources = dsm.getAllDataSources();
		dbs         = new ConcurrentHashMap<>(dataSources.size());
		
		for(String name : dataSources.keySet()){
			dbs.put(name, DbFactory.getInstance(name,dataSources.get(name)));
		}
		
		DataSource defaultDs = dsm.getDefaultDataSource();
		String defaultDbName = Maps.getFirstKey(dataSources,defaultDs);
		defaultDb = dbs.get(defaultDbName);
		
		//drop all schema objects
		Confirm.execute(() -> {
            for(Db db : dbs.values()){
                db.cmdDropSchema(db.getMetadata().getDefaultSchemaName()).execute();
            }
		});
	}
	
	protected Db         db;
	protected DbMetadata metadata;
	protected DbDialect  dialect;
	
	@Rule
	public final ContextualRule contextualRule = new ContextualRule(new ContextualProvider() {
		@Override
        public Iterable<String> names(Description description) {
	        return dataSources.keySet();
        }

		@Override
        public void beforeTest(Description description, String name) throws Exception {
			db = dbs.get(name);
        }

		@Override
        public void afterTest(Description description, String name) throws Exception {
	        
        }

		@Override
        public void finishTests(Description description) throws Exception {
	        
        }
	}, true);

	@Override
    protected final void setUp() throws Exception {
		if(null == db){
			db = defaultDb;
		}
		metadata = db.getMetadata();
		dialect  = db.getDialect();
		
		this.doSetUp();
    }
	
	protected void doSetUp() throws Exception {
		
	}
}
