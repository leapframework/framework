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

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import leap.core.AppConfig;
import leap.core.AppConfigAware;
import leap.core.BeanFactory;
import leap.core.annotation.Configurable;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.ioc.PostCreateBean;
import leap.lang.Args;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;

@Configurable(prefix="webassets")
public class DefaultAssetConfig implements AssetConfig, AssetConfigurator, AppConfigAware, PostCreateBean {

    private static final Log log = LogFactory.get(DefaultAssetConfig.class);

    protected AppConfig config;

    protected boolean enabled = true;
    protected boolean debug;
    protected Charset charset;
    protected boolean gzipEnabled      = true;
    protected int     gzipMinLength    = DEFAULT_GZIP_MIN_LENGTH;
    protected String  pathPrefix       = DEFAULT_PATH_PREFIX;
    protected String  sourceDirectory  = DEFAULT_SOURCE_DIRECTORY;
    protected String  publicDirectory  = DEFAULT_PUBLIC_DIRECTORY;
    protected boolean publishEnabled   = false;
    protected String  publishDirectory = null;
    protected String  manifestFile     = DEFAULT_MANIFEST_FILE;
    protected boolean reloadEnabled    = false;
    protected long    reloadInterval   = DEFAULT_RELOAD_INTERVAL;
    protected String  hashAlgorithm    = DEFAULT_HASH_ALGORITHM;
    protected int     cacheMaxAge      = DEFAULT_CACHE_MAX_AGE;

    protected List<AssetFolder> folders              = new ArrayList<>();
    protected List<AssetFolder> foldersImmutableView = Collections.unmodifiableList(folders);

    protected @Inject @M AssetStrategy fingerprintStrategy;
	
	@Override
    public void setAppConfig(AppConfig config) {
        this.config        = config;
		this.debug         = config.isDebug();
		this.charset       = config.getDefaultCharset();
		this.reloadEnabled = debug ? true : config.isReloadEnabled();
    }
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Configurable.Property
	public AssetConfigurator setEnabled(boolean enabled) {
		this.enabled = enabled;
		return this;
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
	public AssetConfigurator setGzipMinLength(int gzipMinLength) {
		this.gzipMinLength = gzipMinLength;
        return this;
	}

	@Configurable.Property
	public AssetConfigurator setGzipEnabled(boolean gzipEnabled) {
		this.gzipEnabled = gzipEnabled;
        return this;
	}

	@Override
    public Charset getCharset() {
	    return charset;
    }
	
	@Configurable.Property
	public AssetConfigurator setDebug(boolean debug) {
		this.debug = debug;
        return this;
	}
	
	@Override
	public String getPathPrefix() {
		return pathPrefix;
	}

	@Configurable.Property
	public AssetConfigurator setPathPrefix(String pathPrefix) {
		this.pathPrefix = Paths.prefixAndSuffixWithSlash(pathPrefix);
        return this;
	}

	public String getSourceDirectory() {
		return sourceDirectory;
	}
	
	@Configurable.Property
	public AssetConfigurator setSourceDirectory(String sourceDirectory) {
		this.sourceDirectory = Paths.prefixAndSuffixWithSlash(sourceDirectory);
        return this;
	}

	@Override
	public String getPublicDirectory() {
		return publicDirectory;
	}
	
	@Configurable.Property
	public AssetConfigurator setPublicDirectory(String publicDirectory) {
		this.publicDirectory = Paths.prefixAndSuffixWithSlash(publicDirectory);
        return this;
	}

	@Override
	public boolean isPublishEnabled() {
		return publishEnabled;
	}

	@Configurable.Property
	public AssetConfigurator setPublishEnabled(boolean enabled) {
		this.publishEnabled = enabled;
        return this;
	}

	@Override
	public String getPublishDirectory() {
		return publishDirectory;
	}

	@Configurable.Property
	public AssetConfigurator setPublishDirectory(String dir) {
		this.publishDirectory = dir;
        return this;
	}

    public String getManifestFile() {
	    return manifestFile;
    }

	@Configurable.Property
	public AssetConfigurator setManifestFile(String file) {
		this.manifestFile = file;
        return this;
	}
	
	public boolean isReloadEnabled() {
		return reloadEnabled;
	}

	@Configurable.Property
	public AssetConfigurator setReloadEnabled(boolean reloadEnabled) {
		this.reloadEnabled = reloadEnabled;
        return this;
	}

	public long getReloadInterval() {
		return reloadInterval;
	}

	@Configurable.Property
	public AssetConfigurator setReloadInterval(long reloadInterval) {
		this.reloadInterval = reloadInterval;
        return this;
	}

	@Override
    public String getHashAlgorithm() {
	    return hashAlgorithm;
    }

	@Configurable.Property
	public AssetConfigurator setHashAlgorithm(String hashAlgorithm) {
		this.hashAlgorithm = hashAlgorithm;
        return this;
	}

	public int getCacheMaxAge() {
		return cacheMaxAge;
	}

	@Configurable.Property
	public AssetConfigurator setCacheMaxAge(int maxCacheAge) {
		this.cacheMaxAge = maxCacheAge;
        return this;
	}

	public AssetStrategy getFingerprintStrategy() {
		return fingerprintStrategy;
	}

	public AssetConfigurator setFingerprintStrategy(AssetStrategy strategy) {
		this.fingerprintStrategy = strategy;
        return this;
	}

    @Override
    public List<AssetFolder> getFolders() {
        return foldersImmutableView;
    }

    @Override
    public AssetConfigurator addFolder(String location) {
        Args.notEmpty(location, "location");
        folders.add(createAssetFolder(location, null));
        return this;
    }

    @Override
    public AssetConfigurator addFolder(String location, String pathPrefix) {
        Args.notEmpty(location, "location");
        folders.add(createAssetFolder(location, pathPrefix));
        return this;
    }

    @Override
    public void postCreate(BeanFactory factory) throws Throwable {
        AssetConfigExtension ext = config.getExtension(AssetConfigExtension.class);
        if(null == ext) {
            return;
        }
        for(AssetConfigExtension.AssetFolderConfig f : ext.getFolders()) {
            folders.add(createAssetFolder(f.getLocation(), f.getPathPrefix()));
        }
    }

    protected AssetFolder createAssetFolder(String location, String pathPrefix) {
        log.info("Assets folder : {}", location);
        return new FileAssetFolder(new File(location), Paths.prefixAndSuffixWithSlash(pathPrefix));
    }
}