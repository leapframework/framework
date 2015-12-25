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
package leap.web.download;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;

import leap.lang.Strings;
import leap.lang.exception.NestedIOException;
import leap.lang.http.HTTP;
import leap.lang.http.Headers;
import leap.lang.http.MimeTypes;
import leap.lang.io.IO;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;
import leap.web.Request;
import leap.web.Response;

public abstract class AbstractDownload implements Download {
	private static final Log log = LogFactory.get(AbstractDownload.class);
	
	protected String   filename;
	protected String   contentType;
	protected long 	   contentLength = -1;
	protected String   etag;
	protected boolean  gzip;
	protected boolean  gzipped;
	
	@Override
    public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public String getEtag() {
		return etag;
	}

	public void setEtag(String etag) {
		this.etag = etag;
	}

	/**
	 * Returns <code>true</code> if the content will be gzipped on response.
	 * 
	 * <p>
	 * Default is <code>false</code>.
	 */
    public boolean isGzip() {
		return gzip;
	}

	public void setGzip(boolean gzip) {
		this.gzip = gzip;
	}

	/**
	 * Returns <code>true</code> if the content aleady gzipped.
	 */
	public boolean isGzipped() {
		return gzipped;
	}

	public void setGzipped(boolean gzipped) {
		this.gzipped = gzipped;
	}
	
    public long getContentLength() {
		return contentLength;
	}

	public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

    @Override
	public String getContentType(Request request) throws Throwable {
		if(null == contentType) {
			contentType = MimeTypes.getMimeType(filename);
		}
		return contentType;
	}
	
	@Override
	public void render(Request request, Response response) throws Throwable {
		Resource resource = getResource(request, response);
		
		//Not Found
		if(null == resource || !resource.exists()) {
			if(null != resource) {
				log.warn("The downloading resource '{}' not found", resource);	
			}else {
				log.warn("The downloading file '{}' not found", getFilename());
			}
				
			handleNotFound(request, response);
			return;
		}
		
		//Not Modified
		if(checkNotModified(request, response, resource)) {
			handleNotModified(request, response, resource);
			return;
		}
		
		//Render the resource
		render(request, response, resource);
	}
	
	protected void render(Request request, Response response, Resource resource) throws Throwable  {
		//Check filename
		if(Strings.isEmpty(filename)) {
			filename = resource.getFilename();
		}
		
		//Check Length
		long length = this.contentLength == -1 ? resource.contentLength() : contentLength;
		if(length > Integer.MAX_VALUE) {
			throw new IOException("Resource content too long(beyond Integer.MAX_VALUE): " + filename);
		}

		//Set headers
		setHeaders(request, response, resource);
		
		//Write content
		if(gzip) {
			writeGzipContent(request, response, resource, length);
		}else{
			writeRawContent(request, response, resource, length);			
		}
	}
	
	protected void handleNotFound(Request request,Response response) throws ServletException,IOException {
		log.trace("No matching resource found, returning 404");
		response.sendError(HTTP.SC_NOT_FOUND);
	}
	
	protected void handleNotModified(Request  request,
									 Response response,
									 Resource resource) throws Throwable {
		
		log.trace("Resource not modified, returning 304");
		response.setStatus(HTTP.SC_NOT_MODIFIED);
		
		setCacheHeaders(request, response, resource);
	}
	
	//Returns <code>true</code> if not modified
	protected boolean checkNotModified(Request  request,
									   Response response, 
									   Resource resource) throws ServletException,IOException {

		//check etag
		if(null != etag) {
			String ifNoneMatch = request.getHeader(Headers.IF_NONE_MATCH);
			if(!Strings.isEmpty(ifNoneMatch)){
				return ifNoneMatch.equals("\"" + etag + "\"");
			}
		}

		//check lastModified
		long ifModifiedSince = request.getDateHeader(Headers.IF_MODIFIED_SINCE);
		if (ifModifiedSince > 0 && resource.lastModified() == ifModifiedSince) {
			return true;
		}

		return false;
	}
	
	protected void setHeaders(Request  request,
							  Response response,
							  Resource resource) throws Throwable {
		
		response.setContentType(getContentType(request));
		
		response.addHeader(Headers.CONTENT_DISPOSITION, 
						   "attachment; filename=\"" + URLEncoder.encode(filename, request.getCharacterEncoding()) + "\";");
		
		setCacheHeaders(request, response, resource);
	}
	
	protected void setCacheHeaders(Request request, Response response, Resource resource) throws Throwable {
		if(null != etag && etag.length() > 0) {
			response.setHeader(Headers.ETAG, "\"" + etag + "\"");
		}

		try {
            long lastModified = resource.lastModified();
            if(lastModified > 0){
            	response.setDateHeader(Headers.LAST_MODIFIED, lastModified);
            }
        } catch (Exception e) {
            //TODO : handle exception.
        }
	}
	
	protected void writeGzipContent(Request request, Response response, Resource resource,long contentLength) throws Throwable {
		try(InputStream is = resource.getInputStream()) {
			try {
				
				//Compress
	            byte[] compressed = gzip(is,contentLength);
	            
	            if(log.isDebugEnabled()) {
	            	log.debug("Gzip resource '{}' : {} -> {}",resource.getURLString(), contentLength, compressed.length);
	            }
				
	            //Set headers
	            response.setHeader(Headers.CONTENT_ENCODING, "gzip");
	            response.setContentLength(compressed.length);
	            
	            //Response
	            try(ByteArrayInputStream gzipInputStream = new ByteArrayInputStream(compressed)) {
	            	IO.copy(gzipInputStream, response.getOutputStream());
	            }
				
            } catch (NestedIOException e) {
            	log.error("Error writting resource content,{}", e.getMessage(), e);
            	throw e;
            }
		}
	}
	
	protected void writeRawContent(Request request, Response response, Resource resource, long contentLength) throws Throwable {
		if(gzipped) {
            response.setHeader(Headers.CONTENT_ENCODING, "gzip");
		}
		
		try(InputStream is = getResource(request, response).getInputStream()) {
			try {
				//Set content-length
				response.setContentLength((int) contentLength);
				
				//Response
	            IO.copy(is, response.getOutputStream());
            } catch (NestedIOException e) {
            	log.error("Error writting resource content,{}", e.getMessage(), e);
            	throw e;
            }
		}		
	}
	
	protected abstract Resource getResource(Request request, Response response) throws Throwable;
	
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
