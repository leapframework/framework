/*
 * Copyright 2014 the original author or authors.
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
package leap.web.theme;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import leap.core.AppConfigException;
import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.cache.CacheManager;
import leap.core.i18n.MessageSource;
import leap.core.i18n.ResourceMessageSource;
import leap.core.ioc.PostCreateBean;
import leap.web.assets.Asset;
import leap.web.assets.AssetSource;
import leap.lang.exception.ObjectNotFoundException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;
import leap.lang.resource.Resources;
import leap.lang.servlet.ServletResource;
import leap.lang.servlet.Servlets;
import leap.web.App;
import leap.web.Request;
import leap.web.assets.ServletAssetResolver;
import leap.web.assets.SimpleCachingAssetSource;
import leap.web.config.WebConfig;
import leap.web.view.ServletResourceViewSource;
import leap.web.view.View;
import leap.web.view.ViewSource;

public class DefaultThemeManager implements ThemeManager,PostCreateBean {
	
	private static final Log log = LogFactory.get(DefaultThemeManager.class);
	
    protected @Inject App             app;
    protected @Inject WebConfig       webConfig;
    protected @Inject AssetSource     assetSource;
    protected @Inject ViewSource      viewSource;
    protected @Inject CacheManager    cacheManager;
    protected @Inject ThemeResolver[] themeResolvers;
	
	protected Theme				 defaultTheme;
	protected Map<String, Theme> themes = new HashMap<String, Theme>();
	
	@Override
    public Theme getTheme(String name) throws ObjectNotFoundException {
		Theme theme = tryGetTheme(name);
		
		if(null == theme){
			throw new ObjectNotFoundException("The theme '" + name + "' not found");
		}
		
	    return theme;
    }

	@Override
    public Theme tryGetTheme(String name) {
	    return themes.get(name);
    }
	
	@Override
    public Theme resolveTheme(Request request) throws Throwable {
		if(themes.isEmpty()){
			return null;
		}
		
		String themeName = null;
		for(int i=0;i<themeResolvers.length;i++){
			if((themeName = themeResolvers[i].resolveThemeName(request)) != null){
				break;
			}
		}
		
		return null == themeName ? defaultTheme : getTheme(themeName);
    }

	@Override
    public void postCreate(BeanFactory beanFactory) throws Throwable {
		loadThemes();

		if(!themes.isEmpty()){
			this.defaultTheme = themes.get(webConfig.getDefaultThemeName());
			if(null == this.defaultTheme){
				throw new AppConfigException("Default theme '" + webConfig.getDefaultThemeName() + "' not defined");
			}
		}
	}
	
	protected void loadThemes() throws Throwable {
		ServletResource themesDir = Servlets.getResource(app.getServletContext(), webConfig.getThemesLocation());
		if(themesDir.exists()){
			Set<String> themePaths = app.getServletContext().getResourcePaths(webConfig.getThemesLocation());
			for(String themePath : themePaths){
				if(themePath.endsWith("/")){
					loadTheme(themePath);
				}
			}
		}
	}
	
	protected void loadTheme(String path) throws Throwable {
		String themeName = path.substring(webConfig.getThemesLocation().length() + 1,path.length() - 1);
		log.debug("Found theme '" + themeName + "' in path '" + path + "'");
		
		ServletResource dir = Servlets.getResource(app.getServletContext(), path);
		
		SimpleTheme.Builder theme = new SimpleTheme.Builder();
		
		theme.setName(themeName)
			 .setMessageSource(getThemeBasedMessageSource(themeName,dir))
			 .setAssetSource(getThemeBasedAssetSource(themeName,dir))
			 .setViewSource(getThemeBasedViewSource(themeName,dir));
			
		themes.put(themeName, theme.build());
	}
	
	protected MessageSource getThemeBasedMessageSource(String themeName,ServletResource themeDir) throws Throwable {
		//TODO : config theme based messages directory
		ServletResource messagesDir = themeDir.createRelative("messages");
		if(null != messagesDir && messagesDir.exists()){
			ResourceSet rs = Resources.scan(messagesDir, "**/*.*");
			if(!rs.isEmpty()){
				MessageSource themeMessageSource = 
						app.factory().newInstance(ResourceMessageSource.class).readFromResources(rs.toArray(Resource.class));
				return new ThemeOrDefaultMessageSource(themeMessageSource,app.getMessageSource());
			}
		}
		return null;
	}
	
	protected AssetSource getThemeBasedAssetSource(String themeName, ServletResource themeDir) throws Throwable {
		//TODO: config theme based assets directory
		ServletResource assetsDir = themeDir.createRelative("static");
		if(null != assetsDir && assetsDir.exists()){
			String location = assetsDir.getPathWithinContext();
			
			ServletAssetResolver resolver = app.factory().newInstance(ServletAssetResolver.class);
			resolver.setPrefix(Paths.prefixAndSuffixWithSlash(location));
			
			SimpleCachingAssetSource themeAssetSource = new SimpleCachingAssetSource();
			themeAssetSource.setAssetCache(cacheManager.<Object,Asset>createSimpleLRUCache(Theme.class.getName() + "$assets." + themeName));
			themeAssetSource.setResolver(resolver);
			app.factory().inject(themeAssetSource);
			
			return new ThemeOrDefaultAssetSource(themeAssetSource, assetSource);
		}
		
		return null;
	}
	
	protected ViewSource getThemeBasedViewSource(String themeName,ServletResource themeDir) throws Throwable {
		//TODO : config theme based views directory
		ServletResource viewsDir = themeDir.createRelative("views");
		if(null != viewsDir && viewsDir.exists()){
			String location = viewsDir.getPathWithinContext();
			
			ServletResourceViewSource themeViewSource = new ServletResourceViewSource();
			themeViewSource.setLocation(location);
			themeViewSource.setViewCache(cacheManager.<Object,View>createSimpleLRUCache(Theme.class.getName() + "$views." + themeName));
			app.factory().inject(themeViewSource);
			
			return new ThemeOrDefaultViewSource(themeViewSource, viewSource);
		}
		return null;
	}
}