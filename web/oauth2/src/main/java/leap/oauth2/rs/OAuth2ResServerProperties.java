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

package leap.oauth2.rs;

import leap.core.annotation.ConfigProperty;
import leap.core.annotation.Configurable;
import leap.core.annotation.Inject;
import leap.lang.path.Paths;

@Configurable(prefix = "oauth2")
public class OAuth2ResServerProperties {

    private @Inject OAuth2ResServerConfigurator oc;

    @ConfigProperty
    public void setEnabled(boolean enabled) {
        oc.setEnabled(true);
    }

    @ConfigProperty
    public void setServerUrl(String serverUrl) {
        oc.setRemoteTokenInfoEndpointUrl(Paths.suffixWithSlash(serverUrl) + "/oauth2/tokeninfo");
    }

    @ConfigProperty
    public void setClientId(String clientId) {
        oc.setResourceServerId(clientId);
    }

    @ConfigProperty
    public void setClientSecret(String clientSecret) {
        oc.setResourceServerSecret(clientSecret);
    }

}