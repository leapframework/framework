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
import leap.oauth2.rs.OAuth2ResServerConfig;
import leap.oauth2.as.OAuth2AuthzServerConfig;
import leap.oauth2.wac.OAuth2WebAppConfig;
import leap.web.App;
import leap.web.api.config.ApiConfig;
import leap.web.api.config.ApiConfigProcessor;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.config.model.OAuthConfig;

public class OAuthConfigProcessor implements ApiConfigProcessor {
    
    protected @Inject App                     app;
    protected @Inject OAuth2AuthzServerConfig asc;
    protected @Inject OAuth2ResServerConfig   rsc;
    protected @Inject OAuth2WebAppConfig      owc;
    
    @Override
    public void preProcess(ApiConfigurator c) {
        //auto enable oauth if resource server is enabled.
        if(null != rsc && rsc.isEnabled()) {
            c.enableOAuth();
            
            //TODO : scopes
        }
        
        ApiConfig conf = c.config();
        OAuthConfig oauthConfig = conf.getOAuthConfig();
        if(oauthConfig == null ||
                (Strings.isEmpty(oauthConfig.getOauthAuthzEndpointUrl()) && Strings.isEmpty(oauthConfig.getOauthTokenEndpointUrl()))) {
            
            //auto set endpoint url if oauth2 client app is enabled locally.
            if(null != owc && owc.isEnabled()) {
                String authzUrl = owc.getServerAuthorizationEndpointUrl();
                String tokenUrl = owc.getServerTokenEndpointUrl();
                if(oauthConfig == null){
                    oauthConfig = new OAuthConfig(false, authzUrl,tokenUrl);
                    c.setOAuthConfig(oauthConfig);
                    return;
                }
                oauthConfig.setOauthAuthzEndpointUrl(authzUrl);
                oauthConfig.setOauthTokenEndpointUrl(tokenUrl);
                return;
            }

            //auto set endpoint url if authz server is enabled locally.
            if(null != asc && asc.isEnabled()) {
                //we cannot know the host name and port of local server.
                String contextPath = app.getContextPath();
                String authzUrl = contextPath + asc.getAuthzEndpointPath();
                String tokenUrl = contextPath + asc.getTokenEndpointPath();
                if(oauthConfig == null){
                    oauthConfig = new OAuthConfig(false,authzUrl,tokenUrl);
                    c.setOAuthConfig(oauthConfig);
                    return;
                }
                oauthConfig.setOauthAuthzEndpointUrl(authzUrl);
                oauthConfig.setOauthTokenEndpointUrl(tokenUrl);
            }
        }
    }
}
