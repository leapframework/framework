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
package leap.web.config;

import leap.web.FilterMappings;
import leap.web.error.ErrorCodes;
import leap.web.error.ErrorViews;
import leap.web.route.Routes;

import java.util.Set;

/**
 * The configurator of {@link WebConfig}.
 */
public interface WebConfigurator {

    /**
     * The prefix of config properties.
     */
	String CONFIG_PREFIX = "webmvc.";
	
	String CONFIG_DEFAULT_THEME          = CONFIG_PREFIX + "default-theme";
	String CONFIG_THEMES_LOCATION        = CONFIG_PREFIX + "themes-location";
	String CONFIG_VIEWS_LOCATION         = CONFIG_PREFIX + "views-location";
	String CONFIG_DEFAULT_FORMAT         = CONFIG_PREFIX + "default-format";
	String CONFIG_FORMAT_PARAMETER       = CONFIG_PREFIX + "format-parameter";
	String CONFIG_TRIM_PARAMETERS        = CONFIG_PREFIX + "trim-parameters";
	String CONFIG_ALLOW_FORMAT_EXTENSION = CONFIG_PREFIX + "allow-format-extension";
	String CONFIG_ALLOW_FORMAT_PARAMETER = CONFIG_PREFIX + "allow-format-parameter";
	String CONFIG_ALLOW_ACTION_EXTENSION = CONFIG_PREFIX + "allow-action-extension";
	String CONFIG_ACTION_EXTENSIONS      = CONFIG_PREFIX + "action-extensions";
	String CONFIG_CORS_ENABLED           = CONFIG_PREFIX + "cors-enabled";

	String DEFAULT_VIEWS_LOCATION    = "/WEB-INF/views";
	String DEFAULT_THEMES_LOCATION   = "/WEB-INF/themes";
	String DEFAULT_THEME_NAME        = "default";
	String DEFAULT_FORMAT_NAME       = "json";
	String DEFAULT_FORMAT_PARAMETER  = "$format";
	String DEFAULT_EVENT_PARAMETER   = "$event";
	String DEFAULT_JSESSIONID_PREFIX = ";jsessionid";

    /**
     * Returns the configuration.
     */
	WebConfig config();

    /**
     * Returns the {@link Routes} for configuring routes table.
     */
	Routes routes();

    /**
     * Returns the {@link ErrorViews} for configuring error views.
     */
	ErrorViews errorViews();

    /**
     * Returns the {@link ErrorCodes} for configuring error codes.
     */
	ErrorCodes errorCodes();

    /**
     * Returns the {@link FilterMappings} for configuring filter mappings.
     */
	FilterMappings filters();

    /**
     * Returns the {@link WebInterceptors} for configuring interceptors.
     */
	WebInterceptors interceptors();

    /**
     * Optional.
     *
     * Sets the name of default request/response data format.
     *
     * <p/>
     * Default is {@link #DEFAULT_FORMAT_NAME}.
     */
	WebConfigurator setDefaultFormatName(String name);

    /**
     * Optional.
     *
     * Sets the name of default theme.
     *
     * <p/>
     * Default is {@link #DEFAULT_THEME_NAME}.
     */
	WebConfigurator setDefaultThemeName(String name);

    /**
     * Optional.
     *
     * Sets the parameter name for resolving request/response data format.
     *
     * <p/>
     * Default is {@link #DEFAULT_FORMAT_PARAMETER}
     */
	WebConfigurator setFormatParameterName(String name);

    /**
     * Optional.
     *
     * Sets the prefix string of jsessionid value in request path.
     *
     * <p/>
     * Default is {@link #DEFAULT_JSESSIONID_PREFIX}.
     */
	WebConfigurator setJsessionidPrefix(String p);

    /**
     * Optional.
     *
     * Sets enable or disable format extension in request path.
     *
     * <p/>
     * Default is enabled.
     */
	WebConfigurator setFormatExtensionEnabled(boolean enabled);

    /**
     * Optional.
     *
     * Sets enable or disable resolving request/response data format by request parameter, such as <code>.json</code>.
     *
     * <p/>
     * Default is enabled.
     */
	WebConfigurator setFormatParameterEnabled(boolean enabled);

    /**
     * Optional.
     *
     * Sets enable of disable action extension(s) in request path, such as <code>.do</code>.
     *
     * <p/>
     * Default is enabled.
     */
	WebConfigurator setActionExtensionEnabled(boolean enabled);

    /**
     * Optional.
     *
     * Sets enable or disable CROS for all requests by default.
     *
     * <p/>
     * Default is disabled.
     */
	WebConfigurator setCorsEnabled(boolean enabled);

    /**
     * Optional.
     *
     * Sets enable or disable trims all request parameters automatically by default.
     *
     * <p/>
     * Default is enabled.
     */
	WebConfigurator setAutoTrimParameters(boolean enabled);

    /**
     * Optional.
     *
     * Sets the domain name for writing cookie in response.
     *
     * <p/>
     * See {@link javax.servlet.http.Cookie#setDomain(String)} for details.
     *
     * <p/>
     * Default is no domain name specified.
     */
	WebConfigurator setCookieDomain(String domain);

    /**
     * Optional.
     *
     * Sets the action extension(s).
     */
	WebConfigurator setActionExtensions(Set<String> extensions);

    /**
     * Optional.
     *
     * Sets the action extension(s).
     */
	WebConfigurator setActionExtensions(String... extensions);

    /**
     * Adds an action extension.
     *
     * @param extension the extension name without dot prefix.
     */
	WebConfigurator addActionExtension(String extension);
}