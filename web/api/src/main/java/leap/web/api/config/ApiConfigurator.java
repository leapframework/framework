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

import leap.lang.Extensible;
import leap.lang.http.MimeTypes;
import leap.lang.meta.MComplexType;
import leap.lang.naming.NamingStyle;
import leap.web.api.Api;
import leap.web.api.config.model.ModelConfig;
import leap.web.api.config.model.OAuthConfig;
import leap.web.api.config.model.ParamConfig;
import leap.web.api.config.model.RestdConfig;
import leap.web.api.meta.ApiMetadataBuilder;
import leap.web.api.meta.model.*;
import leap.web.api.mvc.ApiFailureHandler;
import leap.web.route.Route;
import leap.web.route.Routes;


public interface ApiConfigurator extends Extensible {

    String[] DEFAULT_PRODUCES = new String[]{MimeTypes.APPLICATION_JSON};
    String[] DEFAULT_CONSUMES = new String[]{MimeTypes.APPLICATION_JSON};

    String DEFAULT_VERSION                = "1.0";
    int    MAX_PAGE_SIZE                  = 1000;
    int    DEFAULT_PAGE_SIZE              = 50;
    int    MAX_PAGE_SIZE_WITH_EXPAND_MANY = 100;
    int    MAX_PAGE_SIZE_WITH_EXPAND_ONE  = 1000;
    int    MAX_RECORDS_PER_EXPAND         = 1000;

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
     * Adds a model config.
     */
    ApiConfigurator addModelConfig(ModelConfig mc);

    /**
     * Adds a model
     */
    ApiConfigurator addModel(MApiModelBuilder model);

    /**
     * Adds a complex type.
     */
    ApiConfigurator addComplexType(MComplexType ct);

    /**
     * Adds a parameter config.
     */
    ApiConfigurator addParam(ParamConfig param);

    /**
     * Puts a common response.
     * <p>
     * <p/>
     * see {@link ApiConfig#getCommonResponses()}.
     */
    ApiConfigurator putCommonResponse(String name, MApiResponse response);

    /**
     * Puts a common response builder for build common response.
     * <p>
     * <p/>
     * see {@link ApiConfig#getCommonResponses()}.
     */
    ApiConfigurator putCommonResponseBuilder(String name, MApiResponseBuilder response);

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
     * Sets the max expand.
     */
    ApiConfigurator setMaxPageSizeWithExpand(int size);

    /**
     * Sets the expand limit.
     */
    ApiConfigurator setMaxRecordsPerExpand(int limit);

    /**
     * Sets all the api operations to default anonymous or not.
     */
    ApiConfigurator setDefaultAnonymous(boolean anonymous);

    /**
     * Disables cors of this api.
     * <p>
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
    ApiConfigurator enableOAuth();

    /**
     * Sets oauth config
     */
    ApiConfigurator setOAuthConfig(OAuthConfig c);

    /**
     * Adds a new or updates an exists permission.
     */
    ApiConfigurator setPermission(MApiPermission p);

    /**
     * Adds a new permission if not exists.
     * <p>
     * <p/>
     * Do nothing if not exists.
     */
    ApiConfigurator tryAddPermission(MApiPermission p);

    /**
     * Adds an operation route in this api.
     * <p>
     * <p/>
     * The permissions defined in the route will be added automatically.
     */
    ApiConfigurator addRoute(Route route);

    /**
     * Adds a dynamic route in this api.
     * <p>
     * <p/>
     * The permissions defined in the route will be added automatically.
     */
    default ApiConfigurator addDynamicRoute(Route route) {
        return addDynamicRoute(route, true);
    }

    /**
     * Adds a dynamic route in this api.
     *
     * <p/>
     * The permissions defined in the route will be added automatically.
     */
    ApiConfigurator addDynamicRoute(Route route, boolean isOperation);

    /**
     * Sets base package of this api
     */
    ApiConfigurator setBasePackage(String basePackage);

    /**
     * Sets the resource type of the route.
     */
    ApiConfigurator setResourceType(Route route, Class<?> resourceType);

    /**
     * Sets the unique operation id property to true or false.
     */
    ApiConfigurator setUniqueOperationId(boolean unqiueOperationId);

    /**
     * Sets the {@link RestdConfig}.
     */
    ApiConfigurator setRestdConfig(RestdConfig c);

    /**
     * Sets the {@link Routes} for added api routes..
     */
    ApiConfigurator setContainerRoutes(Routes routes);

    /**
     * Enables restd.
     */
    ApiConfigurator enableRestd();

    /**
     * Returns the {@link RestdConfig} or null if restd is not enabled.
     */
    RestdConfig getRestdConfig();

    /**
     * Optional.
     */
    ApiMetadataBuilder getMetadata();

    /**
     * Sets {@link ApiMetadataBuilder}.
     */
    void setMetadata(ApiMetadataBuilder metadata);

    /**
     * Sets {@link ApiFailureHandler}.
     */
    void setFailureHandler(ApiFailureHandler failureHandler);
}