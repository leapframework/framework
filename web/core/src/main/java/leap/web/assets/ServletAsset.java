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

import leap.lang.http.ContentTypes;
import leap.lang.http.MimeTypes;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.lang.servlet.ServletResource;
import leap.lang.servlet.Servlets;

public class ServletAsset extends AbstractAsset implements Asset {
	
	private static final Log log = LogFactory.get(ServletAsset.class);

	private final AssetManager manager;
	private final boolean      reloadable;
	private final boolean      text;
	
	private String mimeType;
	
	public ServletAsset(AssetManager manager, String assetPath, ServletResource sr) {
		super(assetPath);
		this.manager    = manager;
		this.reloadable = sr.isFile();
		
		this.mimeType    = Servlets.getMimeType(sr.getServletContext(), sr.getFilename());
		this.text		 = MimeTypes.isText(mimeType);
		this.contentType = text ? ContentTypes.create(mimeType, manager.getConfig().getCharset().name()) : mimeType;
		this.createAssetResources(assetPath, sr);
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
			AssetResource reloaded = reload((ServletAssetResource)resource);
			if(null != reloaded){
				this.resource.expire();
				this.resource = reloaded;
				reload = true;
			}
			
			reloaded = reload((ServletAssetResource)debugResource);
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
	
	protected AssetResource reload(ServletAssetResource current){
		try {
            ServletResource sr = ((ServletAssetResource)current).getServletResource();
            if(!sr.exists()) {
            	//TODO : expire it
            	log.warn("Resource '{}' not exist, cannot reload it", current.getServerPath());
            	return null;
            }
            
            if(sr.lastModified() != current.getLastModified()) {
            	return new ServletAssetResource(manager, this, sr, current.isDebug());
            }
        } catch (Exception e) {
        	log.error("Error reloading asset resource '{}'",current.getServerPath(),e);
        }
		return null;
	}

	protected void createAssetResources(String assetPath, ServletResource sr) {
		//Find minified resource, filename.js -> filename.min.js
	
		String filepath = sr.getPathWithinContext();
		
		String minifiedFilepath = getMinifiedFilepath(filepath);
		
		ServletResource minifiedResoruce = Servlets.getResource(sr.getServletContext(), minifiedFilepath);
		try {
	        if(null != minifiedResoruce && minifiedResoruce.exists()){
				this.path = getMinifiedFilepath(assetPath);
	        	this.resource      = new ServletAssetResource(manager, this, minifiedResoruce, false);
	        	this.debugResource = new ServletAssetResource(manager, this, sr, true);
	        }else{
	        	this.resource      = new ServletAssetResource(manager, this, sr, false);
	        	this.debugResource = new ServletAssetResource(manager, this, sr, true, resource.getFingerprint());
	        }
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