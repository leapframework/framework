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

import java.util.Map;
import java.util.Set;

import leap.lang.exception.ObjectExistsException;
import leap.lang.exception.ObjectNotFoundException;
import leap.web.api.config.ApiConfig;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.config.model.ModelConfig;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiResponse;

/**
 * Contains all the api configurators.
 */
public interface Apis {

	/**
	 * Returns an immutable {@link Set} contains all the {@link ApiConfigurator}.
	 */
	Set<ApiConfigurator> getConfigurators();
	
	/**
	 * Returns an immutable {@link Set} contains all the api configurations.
	 */
	Set<ApiConfig> getConfigurations();

    /**
     * Returns the configurator of the api.
     *
     * @param name the name of the api.
     *
     * @throws ObjectNotFoundException if no api configuration exists for the given name.
     */
    ApiConfigurator getConfigurator(String name) throws ObjectNotFoundException;

    /**
     * Returns the {@link ApiConfigurator} of the api or null if not exists.
     */
    ApiConfigurator tryGetConfigurator(String name);

    /**
     * Returns the {@link ApiMetadata} of the api or null if not exists.
     */
    ApiMetadata tryGetMetadata(String name);

	/**
	 * Creates an api configuration and returns the configurator.
	 * 
	 * @param name the name of api.
	 * @param basePath the base path of api, must starts with a slash '/'.
	 * 
	 * @throws ObjectExistsException if the given name aleady exists.
	 */
	ApiConfigurator add(String name, String basePath) throws ObjectExistsException;
	
	/**
	 * Returns <code>true</code> if default enabled.
	 * 
	 * <p>
	 * Default is <code>false</code>.
	 */
	boolean isDefaultOAuthEnabled();
	
	/**
	 * Optional. Returns the default oauth2 authorization endpoint url.
	 */
	String getDefaultOAuthAuthorizationUrl();
	
	/**
	 * Optional. Returns the default oauth2 token endpoint url.
	 */
	String getDefaultOAuthTokenUrl();

    /**
     * Returns a mutable {@link Map} contains all the common responses.
     */
    Map<String, MApiResponse> getCommonResponses();

    /**
     * Returns a mutable {@link Map} contains all the configurations of model types.
     */
    Map<Class<?>, ModelConfig> getCommonModelTypes();

	/**
	 * Sets all the apis default enable oauth2.
	 */
	Apis setDefaultOAuthEnabled(boolean enabled);
	
	/**
	 * Sets the default oauth2 implicit authorization url for all apis.
	 */
	Apis setDefaultOAuthAuthorizationUrl(String url);
	
	/**
	 * Sets the oauth2 authorization url by endpoint and standard query parameters.   
	 */
	Apis setDefaultOAuthAuthorizationUrl(String endpoint, String clientId, String redirectUri);
	
	/**
	 * Sets the default oauth2 token url for all apis. 
	 */
	Apis setDefaultOAuthTokenUrl(String url);

}