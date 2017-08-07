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

import leap.lang.Described;
import leap.lang.Extensible;
import leap.lang.Named;
import leap.lang.Titled;
import leap.lang.naming.NamingStyle;
import leap.web.App;
import leap.web.api.config.model.ModelConfig;
import leap.web.api.config.model.OAuthConfig;
import leap.web.api.config.model.ParamConfig;
import leap.web.api.config.model.RestdConfig;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiPermission;
import leap.web.api.meta.model.MApiResponse;
import leap.web.api.permission.ResourcePermissionsSet;
import leap.web.api.route.ApiRoute;
import leap.web.route.Route;
import leap.web.route.Routes;

import java.util.Map;
import java.util.Set;

/**
 * Represents the configuration of a web api.
 */
public interface ApiConfig extends Named,Titled,Described,Extensible {

	/**
	 * Returns config source.
	 */
	Object getSource();
	
	/**
	 * Base path of the api, starts with a slash '/'.
	 */
	String getBasePath();
	
	/**
	 * Returns the version string of this api.
	 */
	String getVersion();
	
	/**
	 * A list of MIME types the APIs can produce.
	 *
	 * <p>
	 * Default is {@link ApiConfigurator#DEFAULT_PRODUCES}.
	 *
	 * @see ApiMetadata#getProduces()
	 */
	String[] getProduces();

	/**
	 * A list of MIME types the APIs can consume.
	 *
	 * <p>
	 * Default is {@link ApiConfigurator#DEFAULT_CONSUMES}
	 *
	 * @see ApiMetadata#getConsumes()
	 */
	String[] getConsumes();

	/**
	 * Returns the protocols.
	 */
	String[] getProtocols();

	/**
	 * Returns <code>true</code> is <code>CORS</code> is enabled.
	 */
	default boolean isCorsEnabled() {
		return !isCorsDisabled();
	}

	/**
	 * Returns <code>true</code> if the <code>CORS</code> is disabled.
	 *
	 * <p>
	 * Default is enabled.
	 */
	boolean isCorsDisabled();

	/**
	 * Returns the permissions required by this api.
	 */
	Map<String,MApiPermission> getPermissions();

    /**
     * Returns an immutable {@link Map} contains all the common responses.
     */
    Map<String, MApiResponse> getCommonResponses();

    /**
     * Returns an immutable {@link Set} contains all the configurations of models.
     */
    Set<ModelConfig> getModels();

    /**
     * Returns the model config matches the name or null if not exists.
     */
    ModelConfig getModel(String name);

    /**
     * Returns the model config matches the class or null if not exists.
     */
    default ModelConfig getModel(Class<?> type) {
        return getModelByClassName(null == type ? null : type.getName());
    }

    /**
     * Returns the model config matches the class name or null if not exists.
     */
    ModelConfig getModelByClassName(String className);

    /**
     * Returns an immutable {@link Set} contains all the configurations of parameters.
     */
    Set<ParamConfig> getParams();

    /**
     * todo : doc
     */
    default ParamConfig getParam(String className) {
        return getParam(className, null);
    }

    /**
     * todo : doc
     */
    ParamConfig getParam(String className, String name);

    /**
     * todo : doc
     */
    default ParamConfig getParam(Class<?> type) {
        return getParam(type, null);
    }

    /**
     * todo : doc
     */
    default ParamConfig getParam(Class<?> type, String name) {
        return getParam(type.getName(), name);
    }

	/**
	 * Returns the naming style of parameter names, may be <code>null</code>.
	 */
	NamingStyle getParameterNamingStyle();

	/**
	 * Returns the naming style of property names, may be <coce>null</code>.
	 */
	NamingStyle getPropertyNamingStyle();

	/**
	 * Returns an immutable {@link Set} contains the prefixes will be removed from all the model names.
	 */
	Set<String> getRemovalModelNamePrefixes();

    /**
     * Returns the max page size of pagination.
     */
    int getMaxPageSize();

    /**
     * Returns the default page size of pagination.
     */
    int getDefaultPageSize();

	/**
	 * Returns the base package of this api.
	 */
	String getBasePackage();

    /**
     * Returns the if all the api operations are anonymous access by default.
     */
    boolean isDefaultAnonymous();

    /**
     * Returns the {@link Routes} for added the api route(s) to it.
     *
     * <p/>
     * Default is {@link App#routes()}.
     */
    Routes getContainerRoutes();

    /**
     * Returns all the routes in this api.
     */
    Set<ApiRoute> getApiRoutes();

    /**
     * Returns all the resource types of route.
     */
    Map<Route, Class<?>> getResourceTypes();

    /**
     * Returns the {@link ResourcePermissionsSet}.
     */
    ResourcePermissionsSet getResourcePermissionsSet();

    /**
     * Returns true if generated unique operation id for api operation.
     *
     * <p/>
     * Default is <code>false</code>.
     */
    boolean isUniqueOperationId();

    /**
     * Returns true if the oauth2 security is enabled.
     */
    default boolean isOAuthEnabled() {
        return null != getOAuthConfig() && getOAuthConfig().isEnabled();
    }

    /**
     * Returns true if the RESTful data api is enabled.
     */
    default boolean isRestdEnabled() {
        return null != getRestdConfig();
    }

    /**
     * Returns the oauth config
     */
    OAuthConfig getOAuthConfig();

    /**
     * Returns the {@link RestdConfig} or null if restd is not enabled.
     */
    RestdConfig getRestdConfig();


}