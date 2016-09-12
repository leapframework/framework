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

import leap.lang.exception.ObjectExistsException;
import leap.lang.exception.ObjectNotFoundException;
import leap.web.api.config.ApiConfig;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiResponse;
import leap.web.api.meta.model.MApiResponseBuilder;

/**
 * Contains all the api configurators.
 */
public interface Apis {

	/**
	 * Returns an immutable {@link Map} contains all the {@link ApiConfigurator}.
	 */
	Map<String, ApiConfigurator> configurators();
	
	/**
	 * Returns an immutable {@link Map} contains all the api configurations.
	 */
	Map<String, ApiConfig> configurations();
	
	/**
	 * Returns an immutable {@link Map} contains all the api metadatas.
     *
     * <p/>
     * The key is lower-case.
	 */
	Map<String, ApiMetadata> metadatas();
	
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
	 * Returns the configurator of the api.
	 * 
	 * @param name the name of the api.
	 * 
	 * @throws ObjectNotFoundException if no api configuration exists for the given name.
	 */
	ApiConfigurator of(String name) throws ObjectNotFoundException;
	
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