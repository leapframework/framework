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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import leap.core.BeanFactory;
import leap.core.annotation.Configurable;
import leap.core.annotation.Inject;
import leap.core.ioc.BeanList;
import leap.core.ioc.PostCreateBean;
import leap.lang.Args;
import leap.lang.path.Paths;
import leap.web.FilterMapping;
import leap.web.FilterMappings;
import leap.web.RequestInterceptor;
import leap.web.ajax.AjaxDetector;
import leap.web.ajax.AjaxHandler;
import leap.web.cors.CorsHandler;
import leap.web.error.ErrorCodes;
import leap.web.error.ErrorViews;
import leap.web.format.FormatManager;
import leap.web.pjax.PjaxDetector;
import leap.web.route.Routes;
import leap.web.theme.ThemeManager;

@Configurable(prefix="webmvc")
public class DefaultWebConfig implements WebConfig,WebConfigurator,PostCreateBean {
	
	protected String  viewsLocation        = DEFAULT_VIEWS_LOCATION;
	protected String  themesLocation       = DEFAULT_THEMES_LOCATION;
	protected String  defaultThemeName     = DEFAULT_THEME_NAME;
	protected String  defaultFormatName    = DEFAULT_FORMAT_NAME;
	protected String  formatParameter      = DEFAULT_FORMAT_PARAMETER;
	protected String  eventParameter       = DEFAULT_EVENT_PARAMETER;
	protected String  jsessionidParameter  = DEFAULT_JSESSIONID_PARAMETER;
	protected boolean trimParameters       = true;
	protected boolean allowFormatExtension = true;
	protected boolean allowFormatParameter = true;
	protected boolean allowActionExtension = true;
	protected boolean corsEnabled          = false;
	protected String  cookieDomain		   = null;
	
	protected Routes            routes;
	protected FilterMappings    filters;
	protected ErrorViews		errorViews;
	protected ErrorCodes	    errorCodes;
	
	protected ThemeManager      themeManager;
	protected FormatManager     formatManager;
	protected AjaxHandler		ajaxHandler;
	protected AjaxDetector      ajaxDetector;
	protected PjaxDetector		pjaxDetector;
	protected CorsHandler		corsHandler;
	
	protected @Inject WebInterceptors interceptors;
	
	protected final Set<String> actionExtensions              = new CopyOnWriteArraySet<String>();
	protected final Set<String> actionExtensionsImmutableView = Collections.unmodifiableSet(actionExtensions); 
	
	@Override
    public WebConfig config() {
	    return this;
    }
	
	@Override
    public Routes routes() {
	    return routes;
    }

	@Override
    public ErrorViews errorViews() {
	    return errorViews;
    }
	
	@Override
    public ErrorCodes errorCodes() {
	    return errorCodes;
    }

	@Override
    public FilterMappings filters() {
	    return filters;
    }

	@Override
    public WebInterceptors interceptors() {
	    return interceptors;
    }

	public String getThemesLocation() {
		return themesLocation;
	}

	public String getDefaultThemeName() {
		return defaultThemeName;
	}
	
	public String getDefaultFormatName() {
		return defaultFormatName;
	}

	public String getFormatParameter() {
		return formatParameter;
	}

	public String getEventParameter() {
		return eventParameter;
	}
	
	@Override
    public String getJsessionidParameter() {
	    return jsessionidParameter;
    }

	@Override
    public String getCookieDomain() {
	    return cookieDomain;
    }

	public boolean isAllowFormatExtension() {
		return allowFormatExtension;
	}
	
	public boolean isAllowFormatParameter() {
		return allowFormatParameter;
	}
	
	public boolean isAllowActionExtension() {
		return allowActionExtension;
	}
	
	public boolean isCorsEnabled() {
		return corsEnabled;
	}
	
	public String getViewsLocation() {
		return viewsLocation;
	}

	public boolean isTrimParameters() {
		return trimParameters;
	}
	
	@Override
    public Set<String> getActionExtensions() {
	    return actionExtensionsImmutableView;
    }
	
	@Override
    public ThemeManager getThemeManager() {
	    return themeManager;
    }
	
	@Override
    public FormatManager getFormatManager() {
	    return formatManager;
    }

	@Override
    public AjaxHandler getAjaxHandler() {
	    return ajaxHandler;
    }

	@Override
    public AjaxDetector getAjaxDetector() {
	    return ajaxDetector;
    }

	@Override
    public PjaxDetector getPjaxDetector() {
	    return pjaxDetector;
    }

	@Override
    public CorsHandler getCorsHandler() {
	    return corsHandler;
    }

	@Configurable.Property
	public WebConfigurator setViewsLocation(String viewsLocation) {
		this.viewsLocation = viewsLocation;
		return this;
	}
	
	@Configurable.Property
	public WebConfigurator setDefaultFormatName(String defaultFormatName) {
		this.defaultFormatName = defaultFormatName;
		return this;
	}
	
	@Configurable.Property
	public WebConfigurator setThemesLocation(String themesLocation) {
    	Args.notEmpty(themesLocation,"themes location");
    	this.themesLocation = Paths.prefixWithAndSuffixWithoutSlash(themesLocation);
		return this;
	}

	@Configurable.Property
	public WebConfigurator setDefaultThemeName(String defaultThemeName) {
		this.defaultThemeName = defaultThemeName;
		return this;
	}

	@Configurable.Property
	public WebConfigurator setFormatParameter(String formatParameter) {
		this.formatParameter = formatParameter;
		return this;
	}

	@Configurable.Property
	public WebConfigurator setEventParameter(String eventParameter) {
		this.eventParameter = eventParameter;
		return this;
	}
	
	@Configurable.Property
	public WebConfigurator setJsessionidParameter(String p) {
		this.jsessionidParameter = p;
		return this;
	}

	@Configurable.Property
	public WebConfigurator setAllowFormatExtension(boolean allowFormatExtension) {
		this.allowFormatExtension = allowFormatExtension;
		return this;
	}

	@Configurable.Property
	public WebConfigurator setAllowFormatParameter(boolean allowFormatParameter) {
		this.allowFormatParameter = allowFormatParameter;
		return this;
	}

	@Configurable.Property
	public WebConfigurator setAllowActionExtension(boolean allowActionExtension) {
		this.allowActionExtension = allowActionExtension;
		return this;
	}

	@Configurable.Property
	public WebConfigurator setCorsEnabled(boolean corsEnabled) {
		this.corsEnabled = corsEnabled;
		return this;
	}

	@Configurable.Property
	public WebConfigurator setTrimParameters(boolean trimParameters) {
		this.trimParameters = trimParameters;
		return this;
	}
	
	@Configurable.Property
	public WebConfigurator setCookieDomain(String cookieDomain) {
		this.cookieDomain = cookieDomain;
		return this;
	}
	
	@Configurable.Property
	public WebConfigurator setActionExtensions(Set<String> extensions) {
		this.actionExtensions.clear();
		if(null != extensions) {
			for(String s : extensions) {
				addActionExtension(s);
			}
		}
		return this;
	}

    public WebConfigurator setActionExtensions(String... extensions) {
		this.actionExtensions.clear();
		if(null != extensions) {
			for(String extension : extensions){
				addActionExtension(extension);
			}
		}
		return this;
    }
	
	public WebConfigurator addActionExtension(String extension) throws IllegalArgumentException {
		Args.notEmpty(extension,"action extension");
		if(extension.startsWith(".")){
			extension = extension.substring(1);
		}
		actionExtensions.add(extension);
		return this;
	}
	
	@Override
    public void postCreate(BeanFactory factory) throws Throwable {
		this.themeManager  = factory.getBean(ThemeManager.class);
		this.formatManager = factory.getBean(FormatManager.class);
		this.ajaxHandler   = factory.getBean(AjaxHandler.class);
		this.corsHandler   = factory.getBean(CorsHandler.class);
		this.ajaxDetector  = factory.getBean(AjaxDetector.class);
		this.pjaxDetector  = factory.getBean(PjaxDetector.class);
		this.routes		   = factory.getBean(Routes.class);
		this.filters	   = factory.getBean(FilterMappings.class);
		this.errorViews    = factory.getBean(ErrorViews.class);
		this.errorCodes    = factory.getBean(ErrorCodes.class);
		
		this.filters.addAll(factory.getBeans(FilterMapping.class));
    }
}