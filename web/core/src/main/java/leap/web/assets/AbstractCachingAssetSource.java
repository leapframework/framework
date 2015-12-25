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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import leap.core.AppConfig;
import leap.core.AppConfigAware;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.cache.Cache;
import leap.core.schedule.Scheduler;
import leap.core.schedule.SchedulerManager;
import leap.core.web.assets.Asset;
import leap.core.web.assets.AssetConfig;
import leap.core.web.assets.AssetResource;
import leap.core.web.assets.AssetSource;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

public abstract class AbstractCachingAssetSource implements AssetSource,AppConfigAware {

	protected final Log log = LogFactory.get(this.getClass());
	
	protected final Asset UNRESOLVED_ASSET = new AbstractAsset() {
		@Override
        public boolean reloadable() {
	        return false;
        }

		@Override
        public boolean reload() {
	        return false;
        }

		@Override
        public boolean isText() {
	        return false;
        }
	};
	
	protected final AssetResource UNRESOLVED_RESOURCE = new AbstractAssetResource() {
		@Override
        public InputStream getInputStream() throws IOException {
	        return null;
        }
		@Override
		public Reader getReader() throws IOException {
			return null;
		}
	};

    protected @Inject @M AssetConfig      assetConfig;
    protected @Inject @M SchedulerManager schedulerManager;
	
    protected Locale                      defaultLocale;
    protected Scheduler                   reloadScheduler;
    protected boolean                     reloadSchueduled;
    protected Map<Object, AssetResource>  resourceCache       = new ConcurrentHashMap<Object, AssetResource>();
	
	@Override
    public void setAppConfig(AppConfig config) {
		this.defaultLocale = config.getDefaultLocale();
    }
	
	public void setReloadScheduler(Scheduler reloadScheduler) {
		this.reloadScheduler = reloadScheduler;
	}
	
	@Override
    public void setAsset(String path, Asset asset){
	    getAssetCache().put(getAssetCacheKey(path, defaultLocale), asset);
    }
	
	@Override
	public Asset getAsset(String path,Locale locale){
		if(null == locale){
			locale = defaultLocale;
		}
		
		Cache<Object, Asset> cache = getAssetCache();
		
		Object cacheKey = getAssetCacheKey(path, locale);
		Asset asset = cache.get(cacheKey);
		
		if (asset == null) {
			try {
                asset = loadAsset(path, locale);
            } catch (Throwable e) {
                throw new AssetException("Error loading asset '" + path + "'", e);
            }

			if (asset == null) {
				asset = UNRESOLVED_ASSET;
			}
			
			if (asset != null) {
				cache.put(cacheKey, asset);
				
				if (log.isTraceEnabled()) {
					if(asset == UNRESOLVED_ASSET){
						log.trace("Cached asset [" + cacheKey + " : unresolved]");
					}else{
						log.trace("Cached asset [" + cacheKey + " : " + asset.toString() + "]");
					}
				}
			}
		}
		
		if(asset != UNRESOLVED_ASSET){
			if(!reloadSchueduled){
				scheduleReload();
			}
			return asset;
		}else{
			return null;
		}
    }
	
	@Override
    public AssetResource getAssetResource(String path, String[] pathAndFingerprint, Locale locale) {
		if(null == locale){
			locale = defaultLocale;
		}
		
		String assetPath = path;
		if(null != pathAndFingerprint) {
			assetPath = pathAndFingerprint[0];
		}
		
		Object cacheKey = getAssetCacheKey(assetPath, locale);
		AssetResource resource = resourceCache.get(cacheKey);
		
		if(null != resource && resource.isExpired()){
			resourceCache.remove(cacheKey);
			resource = null;
		}
		
		if(resource == null){
			resource = findAssetResoruce(path, pathAndFingerprint, locale);

			if (resource == null) {
				resource = UNRESOLVED_RESOURCE;
			}
			
			if (resource != null) {
				this.resourceCache.put(cacheKey, resource);
			}
		}

		return resource == UNRESOLVED_RESOURCE ? null : resource;
	}

	protected Object getAssetCacheKey(String assetPath, Locale locale) {
		return assetPath + "_" + locale;	
	}
	
	protected Object getResoruceCacheKey(String requestUri,Locale locale){
		return requestUri + "_" + locale;
	}
	
	protected AssetResource findAssetResoruce(String path, String[] pathAndFingerprint,  Locale locale) {
		String assetPath   = path;
		String fingerprint = null;
		
		if(null != pathAndFingerprint){
			assetPath = pathAndFingerprint[0];
			fingerprint = pathAndFingerprint[1];
		}

		Asset asset = getAsset(assetPath, locale);
		if(null == asset){
			return null;
		}
		
		AssetResource resource = null;
		
		if(asset.getResource().getPath().equals(path)){
			resource = asset.getResource();
		} else if(asset.getDebugResource().getPath().equals(path)){
			resource = asset.getDebugResource();
		}
		
		if(null == resource) {
			return asset.getDebugResource();
		}else if(null != fingerprint && !fingerprint.equals(resource.getFingerprint())){
			return null;
		}
		
		return resource;
	}
	
	protected abstract Cache<Object, Asset> getAssetCache();
	
	protected abstract Asset loadAsset(String assetPath, Locale locale) throws Throwable;
	
	protected void scheduleReload() {
		synchronized (this) {
			if(assetConfig.isReloadEnabled()){
			    if(null == reloadScheduler) {
			        reloadScheduler = schedulerManager.newFixedThreadPoolScheduler("assets-reload");
			    }
				reloadScheduler.scheduleAtFixedRate(new ReloadTask(), assetConfig.getReloadInterval());
			}
			this.reloadSchueduled = true;
        }
	}
	
	protected final class ReloadTask implements Runnable {
		@Override
        public void run() {
			Map<Object, Asset> assets = getAssetCache().getAll();
			
			for(Asset asset : assets.values()){
				try {
	                if(asset.reload()){
	                	log.info("Asset [{}] was reloaded",asset.getPath());
	                }
                } catch (Exception e) {
                	log.warn("Error reloading asset [{}]",asset.getPath(),e);
                }
			}
		}
	}

}
