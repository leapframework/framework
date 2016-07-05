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

import leap.core.*;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.lang.Strings;
import leap.lang.io.FileChangeListenerAdaptor;
import leap.lang.io.FileChangeObserver;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.net.Urls;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ClasspathSqlSource implements SqlSource {
	
	private static final Log log = LogFactory.get(ClasspathSqlSource.class);

    protected @Inject @M AppConfig      config;
    protected @Inject @M AppFileMonitor fileMonitor;
    protected @Inject @M SqlReader[]    readers;
	
	@Override
    public void loadSqlCommands(SqlConfigContext context) throws SqlConfigException, SqlClauseException {
		//load all sqls

		AppResource resources[] = AppResources.get(config).search("sqls");

		loadSqls(context, resources);

		for(AppResource resource : resources){
			try {
				if(Urls.isFileUrl(resource.getResource().getURL())){
					fileMonitor.addObserver(new FileChangeObserver(resource.getResource().getFile()));
				}
			} catch (IOException e) {
				log.error(e);
				throw new RuntimeException(e);
			}
		}


		Resource dir = AppResources.getAppClasspathDirectory("sqls");
		if(dir.isFile()) {
            monitorSqls(context, dir.getFile());
		}
    }
	
	protected void loadSqls(SqlConfigContext context,AppResource[] resources){
		loadSqls(new LoadContext(context,false), resources);
	}
	
	protected void monitorSqls(SqlConfigContext context,File dir) {
		FileChangeObserver observer = new FileChangeObserver(dir);
		observer.addListener(new FileChangeListenerAdaptor(){
			@Override
            public void onFileCreate(FileChangeObserver observer, File file) {
				log.info("Sql file '" + file.getAbsolutePath() + "' was created, load it");
				Resource r = Resources.createFileResource(file);
				loadSqls(new LoadContext(context, true), new SimpleAppResource(r));
			}

			@Override
            public void onFileChange(FileChangeObserver observer, File file) {
				log.info("Sql file '" + file.getAbsolutePath() + "' was changed, reload it");
				Resource r = Resources.createFileResource(file);
				loadSqls(new LoadContext(context, true), new SimpleAppResource(r));
			}

			@Override
            public void onFileDelete(FileChangeObserver observer, File file) {
				log.info("Sql file '" + file.getAbsolutePath() + "' was deleted, do nothing");
			}
			
		});
		
		fileMonitor.addObserver(observer);
	}
	
	protected void loadSqls(SqlReaderContext context,AppResource... resources){
		for(int i=0;i<resources.length;i++){
            AppResource ar = resources[i];
			Resource resource = ar.getResource();
			
			if(resource.isReadable() && resource.exists()){
				try{
					String resourceUrl = resource.getURL().toString();
					
					if(context.getResourceUrls().contains(resourceUrl)){
						throw new AppConfigException("Cycle importing detected, please check your config : " + resourceUrl);
					}

					context.getResourceUrls().add(resourceUrl);

                    context.setDefaultOverride(ar.isDefaultOverride());

					for(SqlReader reader : readers) {
						if(reader.readSqlCommands(context, resource)) {
							break;
						}
					}

                    context.resetDefaultOverride();
				}catch(SqlConfigException e){
					throw e;
				}catch(Exception e){
					throw new SqlConfigException("Error loading sqls from 'classpath:" + resource.getClasspath() + "', msg : " + e.getMessage(),e);
				}
			}
		}
	}
	
	private final class LoadContext implements SqlReaderContext {
		private final Set<String> 	   resources = new HashSet<>();
		private final SqlConfigContext configContext;
        private final boolean          originalDefaultOverride;

		private boolean defaultOverride;
		
		private LoadContext(SqlConfigContext context, boolean defaultOverride){
			this.configContext   = context;
            this.originalDefaultOverride = defaultOverride;
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

        @Override
        public void setDefaultOverride(boolean defaultOverride) {
            this.defaultOverride = defaultOverride;
        }

        @Override
        public void resetDefaultOverride() {
            this.defaultOverride = originalDefaultOverride;
        }

        public boolean acceptDbType(String dbType){
			return Strings.isEmpty(dbType) || configContext.getDb().getType().equalsIgnoreCase(dbType);
		}
	}
}