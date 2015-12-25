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

import java.nio.charset.Charset;

import leap.core.AppConfig;
import leap.core.AppConfigAware;
import leap.core.annotation.Configurable;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.web.assets.AssetConfig;
import leap.core.web.assets.AssetStrategy;
import leap.lang.path.Paths;

@Configurable(prefix="webassets")
public class DefaultAssetConfig implements AssetConfig, AppConfigAware {
	
	protected boolean disabled;
	protected boolean debug;
	protected Charset charset;
	protected boolean gzipEnabled      = true;
	protected int	  gzipMinLength    = DEFAULT_GZIP_MIN_LENGTH;
	protected String  pathPrefix	   = DEFAULT_PATH_PREFIX;
	protected String  sourceDirectory  = DEFAULT_SOURCE_DIRECTORY;
	protected String  publicDirectory  = DEFAULT_PUBLIC_DIRECTORY;
	protected boolean publishEnabled   = false;
	protected String  publishDirectory = null;
	protected String  manifestFile     = DEFAULT_MANIFEST_FILE;
	protected boolean reloadEnabled    = false;
	protected long    reloadInterval   = DEFAULT_RELOAD_INTERVAL;
	protected String  hashAlgorithm    = DEFAULT_HASH_ALGORITHM;
	protected int	  cacheMaxAge      = DEFAULT_CACHE_MAX_AGE;
	
	protected @Inject @M AssetStrategy fingerprintStrategy;
	
	@Override
    public void setAppConfig(AppConfig config) {
		this.debug         = config.isDebug();
		this.charset       = config.getDefaultCharset();
		this.reloadEnabled = debug ? true : config.isReloadEnabled();
    }
	
	@Override
	public boolean isDisabled() {
		return disabled;
	}

	@Configurable.Property
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	@Override
	public boolean isDebug() {
		return debug;
	}
	
	@Override
    public boolean isGzipEnabled() {
	    return gzipEnabled;
    }
	
	@Override
	public int getGzipMinLength() {
		return gzipMinLength;
	}

	@Configurable.Property
	public void setGzipMinLength(int gzipMinLength) {
		this.gzipMinLength = gzipMinLength;
	}

	@Configurable.Property
	public void setGzipEnabled(boolean gzipEnabled) {
		this.gzipEnabled = gzipEnabled;
	}

	@Override
    public Charset getCharset() {
	    return charset;
    }
	
	@Configurable.Property
	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	@Configurable.Property
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	@Override
	public String getPathPrefix() {
		return pathPrefix;
	}

	@Configurable.Property
	public void setPathPrefix(String pathPrefix) {
		this.pathPrefix = Paths.prefixAndSuffixWithSlash(pathPrefix);
	}

	@Override
	public String getSourceDirectory() {
		return sourceDirectory;
	}
	
	@Configurable.Property
	public void setSourceDirectory(String sourceDirectory) {
		this.sourceDirectory = Paths.prefixAndSuffixWithSlash(sourceDirectory);
	}

	@Override
	public String getPublicDirectory() {
		return publicDirectory;
	}
	
	@Configurable.Property
	public void setPublicDirectory(String publicDirectory) {
		this.publicDirectory = Paths.prefixAndSuffixWithSlash(publicDirectory);
	}

	@Override
	public boolean isPublishEnabled() {
		return publishEnabled;
	}

	@Configurable.Property
	public void setPublishEnabled(boolean publishEnabled) {
		this.publishEnabled = publishEnabled;
	}

	@Override
	public String getPublishDirectory() {
		return publishDirectory;
	}

	@Configurable.Property
	public void setPublishDirectory(String publishDirectory) {
		this.publishDirectory = publishDirectory;
	}

	@Override
    public String getManifestFile() {
	    return manifestFile;
    }

	@Configurable.Property
	public void setManifestFile(String manifestFile) {
		this.manifestFile = manifestFile;
	}
	
	public boolean isReloadEnabled() {
		return reloadEnabled;
	}

	@Configurable.Property
	public void setReloadEnabled(boolean manifestReloadEnabled) {
		this.reloadEnabled = manifestReloadEnabled;
	}

	public long getReloadInterval() {
		return reloadInterval;
	}

	@Configurable.Property
	public void setReloadInterval(long manifestReloadInterval) {
		this.reloadInterval = manifestReloadInterval;
	}

	@Override
    public String getHashAlgorithm() {
	    return hashAlgorithm;
    }

	@Configurable.Property
	public void setHashAlgorithm(String hashAlgorithm) {
		this.hashAlgorithm = hashAlgorithm;
	}

	public int getCacheMaxAge() {
		return cacheMaxAge;
	}

	@Configurable.Property
	public void setCacheMaxAge(int maxCacheAge) {
		this.cacheMaxAge = maxCacheAge;
	}

	public AssetStrategy getFingerprintStrategy() {
		return fingerprintStrategy;
	}

	public void setFingerprintStrategy(AssetStrategy strategy) {
		this.fingerprintStrategy = strategy;
	}
}