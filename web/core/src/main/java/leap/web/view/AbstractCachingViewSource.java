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
import java.util.Map;

import leap.core.AppConfig;
import leap.core.AppConfigAware;
import leap.core.AppException;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.cache.Cache;
import leap.core.schedule.Scheduler;
import leap.core.schedule.SchedulerManager;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.Request;
import leap.web.Response;

public abstract class AbstractCachingViewSource implements ViewSource, AppConfigAware {
	
	protected final Log log = LogFactory.get(this.getClass());
	
	protected static final View UNRESOVLED_VIEW = new View() {
		@Override
        public String getContentType(Request request) throws Throwable {
	        return null;
        }

		@Override
        public void render(Request request, Response response) throws Throwable {
        }

		@Override
		public void render(Request request, Response response, ViewData data) throws Exception {
		}
	};
	
    protected @Inject @M AppConfig        appConfig;
    protected @Inject @M SchedulerManager schedulerManager;

    protected Locale                   defaultLocale;
    protected Scheduler                reloadScheduler;
    protected boolean                  reloadSchueduled;
	
	private final Object viewCreationLock = new Object();
	
	@Override
    public void setAppConfig(AppConfig config) {
		this.appConfig     = config;
		this.defaultLocale = config.getDefaultLocale();
    }

	public void setReloadScheduler(Scheduler reloadScheduler) {
		this.reloadScheduler = reloadScheduler;
	}

	@Override
	public View getView(String viewName, Locale locale) {
		if(null == locale){
			locale = defaultLocale;
		}
		
		Cache<Object, View> cache = getViewCache();
		Object key  = getCacheKey(viewName, locale);
		View   view = cache.get(key);
		
		if(null == view){
			synchronized (viewCreationLock) {
				view = cache.get(key);
				if(null == view){
					try {
                        view = resolveView(viewName, locale);
                    } catch (Throwable e) {
                        throw new AppException("Error loading view '" + viewName + "'", e);
                    }
					
					if(null == view){
						view = UNRESOVLED_VIEW;
					}
					
					if(!reloadSchueduled){
						scheduleReload();
					}
					
					cache.put(key, view);
				}
            }
		}
		
		return view == UNRESOVLED_VIEW ? null : view;
	}
	
	protected Object getCacheKey(String viewName, Locale locale) {
		return viewName + "_" + locale;
	}	
	
	protected abstract Cache<Object, View> getViewCache();
	
	protected abstract View resolveView(String viewName, Locale locale) throws Throwable;
	
	protected void scheduleReload() {
		synchronized (this) {
			if(appConfig.isReloadEnabled()){
			    if(reloadScheduler == null) {
			        reloadScheduler = schedulerManager.newFixedThreadPoolScheduler("view-reload");
			    }
				reloadScheduler.scheduleAtFixedRate(new ReloadTask(), 2000); //2 second TODO : hard code
			}
			this.reloadSchueduled = true;
        }
	}
	
	protected final class ReloadTask implements Runnable {
		@Override
        public void run() {
			Map<Object, View> views = getViewCache().getAll();
			
			for(View view : views.values()){
				try {
	                if(view.reload()){
	                	log.info("View [{}] was reloaded",view);
	                }
                } catch (Exception e) {
                	log.warn("Error reloading view [{}]",view,e);
                }
			}
		}
	}
}
