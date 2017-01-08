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

package leap.web.api.config;

import leap.web.api.spec.swagger.SwaggerConstants;

/**
 * Created by kael on 2016/10/31.
 */
public class OauthConfig {
    private boolean oauthEnabled;
    private String flow;
    private String oauthAuthzEndpointUrl;
    private String oauthTokenEndpointUrl;

    public OauthConfig(boolean oauthEnabled,String oAuthAuthzEndpointUrl, String oAuthTokenEndpointUrl) {
        this(oauthEnabled, SwaggerConstants.IMPLICIT,oAuthAuthzEndpointUrl,oAuthTokenEndpointUrl);
    }
    
    public OauthConfig(boolean oauthEnabled,String flow, String oAuthAuthzEndpointUrl, String oAuthTokenEndpointUrl) {
        this.oauthEnabled = oauthEnabled;
        this.flow = flow;
        this.oauthAuthzEndpointUrl = oAuthAuthzEndpointUrl;
        this.oauthTokenEndpointUrl = oAuthTokenEndpointUrl;
    }

    public boolean isOauthEnabled() {
        return oauthEnabled;
    }

    public void setOauthEnabled(boolean oauthEnabled) {
        this.oauthEnabled = oauthEnabled;
    }

    public String getOauthAuthzEndpointUrl() {
        return oauthAuthzEndpointUrl;
    }

    public void setOauthAuthzEndpointUrl(String oauthAuthzEndpointUrl) {
        this.oauthAuthzEndpointUrl = oauthAuthzEndpointUrl;
    }

    public String getOauthTokenEndpointUrl() {
        return oauthTokenEndpointUrl;
    }

    public void setOauthTokenEndpointUrl(String oauthTokenEndpointUrl) {
        this.oauthTokenEndpointUrl = oauthTokenEndpointUrl;
    }

    public String getFlow() {
        return flow;
    }

    public void setFlow(String flow) {
        this.flow = flow;
    }
}
