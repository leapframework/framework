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
package leap.web.api.config;

import leap.web.api.Apis;
import leap.web.api.config.model.OAuthConfig;

public class DefaultApiConfiguratorFactory implements ApiConfiguratorFactory {

	@Override
    public ApiConfigurator createConfigurator(Apis apis, String name, String basePath) {
	    DefaultApiConfig c = new DefaultApiConfig(name, basePath);

		c.setOAuthConfig(newOAuthConfig(apis));

        apis.getCommonModelTypes().forEach(c::putModelType);

	    return c;
    }

    protected OAuthConfig newOAuthConfig(Apis apis) {
        return new OAuthConfig(apis.isDefaultOAuthEnabled(),
                               apis.getDefaultOAuthAuthorizationUrl(),
                               apis.getDefaultOAuthTokenUrl());
    }
	
}