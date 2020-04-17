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

import leap.core.cache.Cache;
import leap.core.cache.SimpleLRUCache;
import leap.core.validation.annotations.NotNull;
import leap.lang.resource.Resource;

public class SimpleCachingAssetSource extends AbstractCachingAssetSource {
	
	protected @NotNull Cache<Object, Asset> assetCache;
	protected @NotNull AssetResolver 		resolver;
	protected Resource                      dirResource;
	
	@Override
    protected Cache<Object, Asset> getAssetCache() {
		if(null == assetCache){
			assetCache = new SimpleLRUCache<>();
		}
	    return assetCache;
    }
	
	public void setAssetCache(Cache<Object, Asset> cache) {
		this.assetCache = cache;
	}

	public void setResolver(AssetResolver resolver) {
		this.resolver = resolver;
	}

	public void setDirResource(Resource dirResource) {
		this.dirResource = dirResource;
	}

	@Override
	protected Asset loadAsset(String assetPath, Locale locale) throws Throwable {
		return resolver.resolveAsset(assetPath, locale, dirResource);
	}

}