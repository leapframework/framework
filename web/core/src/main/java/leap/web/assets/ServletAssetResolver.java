/*
 * Copyright 2013 the original author or authors.
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
package leap.web.assets;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.ioc.PostCreateBean;
import leap.core.web.assets.Asset;
import leap.core.web.assets.AssetConfig;
import leap.core.web.assets.AssetManager;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.lang.servlet.ServletResource;
import leap.lang.servlet.Servlets;

public class ServletAssetResolver extends AbstractAssetResolver implements PostCreateBean {
	
	protected static final Log log = LogFactory.get(ServletAssetResolver.class);

	protected @Inject @M AssetManager manager;
	protected @Inject @M AssetConfig  config;
	
	protected String prefix;
	
	private final Map<String, ServletAsset> cache = new ConcurrentHashMap<String, ServletAsset>();
	
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
    public void postCreate(BeanFactory beanFactory) throws Throwable {
		if(null == prefix){
			prefix = config.getPublicDirectory();
		}
    }

	@Override
    public Asset resolveAsset(String path, Locale locale) throws Exception {
		path = Paths.prefixWithoutSlash(path);
		
		ServletResource resource = getLocaleResource(getResourcePath(prefix,path),locale);
		if(null == resource || !resource.exists()){
			return null;
		}
		
		ServletAsset asset = cache.get(resource.getPathWithinContext());
		if(null == asset){
			asset = new ServletAsset(manager,path,resource); 
			cache.put(resource.getPathWithinContext(), asset);
		}
		return asset;
    }
	
	protected ServletResource getLocaleResource(String resourcePath,Locale locale){
		String suffix     = "." + Paths.getFileExtension(resourcePath);
		String pathPrefix = resourcePath.substring(0,resourcePath.length() - suffix.length());
		
		String lang    = null == locale ? null : locale.getLanguage();
		String country = null == locale ? null : locale.getCountry();
		
		//{pathPrefix}_{lang}_{COUNTRY}{suffix}
		if(!Strings.isEmpty(country)){
			String path = pathPrefix + "_" + locale.getLanguage() + "_" + country + suffix;
			ServletResource resource = Servlets.getResource(servletContext, path);
			if(null != resource && resource.exists()){
				return resource;
			}
		}
		
		//{pathPrefix_{lang}{suffix}
		if(!Strings.isEmpty(lang)){
			String path = pathPrefix + "_" + locale.getLanguage() + suffix;
			ServletResource resource = Servlets.getResource(servletContext, path);
			if(null != resource && resource.exists()){
				return resource;
			}
		}
		
		//{pathPrefix}{suffix}
		String path = pathPrefix + suffix;
		ServletResource resource = Servlets.getResource(servletContext, path);
		if(null != resource && resource.exists()){
			return resource;
		}
		
		return null;
	}
	
	protected static String getResourcePath(String prefix, String path){
		path = Paths.prefixWithoutSlash(path);
		
		if(Strings.isEmpty(prefix) || path.startsWith(prefix + "/")){
			return path;
		}else{
			return Paths.suffixWithSlash(prefix) + path;
		}
	}
}