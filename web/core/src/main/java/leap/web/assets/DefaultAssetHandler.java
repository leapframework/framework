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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;

import leap.core.AppException;
import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.ioc.LoadableBean;
import leap.core.web.RequestBase;
import leap.core.web.ResponseBase;
import leap.lang.Strings;
import leap.lang.exception.NestedIOException;
import leap.lang.http.HTTP;
import leap.lang.http.Headers;
import leap.lang.io.IO;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.AppHandler;
import leap.web.Request;
import leap.web.Response;

public class DefaultAssetHandler implements AssetHandler,LoadableBean {
	
	private static final Log log = LogFactory.get(DefaultAssetHandler.class);
	
    protected @Inject @M AssetConfig   config;
    protected @Inject @M AssetSource   source;
    protected @Inject @M AppHandler    appHandler;
    protected @Inject @M AssetStrategy strategy;
	
	@Override
    public boolean matches(RequestBase request) {
	    return request.getPath().startsWith(config.getPathPrefix());
    }

	@Override
	public boolean handle(Request request, Response response) throws Throwable {
		try {
			appHandler.prepareRequest(request, response);
			
	        AssetSource source = request.getAssetSource();
	        if(null == source){
	        	source = this.source;
	        }
	        
	        //Extract resource path from request
	        String path = request.getPath().substring(config.getPathPrefix().length());
	        
	        String[] pathAndFingerprint = strategy.splitPathAndFingerprint(path);
	        if(null == pathAndFingerprint){
	        	pathAndFingerprint = new String[]{path,null};
	        }
	        
	        //Find AssetResoruce by the resource path.
	        AssetResource resource = source.getAssetResource(path, pathAndFingerprint ,request.getLocale());
	        if(null == resource){
	        	pathAndFingerprint[0] = path;
	        	pathAndFingerprint[1] = null;
	        	resource = source.getAssetResource(path, pathAndFingerprint, request.getLocale());
	        }
	        
	        //Not Found
	        if(null == resource){
	        	handleNotFound(request, response);
	        	return true;
	        }
	        
	        //Not Modified
	        if(checkNotModified(request, response, resource)){
	        	handleNotModified(request, response, resource, pathAndFingerprint);
	        	return true;
	        }
	        
	        handleResource(request, response, resource, pathAndFingerprint);
	        return true;
		} catch(ServletException|IOException|RuntimeException e){
			throw e;
        } catch (Throwable e) {
        	throw new AppException(e.getMessage(),e);
        }
	}

	protected void handleNotFound(RequestBase request,ResponseBase response) throws ServletException,IOException {
		log.trace("No matching resource found, returning 404");
		response.sendError(HTTP.SC_NOT_FOUND);
	}
	
	//Returns <code>true</code> if not modified
	protected boolean checkNotModified(RequestBase request,
									   ResponseBase response, 
									   AssetResource resource) throws ServletException,IOException {

		//check etag
		String ifNoneMatch = request.getHeader(Headers.IF_NONE_MATCH);
		if(!Strings.isEmpty(ifNoneMatch)){
			return ifNoneMatch.equals("\"" + resource.getFingerprint() + "\"");
		}

		//check lastModified
		long ifModifiedSince = request.getDateHeader(Headers.IF_MODIFIED_SINCE);
		if (ifModifiedSince > 0 && resource.getLastModified() == ifModifiedSince) {
			return true;
		}

		return false;
	}	
	
	protected void handleNotModified(RequestBase request,
									 ResponseBase response,
									 AssetResource resource,
									 String[] pathAndFingerprint) throws ServletException,IOException {
		log.trace("Resource not modified, returning 304");
		response.setStatus(HTTP.SC_NOT_MODIFIED);
		
		doClientCache(request, response, resource, pathAndFingerprint);
	}
	
	protected void handleResource(RequestBase request, 
								  ResponseBase response, 
								  AssetResource resource,
								  String[] pathAndFingerprint) throws ServletException, IOException {
		
		//Cache
		doClientCache(request, response, resource, pathAndFingerprint);
		
		//Set headers
		setHeaders(request, response, resource, pathAndFingerprint);
		
		//Write content
		writeContent(request, response, resource, pathAndFingerprint);
	}
	
	protected void doClientCache(RequestBase request, 
						   		 ResponseBase response, 
						   		 AssetResource resource,
						   		 String[] pathAndFingerprint) throws ServletException, IOException {

		
		String fingerprint = pathAndFingerprint[1];
		
		if(null != fingerprint && fingerprint.length() > 0){
			response.setHeader(Headers.CACHE_CONTROL, "public, max-age=" + config.getCacheMaxAge());
			response.setHeader(Headers.ETAG, "\"" + resource.getFingerprint() + "\"");
		}else{
			response.setHeader(Headers.CACHE_CONTROL, "public, must-revalidate");
		}
		
		if(resource.getLastModified() > 0){
			response.setDateHeader(Headers.LAST_MODIFIED, resource.getLastModified());
		}
	}
	
	protected void setHeaders(RequestBase request,
							  ResponseBase response,
							  AssetResource resource,
							  String[] pathAndFingerprint) throws ServletException,IOException {
		//Set content-type
		response.setContentType(resource.getAsset().getContentType());
	}
	
	protected void writeContent(RequestBase request, 
			   					ResponseBase response, 
			   					AssetResource resource,
			   					String[] pathAndFingerprint) throws ServletException, IOException {
		
		//Check length
		long length = resource.getContentLength();
		if (length > Integer.MAX_VALUE) {
			throw new IOException("Resource content too long (beyond Integer.MAX_VALUE): " + resource.getServerPath());
		}
		
		//Only zip text files
		if(config.isGzipEnabled() && resource.getAsset().isText() && 
		   resource.getContentLength() >= config.getGzipMinLength() &&
		   request.isGzipSupport() ) {
			writeGzipContent(request, response, resource);
		}else{
			writeRawContent(request, response, resource);
		}
	}
	
	protected void writeRawContent(RequestBase request, 
								   ResponseBase response,
								   AssetResource resource) throws ServletException, IOException {

		try(InputStream is = resource.getInputStream()) {
			try {
				//Set content-length
				response.setContentLength((int) resource.getContentLength());
				
				//Response
	            IO.copy(is, response.getOutputStream());
            } catch (NestedIOException e) {
            	log.error("Error writting asset content,{}", e.getMessage(), e);
            	throw e;
            }
		}
	}
	
	protected void writeGzipContent(RequestBase request, 
									ResponseBase response,
									AssetResource resource) throws ServletException, IOException {

		try(InputStream is = resource.getInputStream()) {
			try {
				//Compress
	            byte[] compressed = gzip(is,resource.getContentLength());
	            
	            if(log.isDebugEnabled()) {
	            	log.debug("Gzip assets '{}' : {} -> {}",resource.getServerPath(), resource.getContentLength(), compressed.length);
	            }
				
	            //Set headers
	            response.setHeader(Headers.CONTENT_ENCODING, "gzip");
	            response.setContentLength(compressed.length);
	            
	            //Response
	            try(ByteArrayInputStream gzipInputStream = new ByteArrayInputStream(compressed)) {
	            	IO.copy(gzipInputStream, response.getOutputStream());
	            }
				
            } catch (NestedIOException e) {
            	log.error("Error writing asset content,{}", e.getMessage(), e);
            	throw e;
            }
		}
	}

	@Override
    public boolean load(BeanFactory factory) throws Exception {
	    return config.isEnabled();
    }
	
	private final static byte[] gzip(InputStream is, long length) throws IOException {

		final ByteArrayOutputStream compressedOutputStream = new ByteArrayOutputStream((int) (length * 0.75));

		final OutputStream gzipOutputStream = new GZIPOutputStream(compressedOutputStream);

		final byte[] buf = new byte[5000];
		int len;

		while ((len = is.read(buf)) > 0) {
			gzipOutputStream.write(buf, 0, len);
		}

		gzipOutputStream.close();

		return compressedOutputStream.toByteArray();
	}	
}
