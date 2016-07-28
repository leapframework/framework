/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.web.api.config;

import leap.core.AppConfig;
import leap.core.AppConfigException;
import leap.core.AppResource;
import leap.core.AppResources;
import leap.core.annotation.Inject;
import leap.lang.resource.Resource;
import leap.orm.sql.SqlConfigException;
import leap.web.App;
import leap.web.api.Apis;

import java.util.HashSet;
import java.util.Set;

public class DefaultApiConfigSource implements ApiConfigSource{

    protected @Inject ApiConfigReader[] readers;

    @Override
    public void loadConfiguration(App app, Apis apis) {
        AppResource resources[] = AppResources.get(app.config()).search("apis");

        LoadContext context = new LoadContext(app);

        for(int i=0;i<resources.length;i++){
            AppResource ar = resources[i];
            Resource resource = ar.getResource();

            if(resource.isReadable() && resource.exists()){
                try {
                    String resourceUrl = resource.getURL().toString();

                    if (context.getResourceUrls().contains(resourceUrl)) {
                        throw new AppConfigException("Cycle importing detected, please check your config : " + resourceUrl);
                    }

                    context.getResourceUrls().add(resourceUrl);

                    for (ApiConfigReader reader : readers) {
                        if (reader.readConfiguration(apis, context, resource)) {
                            break;
                        }
                    }

                }catch (ApiConfigException e) {
                    throw e;
                }catch(Exception e){
                    throw new ApiConfigException("Error loading apis from 'classpath:" + resource.getClasspath() + "', msg : " + e.getMessage(),e);
                }
            }
        }
    }

    protected static final class LoadContext implements ApiConfigReaderContext {

        private final App app;
        private final Set<String> resourceUrls = new HashSet<>();

        public LoadContext(App app) {
            this.app = app;
        }

        @Override
        public AppConfig getAppConfig() {
            return app.config();
        }

        public Set<String> getResourceUrls() {
            return resourceUrls;
        }

    }
}
