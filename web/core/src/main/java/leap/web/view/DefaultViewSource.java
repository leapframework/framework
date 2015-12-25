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
import leap.core.cache.Cache;
import leap.core.validation.annotations.NotNull;

public class DefaultViewSource extends AbstractCachingViewSource implements ViewSource {
	
	protected @Inject @M ViewResolver viewResolver;
	
	/** Fast access cache for Views, returning already cached instances without a global lock */
	@Inject(name="views")
	protected @NotNull Cache<Object, View> viewCache;

	public void setViewResolver(ViewResolver viewResolver) {
		this.viewResolver = viewResolver;
	}

	public void setViewCache(Cache<Object, View> viewCache) {
		this.viewCache = viewCache;
	}

	@Override
    protected Cache<Object, View> getViewCache() {
	    return viewCache;
    }

	@Override
    protected View resolveView(String viewName, Locale locale) throws Throwable {
	    return viewResolver.resolveView(viewName, locale);
    }

}