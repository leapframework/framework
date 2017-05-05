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

package leap.web.api.config.model;

import leap.lang.Strings;

public class OAuthConfigImpl implements OAuthConfig {

    private Boolean enabled;
    private String  flow;
    private String  authorizationUrl;
    private String  tokenUrl;

    public OAuthConfigImpl() {

    }

    public OAuthConfigImpl(Boolean enabled, String flow, String authorizationUrl, String tokenUrl) {
        this.enabled = enabled;
        this.flow = flow;
        this.authorizationUrl = authorizationUrl;
        this.tokenUrl = tokenUrl;
    }

    @Override
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getFlow() {
        return flow;
    }

    public void setFlow(String flow) {
        this.flow = flow;
    }

    @Override
    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    public void setAuthorizationUrl(String url) {
        this.authorizationUrl = url;
    }

    @Override
    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String url) {
        this.tokenUrl = url;
    }

    public OAuthConfigImpl updateFrom(OAuthConfig config) {
        if (null == config) {
            return this;
        }

        if(null != config.getEnabled()) {
            this.enabled = config.getEnabled();
        }

        if (!Strings.isEmpty(config.getFlow())) {
            this.flow = config.getFlow();
        }

        if (!Strings.isEmpty(config.getAuthorizationUrl())) {
            this.authorizationUrl = config.getAuthorizationUrl();
        }

        if (!Strings.isEmpty(config.getTokenUrl())) {
            this.tokenUrl = config.getTokenUrl();
        }

        return this;
    }

    public OAuthConfigImpl tryUpdateFrom(OAuthConfig config) {
        if (null == config) {
            return this;
        }

        if(null == this.enabled) {
            this.enabled = config.getEnabled();
        }

        if (Strings.isEmpty(flow)) {
            this.flow = config.getFlow();
        }

        if (Strings.isEmpty(authorizationUrl)) {
            this.authorizationUrl = config.getAuthorizationUrl();
        }

        if (Strings.isEmpty(tokenUrl)) {
            this.tokenUrl = config.getTokenUrl();
        }

        return this;
    }
}
