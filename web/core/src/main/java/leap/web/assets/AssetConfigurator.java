/*
 * Copyright 2016 the original author or authors.
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

/**
 * The configurator of {@link AssetConfig}
 */
public interface AssetConfigurator {

    String DEFAULT_PATH_PREFIX		 = "/assets/";
    String DEFAULT_SOURCE_DIRECTORY  = "/WEB-INF/assets/";
    String DEFAULT_PUBLIC_DIRECTORY  = "/static/";
    String DEFAULT_MANIFEST_FILE     = "/WEB-INF/assets.manifest.json";
    String DEFAULT_HASH_ALGORITHM    = "MD5";
    long   DEFAULT_RELOAD_INTERVAL   = 2000; //2 seconds
    int    DEFAULT_CACHE_MAX_AGE	 = 365 * 24 * 60 * 60 ; //1 year in seconds
    int    DEFAULT_GZIP_MIN_LENGTH   = 512;  //512 bytes

    /**
     * Disable web assets.
     */
    default AssetConfigurator disable() {
        return setEnabled(false);
    }

    /**
     * Enables or Disables web assets.
     *
     * <p/>
     * Default is enabled.
     */
    AssetConfigurator setEnabled(boolean enabled);

    /**
     * Sets the minimum length for gzip asset content.
     *
     * <p/>
     * The asset handler do not gzip content if the content-length less then the min-length.
     *
     * <p/>
     * Default is {@link #DEFAULT_GZIP_MIN_LENGTH}.
     */
    AssetConfigurator setGzipMinLength(int length);

    /**
     * Adds an asset folder.
     *
     * @param location The location of asset folder.
     */
    AssetConfigurator addFolder(String location);

    /**
     * Adds an asset folder.
     *
     * @param location the location of asset folder.
     * @param pathPrefix the path prefix of asset folder.
     */
    AssetConfigurator addFolder(String location, String pathPrefix);
}