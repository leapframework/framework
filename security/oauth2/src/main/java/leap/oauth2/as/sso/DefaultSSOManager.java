/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package leap.oauth2.as.sso;

import leap.core.annotation.Inject;
import leap.oauth2.as.AuthzAuthentication;
import leap.oauth2.as.OAuth2ServerConfig;
import leap.web.App;
import leap.web.AppInitializable;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.SecurityConfigurator;
import leap.web.security.SecurityInterceptor;

public class DefaultSSOManager implements SSOManager,AppInitializable,SecurityInterceptor {

    protected @Inject OAuth2ServerConfig   ac;
    protected @Inject SecurityConfigurator sc;

    @Override
    public void postAppInit(App app) throws Throwable {
        if(ac.isSingleLogoutEnabled()) {
            sc.interceptors().add(this);
        }
    }

    @Override
    public void onAuthenticated(Request request, Response response, AuthzAuthentication authc) throws Throwable {



    }

}