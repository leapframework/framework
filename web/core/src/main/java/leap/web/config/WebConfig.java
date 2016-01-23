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

import java.util.Set;

import leap.web.ajax.AjaxDetector;
import leap.web.ajax.AjaxHandler;
import leap.web.cors.CorsHandler;
import leap.web.format.FormatManager;
import leap.web.pjax.PjaxDetector;
import leap.web.theme.ThemeManager;

/**
 * The configuration of web app.
 *
 * <p/>
 * Use {@link WebConfigurator} for configuring.
 */
public interface WebConfig {
	
    /**
     * Required.
     *
     * Returns the path of view's location in web app, It must be starts with '/' but not ends with '/'.
     *
     * <p/>
     * Default is {@link WebConfigurator#DEFAULT_VIEWS_LOCATION}.
     */
    String getViewsLocation();
    
	/**
     * Required.
     *
     * Returns the path of theme's location in web app, It must be starts with '/' but not ends with '/'.
     *
     * <p/>
     * Default is {@link WebConfigurator#DEFAULT_THEMES_LOCATION}.
     */
	String getThemesLocation();
    
    /**
     * Required.
     *
     * Returns the name of default theme name.
	 *
	 * <p/>
	 * Default is {@link WebConfigurator#DEFAULT_THEME_NAME}.
     */
    String getDefaultThemeName();

    /**
     * Required.
     *
     * Returns the name of default request/response data format.
     *
     * <p/>
     * Default is {@link WebConfigurator#DEFAULT_FORMAT_NAME}.
     */
    String getDefaultFormatName();

    /**
     * Required.
     *
     * Returns the request parameter name for resolving request data format.
     *
     * <p/>
     * Default is {@link WebConfigurator#DEFAULT_FORMAT_PARAMETER}.
     */
    String getFormatParameterName();
    
    /**
     * Required.
     *
     * Returns the prefix part of jsessionid in url, such as <code>;jsessionid=xxxxxx</coce>.
     * 
     * <p>
     * Default is {@link WebConfigurator#DEFAULT_JSESSIONID_PREFIX}.
     */
    String getJsessionidPrefix();
    
    /**
     * Optional.
     *
     * Returns the domain name for set cookie in response.
     *
     * <p/>
     * Default is <code>null</code>.
     */
    String getCookieDomain();

    /**
     * Returns <code>true</code> if trims all request parameters automatically.
     *
     * <p/>
     * Default is <code>true</code>.
     */
    boolean isAutoTrimParameters();

    /**
     * Returns <code>true</code> if allow action extension(s) in request path, such as <code>.do</code>.
     *
     * <p/>
     * Default is <code>true</code>.
     */
    boolean isActionExtensionEnabled();

    /**
     * Returns <code>true</code> if allow format extension in request path, such as <code>.json</code>.
     *
     * <p/>
     * Default is <code>true</code>.
     */
    boolean isFormatExtensionEnabled();

    /**
     * Returns <code>true</code> if allow resolving request format form parameter.
     *
     * <p/>
     * Default is <code>true</code>.
     */
    boolean isFormatParameterEnabled();

    /**
     * Returns an immutable {@link Set} contains the action extensions.
     */
    Set<String> getActionExtensions();

    /**
     * Returns <code>true</code> if CORS is enabled in current web app by default.
     *
     * <p/>
     * Default <code>false</code>.
     */
    boolean isCorsEnabled();

	/**
     * Required.
     *
	 * Returns the implementation of {@link ThemeManager}.
	 */
	ThemeManager getThemeManager();

	/**
     * Required.
     *
     * Returns the implementation of {@link FormatManager}.
	 */
	FormatManager getFormatManager();

	/**
	 * Required.
     *
     * Returns the implementation of {@link AjaxHandler}.
	 */
	AjaxHandler getAjaxHandler();

    /**
     * Required.
     *
     * Returns the implementation of {@link AjaxDetector}.
     */
	AjaxDetector getAjaxDetector();

    /**
     * Required.
     *
     * Returns the implementation of {@link PjaxDetector}.
     */
	PjaxDetector getPjaxDetector();

    /**
     * Required.
     *
     * Returns the implementation of {@link CorsHandler}.
     */
    CorsHandler getCorsHandler();
}