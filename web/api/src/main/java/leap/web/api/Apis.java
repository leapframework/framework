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
 * Manages all the registered api(s).
 */
public interface Apis {

    /**
     * Returns the registered {@link Api}.
     *
     * @throws ObjectNotFoundException if the api not exists.
     */
    Api get(String name) throws ObjectNotFoundException;

    /**
     * Returns the registered {@link Api} or null if not exists.
     */
    Api tryGet(String name);

    /**
     * Returns the {@link ApiMetadata} of the api or null if not exists.
     *
     * @throws IllegalStateException if the api has not been created.
     */
    default ApiMetadata tryGetMetadata(String name) throws IllegalStateException{
        Api api = tryGet(name);
        return null == api ? null : api.getMetadata();
    }

    /**
     * Removes the {@link Api}.
     */
    boolean remove(Api api);

    /**
     * Removes the api.
     */
    boolean remove(String name);

    /**
     * Register a {@link Api}.
     */
    void add(Api api) throws ObjectExistsException;

    /**
     * Creates a new api configurator and register it.
     *
     * @param name     the name of api.
     * @param basePath the base path of api, must starts with a slash '/'.
     * @throws ObjectExistsException if the given name already exists.
     */
    ApiConfigurator add(String name, String basePath) throws ObjectExistsException;

    /**
     * Returns a new dynamic {@link Api} (not register).
     */
    Api newDynamic(String name, String basePath);

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