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
import leap.core.annotation.M;
import leap.core.ioc.PostCreateBean;
import leap.lang.Args;
import leap.lang.path.Paths;
import leap.web.FilterMapping;
import leap.web.FilterMappings;
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
	
	protected String  viewsLocation          = DEFAULT_VIEWS_LOCATION;
	protected String  themesLocation         = DEFAULT_THEMES_LOCATION;
	protected String  defaultThemeName       = DEFAULT_THEME_NAME;
	protected String  defaultFormatName      = DEFAULT_FORMAT_NAME;
	protected String  formatParameter        = DEFAULT_FORMAT_PARAMETER;
	protected String  jsessionidPrefix       = DEFAULT_JSESSIONID_PREFIX;
	protected boolean autoTrimParameters     = true;
	protected boolean formatExtensionEnabled = true;
	protected boolean formatParameterEnabled = true;
	protected boolean actionExtensionEnabled = true;
	protected boolean corsEnabled            = false;
	protected String  cookieDomain           = null;

	protected @Inject @M Routes          routes;
	protected @Inject @M FilterMappings  filters;
	protected @Inject @M ErrorViews      errorViews;
	protected @Inject @M ErrorCodes      errorCodes;
	protected @Inject @M ThemeManager    themeManager;
	protected @Inject @M FormatManager   formatManager;
	protected @Inject @M AjaxHandler     ajaxHandler;
	protected @Inject @M AjaxDetector    ajaxDetector;
	protected @Inject @M PjaxDetector    pjaxDetector;
	protected @Inject @M CorsHandler     corsHandler;
	protected @Inject @M WebInterceptors interceptors;
	
	protected final Set<String> actionExtensions              = new CopyOnWriteArraySet<>();
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

	public String getFormatParameterName() {
		return formatParameter;
	}

	@Override
    public String getJsessionidPrefix() {
	    return jsessionidPrefix;
    }

	@Override
    public String getCookieDomain() {
	    return cookieDomain;
    }

	public boolean isFormatExtensionEnabled() {
		return formatExtensionEnabled;
	}
	
	public boolean isFormatParameterEnabled() {
		return formatParameterEnabled;
	}
	
	public boolean isActionExtensionEnabled() {
		return actionExtensionEnabled;
	}
	
	public boolean isCorsEnabled() {
		return corsEnabled;
	}
	
	public String getViewsLocation() {
		return viewsLocation;
	}

	public boolean isAutoTrimParameters() {
		return autoTrimParameters;
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
	public WebConfigurator setViewsLocation(String path) {
		this.viewsLocation = path;
		return this;
	}

    @Configurable.Property
    public WebConfigurator setThemesLocation(String path) {
        Args.notEmpty(path,"themes location");
        this.themesLocation = Paths.prefixWithAndSuffixWithoutSlash(path);
        return this;
    }

    @Configurable.Property
	public WebConfigurator setDefaultFormatName(String defaultFormatName) {
		this.defaultFormatName = defaultFormatName;
		return this;
	}
	
	@Configurable.Property
	public WebConfigurator setDefaultThemeName(String defaultThemeName) {
		this.defaultThemeName = defaultThemeName;
		return this;
	}

	@Configurable.Property
	public WebConfigurator setFormatParameterName(String formatParameter) {
		this.formatParameter = formatParameter;
		return this;
	}

	@Configurable.Property
	public WebConfigurator setJsessionidPrefix(String p) {
		this.jsessionidPrefix = p;
		return this;
	}

	@Configurable.Property
	public WebConfigurator setFormatExtensionEnabled(boolean enabled) {
		this.formatExtensionEnabled = enabled;
		return this;
	}

	@Configurable.Property
	public WebConfigurator setFormatParameterEnabled(boolean enabled) {
		this.formatParameterEnabled = enabled;
		return this;
	}

	@Configurable.Property
	public WebConfigurator setActionExtensionEnabled(boolean enabled) {
		this.actionExtensionEnabled = enabled;
		return this;
	}

	@Configurable.Property
	public WebConfigurator setCorsEnabled(boolean enabled) {
		this.corsEnabled = enabled;
		return this;
	}

	@Configurable.Property
	public WebConfigurator setAutoTrimParameters(boolean eanbled) {
		this.autoTrimParameters = eanbled;
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
	
	public WebConfigurator addActionExtension(String extension){
		Args.notEmpty(extension,"action extension");
		if(extension.startsWith(".")){
			extension = extension.substring(1);
		}
		actionExtensions.add(extension);
		return this;
	}
	
	@Override
    public void postCreate(BeanFactory factory) throws Throwable {
		this.filters.addAll(factory.getBeans(FilterMapping.class));
    }
}