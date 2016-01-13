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
package leap.web.theme;

import java.util.Locale;

import leap.web.assets.Asset;
import leap.web.assets.AssetResource;
import leap.web.assets.AssetSource;

public class ThemeOrDefaultAssetSource implements AssetSource {
	
	private AssetSource themeAssetSource;
	private AssetSource defaultAssetSource;
	
	public ThemeOrDefaultAssetSource(AssetSource themeAssetSource,AssetSource defaultAssetSource) {
		this.themeAssetSource   = themeAssetSource;
		this.defaultAssetSource = defaultAssetSource;
	}

	@Override
    public Asset getAsset(String path, Locale locale) {
		Asset asset = themeAssetSource.getAsset(path, locale);
	    return null == asset ? defaultAssetSource.getAsset(path, locale) : asset;
    }
	
	@Override
    public void setAsset(String path, Asset asset) {
	    defaultAssetSource.setAsset(path, asset);
    }

    @Override
    public AssetResource getAssetResource(String path, String[] pathAndFingerprint, Locale locale) {
		AssetResource r = themeAssetSource.getAssetResource(path, pathAndFingerprint, locale);
	    return null == r ? defaultAssetSource.getAssetResource(path, pathAndFingerprint, locale) : r;
    }

}
