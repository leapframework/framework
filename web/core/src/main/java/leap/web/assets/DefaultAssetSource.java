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

import leap.core.AppConfigAware;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.cache.Cache;
import leap.core.web.assets.Asset;
import leap.core.web.assets.AssetSource;

public class DefaultAssetSource extends AbstractCachingAssetSource implements AssetSource,AppConfigAware {
	
	@Inject(name="assets")
	protected @M Cache<Object, Asset> assetCache;
	
	/** The underlying resolvers to resolve assets*/
	protected @Inject AssetResolver[] resolvers;
	
	@Override
    protected Cache<Object, Asset> getAssetCache() {
	    return assetCache;
    }

	public void setAssetCache(Cache<Object, Asset> assetCache) {
		this.assetCache = assetCache;
	}
	
    protected Asset loadAsset(String assetPath, Locale locale) throws Throwable {
		Asset a;
		
		for(AssetResolver resolver : resolvers) {
			if((a = resolver.resolveAsset(assetPath, locale)) != null) {
				return a;
			}
		}
		
		return null;
	}

}