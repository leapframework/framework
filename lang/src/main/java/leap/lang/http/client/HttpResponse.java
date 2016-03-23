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
package leap.lang.http.client;

import leap.lang.http.HTTP;
import leap.lang.http.MimeType;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A simple http response interface.
 */
public interface HttpResponse {
    
    /**
     * Returns <code>true</code> if the response status is 200.
     */
    default boolean isOk() {
        return getStatus() == HTTP.SC_OK;
    }
    
    /**
     * Returns <code>true</code> if the response status is 404.
     */
    default boolean isNotFound() {
        return getStatus() == HTTP.SC_NOT_FOUND;
    }
    
    /**
     * Returns <code>true</code> if the response status is 400.
     */
    default boolean isBadRequest() {
        return getStatus() == HTTP.SC_BAD_REQUEST;
    }

    /**
     * Returns the response status.
     * 
     * @see HttpURLConnection#getResponseCode()
     */
    int getStatus();
    
    /**
     * Returns the response message or <code>null</code>.
     * 
     * @see HttpURLConnection#getResponseMessage()
     */
    String getReason();
    
    /**
     * Returns the first header value of the name.
     * 
     * <p>
     * Returns <code>null</code> if the header not exists.
     */
    String getHeader(String name);
    
    /**
     * Returns all the header values of the name.
     * 
     * <p>
     * Returns <code>null</code> if the header not exists.
     */
    String[] getHeaderValues(String name);
    
    /**
     * Returns an unmodifiable Map of the header fields.
     * 
     * @see HttpURLConnection#getHeaderFields()
     */
    Map<String, List<String>> getHeaders();
    
    /**
     * Returns the content-type header as {@link MimeType}.
     * 
     * <p>
     * Returns <code>null</code> if the header not exists or the value is empty.
     */
    MimeType getContentType();
    
    /**
     * Returns the response body as byte array.
     */
    byte[] getBytes();

    /**
     * Returns the response body as string.
     */
    String getString();

    /**
     * Executes the function for each header.
     *
     * <p/>
     * The input arguments are (name,value)
     */
    void forEachHeaders(BiConsumer<String,String> func);
}