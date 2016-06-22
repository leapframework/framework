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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

import leap.lang.Strings;
import leap.lang.io.Files;
import leap.lang.io.IO;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;

public abstract class AbstractAssetResource implements AssetResource {
    
    private static final Log log = LogFactory.get(AbstractAssetResource.class);

    protected AssetManager   manager;
    protected Asset          asset;
    protected String         path;
    protected boolean        debug;
    protected String         clientUrl;
    protected String         serverPath;
    protected String         fingerprint;
    protected long           lastModified;
    protected long           contentLength;
    protected boolean        expired;
    
    protected AbstractAssetResource() {
        
    }
	
	public AbstractAssetResource(AssetManager manager, Asset asset) {
	    this.manager = manager;
		this.asset   = asset;
	}

	@Override
    public Asset getAsset() {
	    return asset;
    }

	@Override
    public String getPath() {
	    return path;
    }
	
	@Override
	public boolean isDebug() {
		return debug;
	}
	
	@Override
	public String getClientUrl() {
		return clientUrl;
	}
	
	@Override
	public String getServerPath() {
		return serverPath;
	}

	@Override
	public String getFingerprint() {
		return fingerprint;
	}

	@Override
    public long getLastModified() {
	    return lastModified;
    }

	@Override
    public long getContentLength() {
	    return contentLength;
    }

	@Override
    public boolean isExpired() {
	    return expired;
    }

	@Override
    public void expire() throws IllegalStateException {
		if(expired){
			throw new IllegalStateException("This resource already expired");
		}
		this.expired = true;
    }
	
    protected void generateFingerprint(String path) throws IOException {
        try (Reader r = getReader()) {
            byte[] content = IO.readByteArray(r);

            this.fingerprint = manager.getFingerprint(content);

            log.debug("Got a fingerprint '{}' of {} resource '{}'", fingerprint, debug ? "debug" : "production", path);
        } 
    }
	
    protected void resolveClientPathAndUrl() {
        this.path      = manager.getPathWithFingerprint(asset.getPath(), this.fingerprint);
        this.clientUrl = manager.getServletContext().getContextPath() + manager.getConfig().getPathPrefix() + path;
    }

    protected void publishResource() {
        if(!manager.getConfig().isPublishEnabled()) {
            return;
        }
        
        String dir = manager.getConfig().getPublishDirectory();
        
        if (Strings.isEmpty(dir)) {
            throw new AssetException("The publish directory must not be empty in asset configuration");
        }
        File file = new File(Paths.applyRelative(dir, this.clientUrl));
        if (!file.exists()) {
            log.debug("Publish asset '{}' to '{}'", serverPath, file.getAbsolutePath());
            try {
                Files.mkdirs(file);
                try (InputStream in = getInputStream()) {
                    try (OutputStream out = new FileOutputStream(file)) {
                        IO.copy(in, out);
                    }
                }
            } catch (Exception e) {
                throw new AssetException("Error publishing asset '" + serverPath + "' to '" + file.getAbsolutePath() + "'", e);
            }
        }
    }

	@Override
    public String toString() {
		return "AssetResource[path=" + path + ",resource=" + serverPath + "]";
    }
}