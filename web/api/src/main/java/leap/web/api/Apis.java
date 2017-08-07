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
package leap.web.api;

import leap.lang.exception.ObjectExistsException;
import leap.lang.exception.ObjectNotFoundException;
import leap.web.api.config.ApiConfig;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.meta.ApiMetadata;

import java.util.Set;

/**
 * Contains all the api configurators.
 */
public interface Apis {

    /**
     * Creates a new {@link ApiConfigurator}, do not register it.
     */
    ApiConfigurator newConfigurator(String name, String basePath);

    /**
     * Returns the {@link ApiMetadata} of the api or null if not exists.
     */
    ApiMetadata tryGetMetadata(String name);

    /**
     * Creates a new api configurator and register it.
     *
     * @param name     the name of api.
     * @param basePath the base path of api, must starts with a slash '/'.
     * @throws ObjectExistsException if the given name already exists.
     */
    ApiConfigurator add(String name, String basePath) throws ObjectExistsException;

    /**
     * Removes the api.
     */
    boolean remove(String name);

    /**
     * Creates the api.
     */
    void create(ApiConfigurator c);

    /**
     * Returns <code>true</code> if default enabled.
     * <p>
     * <p>
     * Default is <code>false</code>.
     */
    boolean isDefaultOAuthEnabled();

    /**
     * Sets all the apis default enable oauth2.
     */
    Apis setDefaultOAuthEnabled(boolean enabled);

    /**
     * Optional. Returns the default oauth2 authorization endpoint url.
     */
    String getDefaultOAuthAuthorizationUrl();

    /**
     * Sets the default oauth2 implicit authorization url for all apis.
     */
    Apis setDefaultOAuthAuthorizationUrl(String url);

    /**
     * Sets the oauth2 authorization url by endpoint and standard query parameters.
     */
    Apis setDefaultOAuthAuthorizationUrl(String endpoint, String clientId, String redirectUri);

    /**
     * Optional. Returns the default oauth2 token endpoint url.
     */
    String getDefaultOAuthTokenUrl();

    /**
     * Sets the default oauth2 token url for all apis.
     */
    Apis setDefaultOAuthTokenUrl(String url);

}