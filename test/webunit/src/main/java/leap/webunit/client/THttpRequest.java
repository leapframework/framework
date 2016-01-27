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

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;

import leap.lang.Strings;
import leap.lang.http.ContentTypes;
import leap.lang.http.HTTP.Method;
import leap.lang.http.Headers;
import leap.lang.http.MimeType;
import leap.lang.json.JSON;

@SuppressWarnings("rawtypes") 
public interface THttpRequest {
    
    /**
     * Returns the request charset.
     */
    Charset getCharset();
    
    /**
     * Sets the request charset.
     */
    THttpRequest setCharset(Charset charset);
    
    /**
     * Returns the request method.
     */
    Method getMethod();
    
    /**
     * Sets the request method.
     */
    THttpRequest setMethod(Method method);
	
	/**
	 * Set the header.
	 */
	THttpRequest setHeader(String name, String value);
	
	/**
	 * Adds a header.
	 */
	THttpRequest addHeader(String name, String value);

	/**
	 * Sets the content-type header.
	 */
	default THttpRequest setContentType(String contentType) {
	    return setHeader(Headers.CONTENT_TYPE, contentType);
	}
	
	/**
	 * Sets the content-type header.
	 */
	default THttpRequest setContentType(MimeType contentType) {
	    return setHeader(Headers.CONTENT_TYPE, contentType.toString());
	}
	
    /**
     * Set the header {@link Headers#X_REQUESTED_WITH} with value 'XMLHttpRequest'.
     */
    default THttpRequest ajax() {
        return setHeader(Headers.X_REQUESTED_WITH, "XMLHttpRequest");
    }
    
    /**
     * Adda a query string param.
     */
    THttpRequest addQueryParam(String name, String value);
    
    /**
     * Adds all the query params.
     */
    default THttpRequest addQueryParams(Map params) {
        if(null != params) {
            for(Object item : params.entrySet()) {
                Entry entry = (Entry)item;
                String name  = entry.getKey().toString();
                Object value = entry.getValue();
                addQueryParam(name, null == value ? "" : value.toString());
            }
        }
        return this;
    }
    
    /**
     * Adds a form param (name=value).
     * 
     * <p>
     * The http method must be <code>POST</code>.
     */
    THttpRequest addFormParam(String name, String value);
    
    /**
     * Adds all the form params.
     */
    default THttpRequest addFormParams(Map params) {
        if(null != params) {
            for(Object item : params.entrySet()) {
                Entry entry = (Entry)item;
                String name  = entry.getKey().toString();
                Object value = entry.getValue();
                addFormParam(name, null == value ? "" : value.toString());
            }
        }
        return this;
    }

    /**
     * Sets the json content type and body.
     */
    default THttpRequest json(Object value) {
        setContentType(ContentTypes.APPLICATION_JSON_UTF8);
        return setBody(JSON.stringify(value));
    }
    
    /**
     * Returns the multipart reqeust.
     */
    THttpMultipart multipart(); 
    
    /**
     * Sets the request body
     */
    default THttpRequest setBody(String content) {
        return setBody(Strings.getBytes(content, getCharset().name()));
    }

    /**
     * Sets the request body
     */
    THttpRequest setBody(byte[] content);
	
	/**
	 * Sends this request as ajax and returns the response.
	 */
	default THttpResponse sendAjax() {
		return ajax().send();
	}
	
	/**
	 * Sends a GET request.
	 */
	default THttpResponse get() {
	    return setMethod(Method.GET).send();
	}
	
	/**
     * Sends a POST request.
     */
    default THttpResponse post() {
        return setMethod(Method.POST).send();
    }
    
    /**
     * Send this request and returns the response.
     */
    THttpResponse send();
}
