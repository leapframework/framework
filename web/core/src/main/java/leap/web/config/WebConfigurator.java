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


public interface WebConfigurator {
	
	String CONFIG_PREFIX = "webmvc.";
	
	String CONFIG_DEFAULT_CHARSET        = CONFIG_PREFIX + "default-charset";
	String CONFIG_DEFAULT_LOCALE         = CONFIG_PREFIX + "default-locale";
	String CONFIG_DEFAULT_THEME	         = CONFIG_PREFIX + "default-theme";
	String CONFIG_THEMES_LOCATION		 = CONFIG_PREFIX + "themes-location";
	String CONFIG_VIEWS_LOCATION         = CONFIG_PREFIX + "views-location";
	String CONFIG_DEFAULT_FORMAT         = CONFIG_PREFIX + "default-format";
	String CONFIG_FORMAT_PARAMETER       = CONFIG_PREFIX + "format-parameter";
	String CONFIG_JSESSIONID_PARAMETER   = CONFIG_PREFIX + "jsessionid-parameter";
	String CONFIG_TRIM_PARAMETERS		 = CONFIG_PREFIX + "trim-parameters";
	String CONFIG_ALLOW_FORMAT_EXTENSION = CONFIG_PREFIX + "allow-format-extension";
	String CONFIG_ALLOW_FORMAT_PARAMETER = CONFIG_PREFIX + "allow-format-parameter";
	String CONFIG_ALLOW_ACTION_EXTENSION = CONFIG_PREFIX + "allow-action-extension";
	String CONFIG_ACTION_EXTENSIONS      = CONFIG_PREFIX + "action-extensions";
	String CONFIG_CORS_ENABLED			 = CONFIG_PREFIX + "cors-enabled";
	
	String DEFAULT_VIEWS_LOCATION       = "/WEB-INF/views";
	String DEFAULT_THEMES_LOCATION      = "/WEB-INF/themes";
	String DEFAULT_THEME_NAME		    = "default";
	String DEFAULT_FORMAT_NAME	        = "json";
	String DEFAULT_FORMAT_PARAMETER     = "$format";
	String DEFAULT_EVENT_PARAMETER      = "$event";
	String DEFAULT_JSESSIONID_PARAMETER = ";jsessionid";
	
	WebConfig config();
	
	Routes routes();
	
	ErrorViews errorViews();
	
	ErrorCodes errorCodes();
	
	FilterMappings filters();
	
	WebInterceptors interceptors();
	
	WebConfigurator setViewsLocation(String viewsLocation);
	
	WebConfigurator setDefaultFormatName(String defaultFormatName);
	
	WebConfigurator setThemesLocation(String themesLocation);

	WebConfigurator setDefaultThemeName(String defaultThemeName);

	WebConfigurator setFormatParameter(String formatParameter);

	WebConfigurator setEventParameter(String eventParameter);
	
	WebConfigurator setJsessionidParameter(String p);

	WebConfigurator setAllowFormatExtension(boolean allowFormatExtension);

	WebConfigurator setAllowFormatParameter(boolean allowFormatParameter);

	WebConfigurator setAllowActionExtension(boolean allowActionExtension);

	WebConfigurator setCorsEnabled(boolean corsEnabled);
	
	WebConfigurator setTrimParameters(boolean trimParameters);
	
	WebConfigurator setCookieDomain(String cookieDomain);
	
	WebConfigurator setActionExtensions(Set<String> extensions);
	
	WebConfigurator setActionExtensions(String... extensions);
	
	WebConfigurator addActionExtension(String extension) throws IllegalArgumentException;
}