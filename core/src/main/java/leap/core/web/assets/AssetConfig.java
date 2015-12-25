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
package leap.core.web.assets;

import java.nio.charset.Charset;

import leap.core.AppConfig;

public interface AssetConfig {
	
	String DEFAULT_PATH_PREFIX		 = "/assets/";
	String DEFAULT_SOURCE_DIRECTORY  = "/WEB-INF/assets/";
	String DEFAULT_PUBLIC_DIRECTORY  = "/static/";
	String DEFAULT_MANIFEST_FILE     = "/WEB-INF/assets.manifest.json";
	String DEFAULT_HASH_ALGORITHM    = "MD5";
	long   DEFAULT_RELOAD_INTERVAL   = 2000;	 //2 seconds
	int    DEFAULT_CACHE_MAX_AGE	 = 365 * 24 * 60 * 60 ; //1 year in seconds
	int    DEFAULT_GZIP_MIN_LENGTH   = 512; //512 bytes
	
	/**
	 * Returns <code>true</code> if assets handling was disabled.
	 */
	boolean isDisabled();
	
	/**
	 * The default value is <code>true</code> in development environment.
	 * 
	 * @see AppConfig#getProfile()
	 */
	boolean isDebug();
	
	/**
	 * The default value is <code>true</code>.
	 */
	boolean isGzipEnabled();
	
	/**
	 * Returns the minimum length for gzip.
	 * 
	 * <p>
	 * If the content length less than minimum length, the asset will not be compressed.
	 */
	int getGzipMinLength();
	
	/**
	 * Returns the charset to processing asset resources.
	 * 
	 * <p>
	 * Default is {@link AppConfig#getDefaultCharset()}
	 */
	Charset getCharset();
	
	/**
	 * Returns the path for handling assets resources. 
	 * 
	 * <p>
	 * The path must start with '/' and ends with '/'.
	 * 
	 * <p>
	 * Default is {@link #DEFAULT_PATH_PREFIX}
	 */
	String getPathPrefix();	
	
	/**
	 * Returns the source assets directory in webapp.
	 * 
	 * <p>
	 * Default is {@link #DEFAULT_SOURCE_DIRECTORY}
	 */
	String getSourceDirectory();

	/**
	 * Returns the assets directory in webapp.
	 * 
	 * <p>
	 * Default is {@link #DEFAULT_PUBLIC_DIRECTORY}
	 */
	String getPublicDirectory();
	
	/**
	 * If <code>true</code>, assets will be copied to the publish directory.
	 * 
	 * <p>
	 * Default is <code>false</code>
	 */
	public boolean isPublishEnabled();
	
	/**
	 * Returns the publish directory.
	 * 
	 * <p>
	 * Default is <code>null</code>.
	 */
	String getPublishDirectory();
	
	/**
	 * Returns the assets manifest file's path.
	 * 
	 * <p>
	 * Default is {@link #DEFAULT_MANIFEST_FILE}
	 */
	String getManifestFile();	
	
	/**
	 * Returns <code>true</code> if enable the reloading of asset resources.
	 * 
	 * <p>
	 * The default value is {@link AppConfig#isReloadEnabled()} if not in debug mode.
	 * 
	 * <p>
	 * In debug mode, the default value is <code>true</code>
	 * 
	 * @see #isDebug()
	 */
	boolean isReloadEnabled();
	
	/**
	 * Returns the interval in milliseconds to reload asset resources.
	 * 
	 * <p>
	 * Default is {@link #DEFAULT_RELOAD_INTERVAL}.
	 */
	long getReloadInterval();
	
	/**
	 * Returns the hash algorithm.
	 * 
	 * <p>
	 * Default is {@link #DEFAULT_HASH_ALGORITHM}
	 */
	String getHashAlgorithm();	
	
	/**
	 * Returns the value of max-age (seconds) in Cache-Control header.
	 */
	int getCacheMaxAge();
}