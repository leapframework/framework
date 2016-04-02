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
package leap.htpl;

import leap.core.AppConfig;
import leap.core.AppConfigAware;
import leap.core.cache.Cache;
import leap.core.validation.annotations.NotNull;

import java.io.Writer;
import java.util.Locale;

public abstract class AbstractCachingHtplTemplateSource implements HtplTemplateSource,AppConfigAware {
	
	protected static final HtplTemplate UNRESOLVED_TEMPLATE = new AbstractHtplTemplate() {
		public Object getSource() {return null;}
		public HtplResource getResource() { return null;}
        public HtplPage createPage() {return null;}
		public void render(HtplTemplate parent, HtplContext context, HtplWriter writer) {}
		public void render(HtplContext context, HtplWriter writer) {}
		public void render(HtplContext context, Writer writer) {}
		public boolean reloadable() {return false;}
		public Locale getLocale() {return null;}
		public HtplEngine getEngine() {return null;}
		public HtplDocument getDocument() {return null;}
    };
	
	protected @NotNull Locale defaultLocale;
	
	private final Object templateCreationLock = new Object();
	
	@Override
    public void setAppConfig(AppConfig config) {
		this.defaultLocale = config.getDefaultLocale();
    }

	@Override
	public HtplTemplate getTemplate(String name, Locale locale) throws Throwable {
		if(null == locale){
			locale = defaultLocale;
		}
		
		Cache<Object, HtplTemplate> cache = getCache();
		Object key = getCacheKey(name, locale);
		HtplTemplate t = cache.get(key);
		
		if(null == t){
			synchronized (templateCreationLock) {
				t = cache.get(key);
				if(null == t){
					t = resolveTemplate(name,locale);
					
					if(null == t){
						t = UNRESOLVED_TEMPLATE;
						if(isCacheUnresolved()){
							cache.put(key, t);
						}
					}else{
						cache.put(key, t);
					}
				}
            }
		}
		
		return UNRESOLVED_TEMPLATE == t ? null : t;
	}
	
	protected Object getCacheKey(String name, Locale locale) {
		return name + "_" + locale;
	}	
	
	protected boolean isCacheUnresolved() {
		return false;
	}
	
	protected abstract Cache<Object, HtplTemplate> getCache();
	
	protected abstract HtplTemplate resolveTemplate(String name, Locale locale);
}
