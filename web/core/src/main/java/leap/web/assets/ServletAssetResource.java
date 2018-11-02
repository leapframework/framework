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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.concurrent.CountDownLatch;

import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;
import leap.lang.servlet.ServletResource;

public class ServletAssetResource extends AbstractAssetResource {
	
	private static final Log log = LogFactory.get(ServletAssetResource.class);
	
	protected final Resource resource;

    private volatile long lastChecked;
	
	public ServletAssetResource(AssetManager manager, Asset asset, Resource resource, boolean debug) throws IOException {
		this(manager,asset,resource,debug,null);
	}
	
	public ServletAssetResource(AssetManager manager, Asset asset, Resource resource, boolean debug, String fingerprint) throws IOException {
	    super(manager, asset);
	    
		this.debug		   = debug;
		this.resource      = resource;
		this.serverPath    = resource.getPath();
		this.lastModified  = resource.lastModified();
        this.lastChecked   = System.currentTimeMillis();

		if(Strings.isEmpty(fingerprint)) {
			this.generateFingerprint(resource.getPath());
		}else{
			this.fingerprint = fingerprint;
			log.debug("Got a fingerprint '{}' of {} resource '{}'", 
					  fingerprint, debug ? "debug" : "production", resource.getPath());
		}
		
		this.resolveClientPathAndUrl();
		this.publishResource();
	}

	public Resource getResource(){
		return resource;
	}

    @Override
    public void access() {
        if(isExpired()) {
            return;
        }

        //check changed? if changed, expires it.
        long now      = System.currentTimeMillis();
        long duration = now - lastChecked;
        if(duration > 30000) { //todo : hard code 30 seconds
            lastChecked = now;
            try {
                if(resource.lastModified() != lastModified) {
                    //Expires it, so next time we will use the new asset resource.
                    this.expire();
                }
            } catch (IOException e) {
                //todo : throw exception?
                log.error(e);
            }
        }
    }

    @Override
    public long getContentLength() throws IOException {
        return resource.contentLength();
    }

    @Override
    public InputStream getInputStream() throws IOException {
	    return resource.getInputStream();
    }

	@Override
    public Reader getReader() {
	    return resource.getInputStreamReader();
    }
}