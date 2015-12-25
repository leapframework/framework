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

import leap.core.BeanFactory;
import leap.web.ajax.AjaxDetector;
import leap.web.ajax.AjaxHandler;
import leap.web.cors.CorsHandler;
import leap.web.format.FormatManager;
import leap.web.pjax.PjaxDetector;
import leap.web.theme.ThemeManager;

public interface WebConfig {
	
    /**
     * Starts with '/' but not ends with '/'
     */
    String getViewsLocation();
    
	/**
     * Starts with '/' but not ends with '/'
     */
	String getThemesLocation();
    
    /**
     * Returns this application's theme name.
     */
    String getDefaultThemeName();
    
    String getDefaultFormatName();
    
    String getFormatParameter();
    
    /**
     * Returns the name of jsessionid in url, such as <code>;jsessionid=xxxxxx</coce>.
     * 
     * <p>
     * Default is <code>;jsessionid</code>
     */
    String getJsessionidParameter();
    
    /**
     * Returns the domain string for set cookie in response.
     * 
     * <p>
     * Returns <code>null</code> if no domain.
     */
    String getCookieDomain();
    
    boolean isTrimParameters();
    
    boolean isAllowActionExtension();
    
    boolean isAllowFormatExtension();
    
    boolean isAllowFormatParameter();

    Set<String> getActionExtensions();
    
    boolean isCorsEnabled();
    
	/**
	 * Returns the {@link ThemeManager} of current application.
	 * 
	 * <p>
	 * The returned object is a reference of primary bean for type {@link ThemeManager}.
	 * 
	 * @see BeanFactory#getBean(Class)
	 */
	ThemeManager getThemeManager();
	
	/**
	 * Returns the {@link FormatManager} of current application.
	 * 
	 * <p>
	 * The returned object is a reference of primary bean for type {@link FormatManager}.
	 * 
	 * @see BeanFactory#getBean(Class)
	 */
	FormatManager getFormatManager();
	
	/**
	 * Returns the {@link AjaxHandler} of current application.
	 */
	AjaxHandler getAjaxHandler();
	
	AjaxDetector getAjaxDetector();
	
	PjaxDetector getPjaxDetector();
	
    CorsHandler getCorsHandler();
}