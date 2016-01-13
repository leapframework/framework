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

import javax.servlet.ServletContext;

import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.web.ServletContextAware;
import leap.lang.Args;
import leap.lang.exception.ObjectExistsException;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;

public class DefaultAssetManager implements AssetManager,ServletContextAware {

    private static final leap.lang.logging.Log log = LogFactory.get(DefaultAssetManager.class);

    protected @Inject @M AssetConfig    config;
    protected @Inject @M AssetStrategy  strategy;
    protected @Inject @M AssetSource    source;
    protected @Inject @M AssetBundler[] bundlers;

    protected ServletContext sc;
    
    @Override
    public ServletContext getServletContext() {
        return sc;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.sc = servletContext;
    }
    
    @Override
    public AssetConfig getConfig() {
        return config;
    }
    
    @Override
    public AssetSource getSource() {
        return source;
    }

    @Override
    public String getFingerprint(byte[] content) {
        return strategy.getFingerprint(content);
    }
    
    @Override
    public String getPathWithFingerprint(String path, String fingerprint) {
        return strategy.getPathWithFingerprint(path, fingerprint);
    }

    @Override
    public String prefixWithoutAssetsPath(String path) {
        if(null == path) {
            return null;
        }
        
        if(path.startsWith(config.getPathPrefix())) {
            return path.substring(config.getPathPrefix().length());
        }
        
        if(path.startsWith(config.getPublicDirectory())) {
            return path.substring(config.getPublicDirectory().length());
        }
        
        return path;
    }

    @Override
    public String prefixWithAssetsPath(Asset asset) {
        return config.getPathPrefix() + Paths.prefixWithoutSlash(asset.getPath());
    }
    
    @Override
    public String prefixWithAssetsPath(String path) {
        if(null == path) {
            return null;
        }
        return config.getPathPrefix() + Paths.prefixWithoutSlash(path);
    }

    @Override
    public String getClientPath(String path) {
        if(null == path) {
            return null;
        }
        return sc.getContextPath() + config.getPathPrefix() + Paths.prefixWithoutSlash(path);
    }

    @Override
    public Asset createBundleAsset(AssetBundle bundle) {
        return createBundleAsset(bundle, false);
    }

    @Override
    public Asset createBundleAssetOverrided(AssetBundle bundle) {
        return createBundleAsset(bundle, true);
    }

    protected Asset createBundleAsset(AssetBundle bundle, boolean override) {
        String path = bundle.getPath();
        Args.notEmpty(path, "path");
        Args.assertTrue(!path.startsWith("/"), "The bundle's path must not starts with '/'");

        boolean exists = false;
        Asset asset = source.getAsset(path);
        if(null != asset) {
            exists = true;
            if(!asset.isBundle() || !override) {
                throw new ObjectExistsException("Asset '" + path + "' aleady exists, cannot create bundle");
            }
        }

        asset = null;
        try {
            for(AssetBundler bundler : bundlers) {
                if(null != (asset = bundler.bundle(bundle))){
                    break;
                }
            }
        } catch (IOException e) {
            throw new AssetException("Error bundling the asset bundle " + bundle.toString()  + " : " + e.getMessage(), e);
        }

        if(null == asset) {
            throw new IllegalStateException("The bundle's asset type '" + bundle.getType() + "' not supported");
        }

        if(exists) {
            log.info("Creates a bundle asset and replace the existence.");
        }else{
            log.info("Creates a bundle asset and save it.");
        }

        //Save asset.
        source.setAsset(path, asset);

        return asset;
    }

}