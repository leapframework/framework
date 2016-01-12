/*
 * Copyright 2015 the original author or authors.
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

import leap.core.web.assets.Asset;
import leap.core.web.assets.AssetBundle;
import leap.core.web.assets.AssetManager;
import leap.core.web.assets.AssetType;
import leap.lang.Strings;
import leap.lang.http.ContentTypes;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

public class BundledAsset extends TextAsset {
    private static final Log log = LogFactory.get(BundledAsset.class);
    
    protected final AssetBundler bundler;
    protected final AssetBundle  bundle;
    
    private long[] loadedAtArray;
    
    public BundledAsset(AssetManager manager, AssetBundler bundler, AssetBundle bundle, String text, String debugText) {
        super(manager, bundle.getPath(), text, getContentType(bundle.getType()));
        
        this.bundler = bundler;
        this.bundle  = bundle;
        
        if(null != debugText && !debugText.equals(text)) {
            this.debugResource = new TextAssetResource(manager, this, Strings.getBytesUtf8(debugText));
        }
        
        loadedAtArray = new long[bundle.getAssets().length];
        updateLoadedAtArray();
    }
    
    protected void updateLoadedAtArray() {
        for(int i=0;i<loadedAtArray.length;i++) {
            Asset asset = bundle.getAssets()[i];
            loadedAtArray[i] = asset.getLoadedAt();
        }
    }

    @Override
    public boolean isText() {
        return false;
    }

    @Override
    public boolean reloadable() {
        return true;
    }

    @Override
    public boolean isBundle() {
        return true;
    }

    @Override
    public boolean reload() {
        boolean reload = false;
        
        for(int i=0;i<loadedAtArray.length;i++) {
            Asset asset = bundle.getAssets()[i];
            if(asset.reloadable() && asset.getLoadedAt() > loadedAtArray[i]) {
                reload = true;
                break;
            }
        }

        if(reload) {
            updateLoadedAtArray();
            try {
                log.info("Bunlde asset '{}' reloaded", path);
                Asset newBundleAsset = bundler.bundle(bundle);
                if(!resource.isExpired()){
                	resource.expire();
                }
                if(!debugResource.isExpired()){
                	 debugResource.expire();
                }
                this.resource = newBundleAsset.getResource();
                this.debugResource = newBundleAsset.getDebugResource();
            } catch (IOException e) {
                log.error("Error realoding bundle asset : {}", path, e);
            }
        }
        return reload;
    }

    protected static String getContentType(AssetType type) {
        if(type.isCSS()) {
            return ContentTypes.TEXT_CSS_UTF8;
        }
        
        if(type.isJS()) {
            return ContentTypes.TEXT_JAVASCRIPT_UTF8;
        }
        
        throw new IllegalStateException("Not supported asset type : " + type);
    }
    
}