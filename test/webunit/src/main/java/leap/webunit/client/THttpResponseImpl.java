/*
 * Copyright 2013 the original author or authors.
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
package leap.webunit.client;

import leap.junit.TestBase;
import leap.lang.Arrays2;
import leap.lang.Charsets;
import leap.lang.Exceptions;
import leap.lang.Strings;
import leap.lang.exception.NestedIOException;
import leap.lang.http.HTTP;
import leap.lang.http.Headers;
import leap.lang.http.MimeType;
import leap.lang.http.MimeTypes;
import leap.lang.io.IO;
import leap.lang.jsoup.Jsoup;
import leap.lang.jsoup.nodes.Document;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

class THttpResponseImpl implements THttpResponse {
	
	protected static final leap.lang.http.Header[] EMPTY_HEADER_ARRAY = new leap.lang.http.Header[]{};

	private final THttpRequest    request;
	private final HttpRequestBase httpRequest;
	private final HttpResponse    httpResponse;
	
	private MimeType contentType;
	private String	 content;
	private Document htmlDocument;
	
	protected THttpResponseImpl(THttpRequest request, HttpRequestBase httpRequest, HttpResponse httpResponse){
		this.request      = request;
		this.httpRequest  = httpRequest;
		this.httpResponse = httpResponse;
	}
	
	@Override
    public THttpRequest request() {
	    return request;
    }

	public HttpRequestBase getHttpRequest() {
		return httpRequest;
	}
	
	@Override
    public Integer getStatus(){
		StatusLine statusLine = httpResponse.getStatusLine();
		return null == statusLine ? null : statusLine.getStatusCode();
	}
	
	@Override
    public boolean isOk(){
		return getStatus() == HTTP.SC_OK;
	}
	
	@Override
    public boolean isSuccess(){
		return 200 <= getStatus() && getStatus() < 300;
	}

	@Override
	public boolean isFailure() {
		return getStatus() >= 300;
	}

	@Override
    public String getLocation(){
		return getHeader(Headers.LOCATION);
	}
	
	@Override
    public MimeType getContentType(){
		if(null == contentType){
			HttpEntity entity = httpResponse.getEntity();
			Header     header = null == entity ? null : entity.getContentType();
			
			if(null != header){
				contentType = MimeTypes.parse(header.getValue());
			}
		}

		return contentType;
	}
	
	@Override
    public long getContentLength(){
		HttpEntity entity = httpResponse.getEntity();
		return null == entity ? -1L : entity.getContentLength();
	}
	
	@Override
    public String getMediaType(){
		MimeType contentType = getContentType();
		return null == contentType ? null : contentType.getMediaType();
	}
	
	@Override
    public String getCharset(){
		MimeType contentType = getContentType();
		return null == contentType ? null : contentType.getCharset();
	}
	
	@Override
    public String getHeader(String name){
		Header[] headers = httpResponse.getHeaders(name);
		return Arrays2.isEmpty(headers) ? null : headers[0].getValue();
	}
	
	@Override
    public leap.lang.http.Header[] getHeaders(String name){
		Header[] headers = httpResponse.getHeaders(name);
		
		if(null == headers || headers.length == 0) {
			return EMPTY_HEADER_ARRAY;
		}
		
		leap.lang.http.Header[] array = new leap.lang.http.Header[headers.length];
		for(int i=0;i<array.length;i++) {
			Header header = headers[i];
			
			array[i] =  new leap.lang.http.Header(header.getName(), header.getValue());
		}
		
		return array;
	}
	
	@Override
    public String getContent() throws NestedIOException {
		if(null == content){
			try {
		        HttpEntity entity = httpResponse.getEntity();
		        content = null == entity ? null : IO.readString(entity.getContent(), charset());
	        } catch (IOException e) {
	        	throw Exceptions.wrap("Error reading response body", e);
	        }
		}
		return content;
	}
	
	@Override
    public InputStream getInputStream() throws NestedIOException {
		try {
	        HttpEntity entity = httpResponse.getEntity();
	        return null == entity ? null : entity.getContent();
        } catch (IOException e) {
        	throw Exceptions.wrap("Error reading response body", e);
        }
	}
	
	@Override
    public Document getDocument() throws NestedIOException {
		if(null == htmlDocument){
			String html = getContent();
			htmlDocument = Jsoup.parse(html);
		}
	    return htmlDocument;
    }

	@Override
    public THttpResponse assertStatusEquals(int status){
		TestBase.assertEquals((Integer)status, getStatus());
		return this;
	}
	
	@Override
    public THttpResponse assertContentEquals(String expectedContent){
		TestBase.assertEquals(expectedContent, getContent());
		return this;
	}
	
	@Override
    public THttpResponse assertContentEmpty() {
		TestBase.assertEmpty(getContent());
	    return this;
    }

	@Override
	public THttpResponse assertContentNotEmpty() {
        TestBase.assertNotEmpty(getContent());
		return this;
	}

	@Override
    public THttpResponse assertContentContains(String containsContent){
		TestBase.assertContains(getContent(),containsContent);
		return this;
	}
	
	@Override
    public THttpResponse assertSuccess(){
		TestBase.assertTrue(isSuccess());
		return this;
	}

	@Override
	public THttpResponse assertFailure() {
		TestBase.assertFalse(isSuccess());
		return this;
	}

	@Override
    public THttpResponse assertOk(){
		TestBase.assertTrue("The response status should be 200, but is " + getStatus(),isOk());
		return this;
	}
	
	@Override
    public THttpResponse assertNotOk() {
		TestBase.assertFalse("The response status should not be 200", isOk());
	    return this;
    }

	@Override
    public THttpResponse assertContentTypeHtml(){
		TestBase.assertEquals(MimeTypes.TEXT_HTML,getMediaType());
		return this;
	}
	
	@Override
    public THttpResponse assertContentTypeText(){
		TestBase.assertEquals(MimeTypes.TEXT_PLAIN,getMediaType());
		return this;
	}
	
	@Override
    public THttpResponse assertContentTypeEquals(String mediaType){
		TestBase.assertEquals(mediaType,getMediaType());
		return this;
	}
	
	@Override
    public THttpResponse assertContentTypeEquals(String mediaType,String charset){
		TestBase.assertEquals(mediaType,getMediaType());
		TestBase.assertEquals(Strings.upperCase(charset),Strings.upperCase(getCharset()));
		return this;
	}
	
	@Override
    public THttpResponse assertContentTypePresent(){
		TestBase.assertNotNull(getContentType());
		return this;
	}
	
	@Override
    public THttpResponse assertHeaderEquals(String headerName,String expectedValue){
		TestBase.assertEquals(expectedValue, getHeader(headerName));
		return this;
	}
	
	@Override
    public THttpResponse assertCharsetEquals(String expectedCharset){
		TestBase.assertEquals(expectedCharset,getCharset());
		return this;
	}
	
	private Charset charset(){
		MimeType contentType = getContentType();
		String   charset     = null == contentType ? null : contentType.getCharset();
		
		return null == charset ? Charsets.UTF_8 : Charsets.forName(charset);
	}
}
