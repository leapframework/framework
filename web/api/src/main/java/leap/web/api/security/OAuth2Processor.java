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
package leap.web.api.security;

import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.oauth2.resource.OAuth2ResourceConfig;
import leap.oauth2.server.OAuth2ServerConfig;
import leap.oauth2.web.OAuth2WebConfig;
import leap.web.App;
import leap.web.api.config.ApiConfig;
import leap.web.api.config.ApiConfigProcessor;
import leap.web.api.config.ApiConfigurator;

public class OAuth2Processor implements ApiConfigProcessor {
    
    protected @Inject App                  app;
    protected @Inject OAuth2ServerConfig   asc;
    protected @Inject OAuth2ResourceConfig rsc;
    protected @Inject OAuth2WebConfig      owc;
    
    @Override
    public void preProcess(ApiConfigurator c) {
        //auto enable oauth if resource server is enabled.
        if(null != rsc && rsc.isEnabled()) {
            c.enabledOAuth();
            
            //TODO : scopes
        }
        
        ApiConfig conf = c.config();
        
        if(Strings.isEmpty(conf.getOAuthAuthorizationUrl()) &&
           Strings.isEmpty(conf.getOAuthTokenUrl())) {
            
            //auto set endpoint url if oauth2 client app is enabled locally.
            if(null != owc && owc.isEnabled()) {
                c.setOAuthAuthorizationUrl(owc.getRemoteAuthzEndpointUrl());
                c.setOAuthTokenUrl(owc.getRemoteTokenEndpointUrl());
                return;
            }

            //auto set endpoint url if authz server is enabled locally.
            if(null != asc && asc.isEnabled()) {
                //we cannot know the host name and port of local server.
                String contextPath = app.getContextPath();
                
                c.setOAuthAuthorizationUrl(contextPath + asc.getAuthzEndpointPath());
                c.setOAuthTokenUrl(contextPath + asc.getTokenEndpointPath());
            }
        }
    }
}
