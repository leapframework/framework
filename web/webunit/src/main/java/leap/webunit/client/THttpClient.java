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

import java.nio.charset.Charset;
import java.util.Set;

import leap.lang.http.Cookie;
import leap.lang.http.HTTP.Method;

public interface THttpClient {

    /**
     * Returns the base url of this client. 
     * 
     * <p>
     * Default is <code>http(s)://127.0.0.1:port</code>
     */
	String getBaseUrl();
	
	/**
	 * Returns the default charset.
	 * 
	 * <p>
	 * Default is utf-8.
	 */
	Charset getDefaultCharset();
	
	/**
	 * Sets the default charset.
	 */
	void setDefaultCharset(Charset charset);
	
	/**
	 * Returns the cookie or <code>null</code> if not exists.
	 */
	Cookie getCookie(String name);

    /**
     * Adds a cookie
     */
    THttpClient addCookie(String name, String value);
	
	/**
	 * Removes the cookie and returns the removed cookie if exists. 
	 */
	Cookie removeCookie(String name);
	
	/**
	 * Adds a host name mapping to 127.0.0.1 (dns).
	 */
	THttpClient addHostName(String hostName);

	/**
	 * Returns the registered context paths.
     */
	Set<String> getContextPaths();

	/**
	 * Adds the context paths.
     */
	THttpClient addContextPaths(String... contextPaths);

	/**
     * Creates a new http request.
     */
    THttpRequest request(String uri);
	
	/**
	 * Creates a new request according to the given http method.
	 */
	THttpRequest request(Method method,String uri);
	
	/**
	 * Sends a GET request to the given uri.
	 */
	default THttpResponse get(String uri) {
	    return request(uri).get();
	}

	/**
	 * Sends a POST request to the given uri with empty body.
	 */
	default THttpResponse post(String uri) {
	    return request(uri).post();
	}
}