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

import javax.servlet.ServletContext;

import leap.lang.exception.ObjectExistsException;

public interface AssetManager {
   
    /**
     * Returns the {@link ServletContext}.
     */
    ServletContext getServletContext();
    
    /**
     * Returns the {@link AssetConfig}.
     */
    AssetConfig getConfig();
    
    /**
     * Returns the {@link AssetSource}.
     */
    AssetSource getSource();
    
    /**
     * Returns the finterprint of asset's content. 
     */
    String getFingerprint(byte[] content);
    
    /**
     * Returns the path with fingerprint.
     */
    String getPathWithFingerprint(String path, String fingerprint);
    
    /**
     * Returns the asset's path without '/assets/' prefix.
     */
    String prefixWithoutAssetsPath(String path);

    /**
     * Returns the path prefix with '/assets/' defined in {@link AssetConfig#getPathPrefix()}.
     *
     * <p/>
     * The path does not include context path.
     */
    String prefixWithAssetsPath(Asset asset);
    
    /**
     * Returns the path prefix with '/assets/' defined in {@link AssetConfig#getPathPrefix()}.
     *
     * <p/>
     * The path does not include context path.
     */
    String prefixWithAssetsPath(String path);

    /**
     * Returns the path prefix with context path and assets path.
     */
    String getClientPath(String path);

    /**
     * Creates an asset of the bundle.
     * 
     * <p>
     * The created asset will be saved to {@link AssetSource} by the manager.
     * 
     * @throws ObjectExistsException if the asset path aleady exists.
     */
    Asset createBundleAsset(AssetBundle bundle) throws ObjectExistsException;

    /**
     * Creates an asset of the bundle.
     *
     * <p/>
     * If the bundle aleady exists, override it.
     *
     * <p>
     * The created asset will be saved to {@link AssetSource} by the manager.
     */
    Asset createBundleAssetOverrided(AssetBundle bundle);
    
}