/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.api.security;

import leap.core.annotation.Inject;
import leap.oauth2.rs.OAuth2ResServerConfig;
import leap.web.api.config.ApiConfigProcessor;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.config.model.OAuthConfigImpl;
import leap.web.api.spec.swagger.SwaggerConstants;

public class OAuth2ConfigProcessor implements ApiConfigProcessor {

    protected @Inject OAuth2ResServerConfig rsc;

    @Override
    public void preProcess(ApiConfigurator c) {
        if(rsc.isEnabled()) {
            OAuthConfigImpl ac = new OAuthConfigImpl();

            ac.setEnabled(true);
            ac.setTokenUrl(rsc.getTokenEndpointUrl());
            ac.setAuthorizationUrl(rsc.getAuthorizationEndpointUrl());
            ac.setFlow(SwaggerConstants.IMPLICIT);

            c.setOAuthConfig(ac);
        }
    }

}
