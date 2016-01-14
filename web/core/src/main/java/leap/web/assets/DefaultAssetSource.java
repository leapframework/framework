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

import java.util.List;
import java.util.Locale;

import leap.core.AppConfigAware;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.cache.Cache;
import leap.lang.Locales;
import leap.lang.Strings;
import leap.lang.path.Paths;
import leap.lang.resource.Resource;

public class DefaultAssetSource extends AbstractCachingAssetSource implements AssetSource,AppConfigAware {
	
	protected @Inject AssetConfig  config;
    protected @Inject AssetManager manager;
	
	/** The underlying resolvers to resolve assets*/
	protected @Inject AssetResolver[] resolvers;

    @Inject(name="assets")
    protected @M Cache<Object, Asset> assetCache;

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

		return loadAssetFromExternalFolders(assetPath, locale);
	}

	protected Asset loadAssetFromExternalFolders(String assetPath, Locale locale) throws Throwable {
		List<AssetFolder> folders = config.getFolders();
		if(folders.isEmpty()) {
			return null;
		}

		Asset asset;

		for(AssetFolder folder : folders) {

			if(null != (asset = loadAssetFromExternalFolder(assetPath, locale, folder))) {
				return asset;
			}

		}

		return null;
	}

	protected Asset loadAssetFromExternalFolder(String assetPath, Locale locale, AssetFolder folder) throws Throwable {
        String filepath = assetPath;

        if(!Strings.isEmpty(folder.getPathPrefix())) {

            String prefix = Paths.prefixWithoutSlash(folder.getPathPrefix());

            if(!assetPath.startsWith(prefix)) {
                return null;
            }

            filepath = Strings.removeStart(filepath, prefix);
        }

        String[] filepaths = Locales.getLocaleFilePaths(locale, filepath);

        Resource resource = null;
        for(String path : filepaths) {
            resource = folder.getRelativeResource(path);
            if(null != resource && resource.exists()) {
                break;
            }
        }

        if(null == resource || !resource.exists()) {
            return null;
        }

		return new SimpleAsset(manager, assetPath, resource);
	}
}