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

import leap.core.AppConfig;
import leap.core.BeanFactory;
import leap.core.annotation.*;
import leap.core.ioc.PostCreateBean;
import leap.lang.Args;
import leap.lang.collection.ImvCopyOnWriteArraySet;
import leap.lang.collection.ImvSet;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
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

    private static final Log log = LogFactory.get(DefaultWebConfig.class);
	
	protected @R String  viewsLocation          = DEFAULT_VIEWS_LOCATION;
	protected @R String  themesLocation         = DEFAULT_THEMES_LOCATION;
	protected @R String  defaultThemeName       = DEFAULT_THEME_NAME;
	protected @R String  defaultFormatName      = DEFAULT_FORMAT_NAME;
	protected @R String  formatParameterName    = DEFAULT_FORMAT_PARAMETER_NAME;
	protected @R String  jsessionidPrefix       = DEFAULT_JSESSIONID_PREFIX;
    protected @R String  homeControllerName     = DEFAULT_HOME_CONTROLLER_NAME;
    protected @R String  indexActionName        = DEFAULT_INDEX_ACTION_NAME;
	protected @R boolean autoTrimParameters     = true;
	protected @R boolean formatExtensionEnabled = true;
	protected @R boolean formatParameterEnabled = true;
	protected @R boolean actionExtensionEnabled = true;
	protected @R boolean corsEnabled            = false;
	protected @N String  cookieDomain           = null;

    protected @Inject @M AppConfig       config;
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

    protected final Set<ModuleConfig> modules          = new ImvCopyOnWriteArraySet<>();
    protected final Set<String>       actionExtensions = new ImvCopyOnWriteArraySet<>();

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
		return formatParameterName;
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
    public String getHomeControllerName() {
        return homeControllerName;
    }

    @Override
    public String getIndexActionName() {
        return indexActionName;
    }

    @Override
    public Set<String> getActionExtensions() {
	    return ((ImvSet)actionExtensions).getImmutableView();
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

	@ConfigProperty
	public WebConfigurator setViewsLocation(String path) {
        Args.notEmpty(path, "views location");
		this.viewsLocation = path;
		return this;
	}

    @ConfigProperty
    public WebConfigurator setThemesLocation(String path) {
        Args.notEmpty(path,"themes location");
        this.themesLocation = Paths.prefixWithAndSuffixWithoutSlash(path);
        return this;
    }

    @ConfigProperty
	public WebConfigurator setDefaultFormatName(String defaultFormatName) {
		this.defaultFormatName = defaultFormatName;
		return this;
	}
	
	@ConfigProperty
	public WebConfigurator setDefaultThemeName(String defaultThemeName) {
		this.defaultThemeName = defaultThemeName;
		return this;
	}

	@ConfigProperty
	public WebConfigurator setFormatParameterName(String formatParameter) {
		this.formatParameterName = formatParameter;
		return this;
	}

	@ConfigProperty
	public WebConfigurator setJsessionidPrefix(String p) {
		this.jsessionidPrefix = p;
		return this;
	}

	@ConfigProperty
	public WebConfigurator setFormatExtensionEnabled(boolean enabled) {
		this.formatExtensionEnabled = enabled;
		return this;
	}

	@ConfigProperty
	public WebConfigurator setFormatParameterEnabled(boolean enabled) {
		this.formatParameterEnabled = enabled;
		return this;
	}

	@ConfigProperty
	public WebConfigurator setActionExtensionEnabled(boolean enabled) {
		this.actionExtensionEnabled = enabled;
		return this;
	}

	@ConfigProperty
	public WebConfigurator setCorsEnabled(boolean enabled) {
		this.corsEnabled = enabled;
		return this;
	}

	@ConfigProperty
	public WebConfigurator setAutoTrimParameters(boolean eanbled) {
		this.autoTrimParameters = eanbled;
		return this;
	}
	
	@ConfigProperty
	public WebConfigurator setCookieDomain(String cookieDomain) {
		this.cookieDomain = cookieDomain;
		return this;
	}

    @ConfigProperty
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

    public Set<ModuleConfig> getModules() {
        return ((ImvSet)modules).getImmutableView();
    }
	
	@Override
    public void postCreate(BeanFactory factory) throws Throwable {
        //filters.
        this.filters.addAll(factory.getBeans(FilterMapping.class));

        //modules.
        ModuleConfigExtension moduleConfigExtension = config.getExtension(ModuleConfigExtension.class);
        if(null != moduleConfigExtension) {
            modules.addAll(moduleConfigExtension.getModules().values());
            log.info("Load {} web module configurations", modules.size());
        }
    }
}