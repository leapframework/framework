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
package leap.web.view;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.ioc.PostCreateBean;
import leap.lang.Locales;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.lang.servlet.ServletResource;
import leap.lang.servlet.Servlets;

import java.util.Locale;

public abstract class AbstractServletResourceViewResolver extends AbstractViewResolver implements ServletResourceViewResolver,PostCreateBean {
	
	protected final Log log = LogFactory.get(this.getClass());
	
	protected @Inject @M ViewStrategy viewStrategy;
	
	protected String prefix = "";
	protected String suffix = "";
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	
	@Override
    public void postCreate(BeanFactory beanFactory) throws Throwable {
		if(Strings.isEmpty(prefix)){
			prefix = webConfig.getViewsLocation();
		}
    }

	@Override
    public View resolveView(String location, String viewName, Locale locale) throws Throwable {
	    return doResolveView(location, viewName, locale);
    }

	@Override
    public View resolveView(String viewName, Locale locale) throws Throwable {
		return doResolveView(prefix, viewName, locale);
    }
	
    protected View doResolveView(String prefix, String viewName, Locale locale) throws Throwable {
		ServletResource resource = getLocaleResource(prefix, suffix, viewName, locale);
		if(null == resource || !resource.exists()){
			
			String[] candidateViewPaths = viewStrategy.getCandidateViewPaths(viewName);
			
			for(String candidateViewPath : candidateViewPaths){
				resource = getLocaleResource(prefix,suffix, candidateViewPath, locale);
				
				if(null != resource && resource.exists()){
					break;
				}
			}
			
			if(null == resource){
				return null;
			}
		}

		return loadView(prefix, suffix, viewName, locale, resource.getPathWithinContext(), resource);
    }

	protected ServletResource getLocaleResource(String prefix, String suffix, String viewPath,Locale locale){
		String pathPrefix = Paths.suffixWithoutSlash(prefix) + Paths.prefixWithSlash(viewPath);
		
		if(!Strings.isEmpty(suffix) && pathPrefix.endsWith(suffix)){
			return Servlets.getResource(servletContext, pathPrefix);
		}
		
		String[] paths = Locales.getLocalePaths(locale, pathPrefix, suffix);
		for(String path : paths){
			ServletResource r = Servlets.getResource(servletContext, path);
			if(null != r && r.exists()){
				return r;
			}
		}
		
		return null;
	}
	
	protected abstract View loadView(String prefix, String suffix, String viewName, Locale locale, String resourcePath, ServletResource resource);
}