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

/**
 * The configuration of OAuth2.
 */
public interface OAuthConfig {

    /**
     * Is OAuth2 security enabled explicitly.
     * @return
     */
    default boolean isEnabled() {
        return Boolean.TRUE.equals(getEnabled());
    }

    /**
     * Is OAuth2 security enabled, returns null if did not configured.
     */
    Boolean getEnabled();

    /**
     * Optional. Returns the OAuth2 flow.
     */
    String getFlow();

    /**
     * Required. Returns the authorization endpoint url of auth server.
     */
    String getAuthorizationUrl();

    /**
     * Required. Returns the token endpoint url of auth server.
     */
    String getTokenUrl();
}
