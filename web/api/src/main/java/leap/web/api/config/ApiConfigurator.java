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

import leap.lang.http.MimeTypes;
import leap.lang.naming.NamingStyle;
import leap.web.api.meta.model.MApiResponse;
import leap.web.api.meta.model.MPermission;
import leap.web.route.Route;


public interface ApiConfigurator {
	
	String[] DEFAULT_PRODUCES = new String[]{MimeTypes.APPLICATION_JSON};
	String[] DEFAULT_CONSUMES = new String[]{MimeTypes.APPLICATION_JSON};
	
	String   DEFAULT_VERSION   = "1.0";
    int      MAX_PAGE_SIZE     = 10000;
    int      DEFAULT_PAGE_SIZE = 50;
	
	/**
	 * Returns the configuration object.
	 */
	ApiConfig config();
	
	/**
	 * Sets the title of api.
	 */
	ApiConfigurator setTitle(String t);
	
	/**
	 * Sets the short description of api.
	 */
	ApiConfigurator setSummary(String s);
	
	/**
	 * Sets the long description of api.
	 */
	ApiConfigurator setDescription(String s);
	
	/**
	 * Sets the version string of api.
	 */
	ApiConfigurator setVersion(String v);
	
	/**
	 * Sets the Mime types can produce.
	 */
	ApiConfigurator setProduces(String... produces);

	/**
	 * Sets the Mime types can consumes.
	 */
	ApiConfigurator setConsumes(String... consumes);

	/**
	 * Sets the protocols.
	 */
	ApiConfigurator setProtocols(String... protocols);

    /**
     * Puts a common response.
     *
     * <p/>
     * see {@link ApiConfig#getCommonResponses()}.
     */
    ApiConfigurator putCommonResponse(String name, MApiResponse response);

	/**
	 * Sets the naming style of parameter names.
	 */
	ApiConfigurator setParameterNamingStyle(NamingStyle ns);

	/**
	 * Sets the naming style of property names.
	 */
	ApiConfigurator setPropertyNamingStyle(NamingStyle ns);

	/**
	 * Removes all the prefixes in model's name.
	 */
	ApiConfigurator removeModelNamePrefixes(String... prefixes);

    /**
     * Sets the max page size.
     */
    ApiConfigurator setMaxPageSize(int size);

    /**
     * Sets the default page size.
     */
    ApiConfigurator setDefaultPageSize(int size);

	/**
	 * Disables cors of this api.
	 *
	 * <p>
	 * Default cors is enabled.
	 */
	default ApiConfigurator disableCors() {
		return setCorsEnabled(false);
	}

	/**
	 * Enables or Disables cors.
	 */
	ApiConfigurator setCorsEnabled(boolean enabled);

	/**
	 * Enables OAuth authentication.
	 */
	default ApiConfigurator enableOAuth() {
		return setOAuthEnabled(true);
	}

	/**
	 * Enables or Disables oauth2 authentication.
	 *
	 * <p>
	 * Default is disabled.
	 */
	ApiConfigurator setOAuthEnabled(boolean enabled);

	/**
	 * Sets the url of authorization endpoint in oauth2 server.
	 */
	ApiConfigurator setOAuthAuthorizationUrl(String url);

	/**
	 * Sets the url of token endpoint in oauth2 server.
	 */
	ApiConfigurator setOAuthTokenUrl(String url);

    /**
     * Adds a new or updates an exists permission.
     */
    ApiConfigurator setPermission(MPermission p);

	/**
	 * Adds a route in this api.
     *
     * <p/>
     * The permissions defined in the route will be added automatically.
	 */
	ApiConfigurator addRoute(Route route);

    /**
     * Sets the resource type of the route.
     */
    ApiConfigurator setResourceType(Route route, Class<?> resourceType);
}