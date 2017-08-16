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

import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.http.ContentTypes;
import leap.lang.http.HTTP.Method;
import leap.lang.http.MimeType;
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
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.HeaderGroup;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

class ApacheTHttpRequest extends THttpRequestBase<ApacheTHttpClient> {
	
	private static final Log log = LogFactory.get(ApacheTHttpRequest.class);
	
    private final HttpClient          httpClient;
    private final HeaderGroup         headers     = new HeaderGroup();
    private final List<NameValuePair> formParams  = new ArrayList<NameValuePair>();

    private HttpRequestBase      request;
    private ApacheTHttpMultipart multipart;
    private HttpEntity           entity;
	
	public ApacheTHttpRequest(ApacheTHttpClient client, String uri) {
        super(client, uri);
		this.httpClient = client.getHttpClient();
	}
	
	@Override
    public THttpMultipart multipart() {
	    if(null == multipart) {
	        multipart = new ApacheTHttpMultipart(this);
	    }
        return multipart;
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
    public THttpRequest addFormParam(String name, String value) {
        formParams.add(new BasicNameValuePair(name, value));
        return this;
    }

	@Override
    public THttpResponse send(){
	    String url = buildRequestUrl();
		try {
		    newRequest(url);
		    
			log.debug("Sending '{}' request to '{}'...", method, url);
			
			ApacheTHttpResponse response = new ApacheTHttpResponse(this, request, httpClient.execute(request) );

            if(log.isDebugEnabled()) {
                log.debug("Response result : [status={}, content-type='{}', content-length={}]",
                        response.getStatus(),
                        response.getContentType(),
                        response.getContentLength());

                MimeType contentType = response.getContentType();
                if(null != contentType && ContentTypes.isText(contentType.toString())) {
                    log.debug("Content -> \n{}", Strings.abbreviate(response.getContent(), 200));
                }
            }
	        return response;
        } catch (Exception e) {
        	throw new HttpException("Error send http request : " + e.getMessage(),e);
        }finally{
            if(null != request) {
                request.releaseConnection();
            }
        }
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

        if (method.equals(Method.HEAD)) {
            request = new HttpHead(url);
        }

        if (method.equals(Method.OPTIONS)) {
            request = new HttpOptions(url);
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
