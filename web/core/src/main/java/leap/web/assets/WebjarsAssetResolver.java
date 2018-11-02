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
package leap.web.assets;

import java.util.Locale;

import leap.core.BeanFactory;
import leap.lang.Strings;
import leap.lang.path.Paths;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;
import leap.lang.resource.Resources;
import leap.lang.servlet.ServletResource;

public class WebjarsAssetResolver extends ServletAssetResolver {

	protected static final String WEBJARS_PREFIX = "/webjars";
	
	@Override
    protected Resource getLocaleResource(String resourcePath, Locale locale) {
		if(Strings.startsWith(resourcePath, WEBJARS_PREFIX + "/")) {
			return super.getLocaleResource(resourcePath, locale);
		}

		Resource sr = super.getLocaleResource(getResourcePath(WEBJARS_PREFIX, resourcePath), locale);
		if(null != sr && sr.exists()) {
			return sr;
		}
		
		int slashIndex = resourcePath.indexOf('/', 1);
		if(slashIndex > 0) {
			String name = resourcePath.substring(0,slashIndex);
			String path = resourcePath.substring(slashIndex);
			
			ResourceSet rs =
					Resources.scan("classpath*:/META-INF/resources/webjars/" + Paths.prefixWithoutSlash(name) + "/*" + path);
			
			if(!rs.isEmpty()){
				Resource cr = rs.first();
				
				String servletPath = cr.getClasspath().substring("/META-INF/resources".length());
				return super.getLocaleResource(servletPath, locale);
			}
		}

		return null;
    }

	@Override
    public void postCreate(BeanFactory beanFactory) throws Throwable {
		this.prefix = "";
		super.postCreate(beanFactory);
    }
}