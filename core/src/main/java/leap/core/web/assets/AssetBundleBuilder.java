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
package leap.core.web.assets;

import java.util.ArrayList;
import java.util.List;

import leap.lang.Args;
import leap.lang.Buildable;

public class AssetBundleBuilder implements Buildable<AssetBundle> {

    protected AssetType   type;
    protected String      path;
    protected List<Asset> assets = new ArrayList<Asset>();
    
    public boolean hasType() {
        return null != type;
    }
    
    public boolean isCssType() {
        return type == AssetType.CSS;
    }
    
    public boolean isJsType() {
        return type == AssetType.JS;
    }
    
    public boolean hasAssets() {
        return !assets.isEmpty();
    }
    
    public AssetBundleBuilder cssType() {
        this.type = AssetType.CSS;
        return this;
    }
    
    public AssetBundleBuilder jsType() {
        this.type = AssetType.JS;
        return this;
    }

    public String getPath() {
        return path;
    }

    public AssetBundleBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    public AssetType getType() {
        return type;
    }

    public AssetBundleBuilder setType(AssetType type) {
        this.type = type;
        return this;
    }

    public List<Asset> getAssets() {
        return assets;
    }
    
    public AssetBundleBuilder addAsset(Asset asset) {
        assets.add(asset);
        return this;
    }

    @Override
    public AssetBundle build() {
        return new SimpleAssetBundle(path, type, assets.toArray(new Asset[]{}));
    }

    protected static class SimpleAssetBundle implements AssetBundle {
        
        protected final String    path;
        protected final AssetType type;
        protected final Asset[]   assets;
        
        public SimpleAssetBundle(String path, AssetType type, Asset[] assets) {
            Args.notEmpty(path,   "path");
            Args.notNull(type ,   "type");
            Args.notEmpty(assets, "assets");
            this.path   = path;
            this.type   = type;
            this.assets = assets;
        }

        public AssetType getType() {
            return type;
        }

        public String getPath() {
            return path;
        }

        public Asset[] getAssets() {
            return assets;
        }
        
    }

}