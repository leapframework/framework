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
package leap.web.view;

import java.util.Locale;

import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.annotation.R;
import leap.core.cache.Cache;
import leap.core.cache.SimpleLRUCache;

public class ServletResourceViewSource extends AbstractCachingViewSource {
	
    protected @R String                             location;
    protected @M Cache<Object, View>                viewCache;
    protected @Inject ServletResourceViewResolver[] viewResolvers;
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	public void setViewCache(Cache<Object, View> viewCache) {
		this.viewCache = viewCache;
	}
	
	@Override
	protected Cache<Object, View> getViewCache() {
		if(null == viewCache){
			viewCache = new SimpleLRUCache<>();
		}
		return viewCache;
	}

	@Override
	protected View resolveView(String viewName, Locale locale) throws Throwable {
		View view = null;
		for(int i=0;i<viewResolvers.length;i++){
			if((view = viewResolvers[i].resolveView(location, viewName, locale)) != null){
				break;
			}
		}
		return view;
	}
}