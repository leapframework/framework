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
package leap.webunit.client;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.http.HTTP.Method;
import leap.lang.http.QueryStringBuilder;
import leap.lang.http.exception.HttpException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.net.Urls;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.io.SessionOutputBuffer;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.HeaderGroup;

class THttpRequestImpl implements THttpRequest {
	
	private static final Log log = LogFactory.get(THttpRequestImpl.class);
	
    private final THttpClient         tclient;
    private final HttpClient          client;
    private final String              uri;
    private final HeaderGroup         headers     = new HeaderGroup();
    private final QueryStringBuilder  queryString = new QueryStringBuilder();
    private final List<NameValuePair> formParams  = new ArrayList<NameValuePair>();

    private Method                    method;
    private Charset                   charset;
    private byte[]                    body;
    private HttpRequestBase           request;
    private THttpMultipartImpl        multipart;
    private HttpEntity                entity;
	
	public THttpRequestImpl(THttpClientImpl client, String uri) {
	    this.tclient = client;
		this.client  = client.getHttpClient();
		this.uri     = uri;
		
		this.method  = null;
		this.charset = tclient.getDefaultCharset();
	}
	
	@Override
    public THttpMultipart multipart() {
	    if(null == multipart) {
	        multipart = new THttpMultipartImpl(this);
	    }
        return multipart;
    }

    @Override
    public Charset getCharset() {
        return charset;
    }

    @Override
    public THttpRequest setCharset(Charset charset) {
        Args.notNull(charset, "charset");
        this.charset = charset;
        return this;
    }

    @Override
    public Method getMethod() {
        if(null == method) {
            return Method.GET;
        }
        return method;
    }

    @Override
    public THttpRequest setMethod(Method method) {
        Args.notNull(method,"method");
        this.method = method;
        return this;
    }

    @Override
	public THttpRequest setHeader(String name, String value) {
        Args.notNull(name, "Header name");
		headers.updateHeader(new BasicHeader(name, value)); 
		return this;
	}
	
	@Override
    public THttpRequest addHeader(String name, String value) {
	    Args.notNull(name, "Header name");
		headers.addHeader(new BasicHeader(name, value));
	    return this;
    }
	
	@Override
    public THttpRequest addQueryParam(String name, String value) {
	    Args.notEmpty(name, "name");
	    queryString.add(name, value);
        return this;
    }

    @Override
    public THttpRequest addFormParam(String name, String value) {
	    formParams.add(new BasicNameValuePair(name, value));
	    return this;
    }

    @Override
    public THttpRequest setBody(byte[] content) {
        this.body = content;
        return this;
    }
	
	@Override
    public THttpResponse send(){
	    String url = buildRequestUrl();
		try {
		    newRequest(url);
		    
			log.debug("Sending '{}' request to '{}'...", method, url);
			
			THttpResponseImpl response = new THttpResponseImpl(this, request, client.execute(request) );
			
			log.debug("Response result : [status={}, type='{}', length={}]",
					  response.getStatus(),
					  response.getContentType(),
					  response.getContentLength());
			
	        return response;
        } catch (Exception e) {
        	throw new HttpException("Error send http request : " + e.getMessage(),e);
        }finally{
        	request.releaseConnection();
        }
	}
	
    protected String buildRequestUrl(){
        String url = null;
        
        if(Strings.isEmpty(uri)){
            url = tclient.getBaseUrl();
        }else if(uri.indexOf("://") > 0) {
            url = uri;
        }else if(Strings.startsWith(uri, "/")){
            url = tclient.getBaseUrl() + uri;
        }else{
            url = tclient.getBaseUrl() + "/" + uri;
        }
        
        if(!queryString.isEmpty()) {
            url = Urls.appendQueryString(url, queryString.build());
        }

        URI uri = URI.create(url);
        String path = uri.getPath();
        if(!"".equals(path)) {
            for(String contextPath : tclient.getContextPaths()) {
                if(path.equals(contextPath)) {
                    url = uri.getScheme() + ":" + uri.getSchemeSpecificPart() + "/";
                    if(null != uri.getQuery()) {
                        url = url + "?" + uri.getRawQuery();
                    }
                    break;
                }
            }
        }

        return url;
    }
    
    protected void initRequest() {
        if(!formParams.isEmpty()) {
            entity = new UrlEncodedFormEntity(formParams,charset);
        }else if(null != body && body.length > 0) {
            entity = new ByteArrayEntity(body);
        }else if(multipart != null && !multipart.isEmpty()) {
            entity = multipart.buildEntity();
        }
        
        if(null != entity && method == null) {
            method = Method.POST;
        }
    }
	
    protected HttpRequestBase newRequest(String url) {
        initRequest();
        
        if (method.equals(Method.GET)) {
            request = new HttpGet(url);
        }

        if (method.equals(Method.POST)) {
            request = new HttpPost(url);
        }

        if (method.equals(Method.PUT)) {
            request = new HttpPut(url);
        }

        if (method.equals(Method.DELETE)) {
            request = new HttpDelete(url);
        }

        if (method.equals(Method.PATCH)) {
            request = new HttpPatch(url);
        }

        if (null == request) {
            throw new IllegalStateException("Http method '" + method.name() + "' not supported now");
        }

        //set headers
        Header[] headerArray = headers.getAllHeaders();
        if(null != headerArray && headerArray.length > 0) {
            request.setHeaders(headerArray);
        }
        
        if(null != entity) {
            entityEnclosingRequest().setEntity(entity);
        }
        
        return request;
    }
    
    protected HttpEntityEnclosingRequest entityEnclosingRequest()  {
        if(!(request instanceof HttpEntityEnclosingRequest)){
            throw new IllegalStateException("Http method '" + method + "' does not supports request body");
        }
        return (HttpEntityEnclosingRequest)request;
    }
}
