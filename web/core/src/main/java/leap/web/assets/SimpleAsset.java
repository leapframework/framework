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

import leap.lang.http.ContentTypes;
import leap.lang.http.MimeTypes;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.lang.resource.Resource;
import leap.lang.servlet.ServletResource;
import leap.lang.servlet.Servlets;
import leap.web.Response;

import java.io.IOException;

public class SimpleAsset extends AbstractAsset implements Asset {

	private static final Log log = LogFactory.get(SimpleAsset.class);

	private final AssetManager manager;
	private final boolean      reloadable;
	private final boolean      text;

	private String mimeType;

	public SimpleAsset(AssetManager manager, String assetPath, Resource resource) {
		super(assetPath);
		this.manager    = manager;
		this.reloadable = resource.isFile();
		
		this.mimeType    = Servlets.getMimeType(manager.getServletContext(), resource.getFilename());
		this.text		 = MimeTypes.isText(mimeType);
		this.contentType = text ? ContentTypes.create(mimeType, manager.getConfig().getCharset().name()) : mimeType;
		this.createAssetResources(assetPath, resource);
	}
	
	@Override
    public boolean isText() {
	    return text;
    }

	@Override
    public boolean reloadable() {
	    return reloadable;
    }

	@Override
    public boolean reload() {
		if(reloadable){
			boolean reload = false;
			AssetResource reloaded = reload(resource);
			if(null != reloaded){
				this.resource.expire();
				this.resource = reloaded;
				reload = true;
			}
			
			reloaded = reload(debugResource);
			if(null != reloaded){
				this.debugResource.expire();
				this.debugResource = reloaded;
				reload = true;
			}
			
			if(reload) {
			    this.loadedAt = System.currentTimeMillis();
			}
			
			return reload;
		}
		return false;
    }
	
	protected AssetResource reload(AssetResource current){
		try {
            Resource sr = ((SimpleAssetResource)current).getResource();
            if(!sr.exists()) {
            	log.warn("Resource '{}' not exist, cannot reload it", sr.getFilepath());
            	current.expire();
            	return null;
            }
            
            if(sr.lastModified() != sr.getFile().lastModified()) {
            	return new SimpleAssetResource(manager, this, sr, current.isDebug());
            }
        } catch (Exception e) {
        	log.error("Error reloading asset resource '{}'",current.getPath(),e);
        }
		return null;
	}

	protected void createAssetResources(String assetPath, Resource r) {
		//TODO : Find minified resource, filename.js -> filename.min.js
	
		String filepath = r.getFilepath();
		try {
			this.resource      = new SimpleAssetResource(manager, this, r, false);
			this.debugResource = new SimpleAssetResource(manager, this, r, true, resource.getFingerprint());
        } catch (IOException e) {
        	throw new AssetException("Error creating asset resources, " + e.getMessage(), e);
        }
	}
	
	protected String getMinifiedFilepath(String filepath){
		String filedir  = Paths.getDirPath(filepath);
		String filename = Paths.getFileName(filepath);
		
		int lastDotIndex = filename.lastIndexOf('.');
		
		if(lastDotIndex > 0){
			return filedir + filename.substring(0,lastDotIndex) + ".min" + filename.substring(lastDotIndex);
		}else{
			return filedir + filename + ".min";
		}
	}
}