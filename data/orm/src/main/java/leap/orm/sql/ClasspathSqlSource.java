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
package leap.orm.sql;

import leap.core.AppConfigException;
import leap.core.AppFileMonitor;
import leap.core.AppResources;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.lang.Strings;
import leap.lang.io.FileChangeListenerAdaptor;
import leap.lang.io.FileChangeObserver;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ClasspathSqlSource implements SqlSource {
	
	private static final Log log = LogFactory.get(ClasspathSqlSource.class);
	
    protected @Inject @M AppFileMonitor     fileMonitor;
    protected @Inject @M SqlReader[] readers;
	
	@Override
    public void loadSqlCommands(SqlConfigContext context) throws SqlConfigException, SqlClauseException {
		//load all sqls
		loadSqls(context, AppResources.getAllClasspathResourcesForXml("sqls"));
		
		Resource dir = AppResources.getAppClasspathDirectory("sqls");
		if(dir.isFile()) {
            monitorSqls(context, dir.getFile());
		}
    }
	
	protected void loadSqls(SqlConfigContext context,Resource[] resources){
		loadSqls(new LoadContext(context,false), resources);
	}
	
	protected void monitorSqls(SqlConfigContext context,File dir) {
		FileChangeObserver observer = new FileChangeObserver(dir);
		observer.addListener(new FileChangeListenerAdaptor(){
			@Override
            public void onFileCreate(FileChangeObserver observer, File file) {
				log.info("Sql file '" + file.getAbsolutePath() + "' was created, load it");
				Resource r = Resources.createFileResource(file);
				loadSqls(new LoadContext(context, true), r);
			}

			@Override
            public void onFileChange(FileChangeObserver observer, File file) {
				log.info("Sql file '" + file.getAbsolutePath() + "' was changed, reload it");
				Resource r = Resources.createFileResource(file);
				loadSqls(new LoadContext(context, true), r);
			}

			@Override
            public void onFileDelete(FileChangeObserver observer, File file) {
				log.info("Sql file '" + file.getAbsolutePath() + "' was deleted, do nothing");
			}
			
		});
		
		fileMonitor.addObserver(observer);
	}
	
	protected void loadSqls(SqlReaderContext context,Resource... resources){
		for(int i=0;i<resources.length;i++){
			Resource resource = resources[i];
			
			if(resource.isReadable() && resource.exists()){
				try{
					String resourceUrl = resource.getURL().toString();
					
					if(context.getResourceUrls().contains(resourceUrl)){
						throw new AppConfigException("Cycle importing detected, please check your config : " + resourceUrl);
					}

					context.getResourceUrls().add(resourceUrl);
					
					for(SqlReader reader : readers) {
						if(reader.readSqlCommands(context, resource)) {
							break;
						}
					}
				}catch(SqlConfigException e){
					throw e;
				}catch(Exception e){
					throw new SqlConfigException("Error loading sqls from 'classpath:" + resource.getClasspath() + "', msg : " + e.getMessage(),e);
				}
			}
		}
	}
	
	private final class LoadContext implements SqlReaderContext {
		private final Set<String> 	   resources = new HashSet<String>();
		private final SqlConfigContext configContext;
		private final boolean		   defaultOverride;
		
		private LoadContext(SqlConfigContext context, boolean defaultOverride){
			this.configContext   = context;
			this.defaultOverride = defaultOverride;
		}
		
		@Override
        public SqlConfigContext getConfigContext() {
	        return configContext;
        }

		@Override
        public Set<String> getResourceUrls() {
	        return resources;
        }

		@Override
        public boolean isDefaultOverride() {
	        return defaultOverride;
        }

		public boolean acceptDbType(String dbType){
			return Strings.isEmpty(dbType) || configContext.getDb().getType().equalsIgnoreCase(dbType);
		}
	}
}