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
import leap.web.App;
import leap.web.AppListener;
import leap.web.config.WebConfig;
import leap.web.security.SecurityConfigurator;


@Configurable(prefix = "oauth2")
public class OAuth2ResServerAutoConfig implements AppListener {

    private @Inject OAuth2ResServerConfigurator oc;
    private @Inject SecurityConfigurator        sc;

    private Boolean enabled;
    private String  serverUrl;
    private String  clientId;
    private String  clientSecret;

    @ConfigProperty
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @ConfigProperty
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    @ConfigProperty
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @ConfigProperty
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Override
    public void postAppConfigure(App app, WebConfig c) throws Throwable {
        if(null == enabled) {
            return;
        }

        if(enabled) {
            sc.setEnabled(true);
            oc.setEnabled(true);

            oc.useRemoteAuthorizationServer();
            oc.setRemoteTokenInfoEndpointUrl(Paths.suffixWithSlash(serverUrl + "/oauth2/tokeninfo"));
            oc.setResourceServerId(clientId);
            oc.setResourceServerSecret(clientSecret);
        }else{
            sc.setEnabled(false);
            oc.setEnabled(false);
        }
    }
}
