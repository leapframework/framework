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

import leap.lang.Arrays2;
import leap.lang.http.HTTP;
import leap.lang.http.MimeType;
import leap.lang.http.exception.HttpIOException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * A simple http response interface.
 */
public interface HttpResponse {

    interface ContentReader<T> {
        T read(InputStream is) throws IOException;
    }
    
    /**
     * Returns <code>true</code> if the response status is 200.
     */
    default boolean isOk() {
        return getStatus() == HTTP.SC_OK;
    }

    /**
     * Returns true if the response status is 2xx;
     */
    default boolean is2xx() {
        return getStatus() >= 200 && getStatus() < 300;
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
     * Executes the function for each header.
     *
     * <p/>
     * The input arguments are (name,value)
     */
    default void forEachHeaders(BiConsumer<String,String> func) {
        getHeaders().forEach(func);
    }

    /**
     * Returns the response status.
     * 
     * @see HttpURLConnection#getResponseCode()
     */
    int getStatus();
    
    /**
     * Returns the first header value of the name.
     * 
     * <p>
     * Returns <code>null</code> if the header not exists.
     */
    default String getHeader(String name) {
        List<String> values = getHeaders().get(name);
        if(null == values || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }
    
    /**
     * Returns all the header values of the name.
     * 
     * <p>
     * Returns <code>null</code> if the header not exists.
     */
    default String[] getHeaderValues(String name) {
        List<String> values = getHeaders().get(name);
        if(null == values || values.isEmpty()) {
            return null;
        }
        return values.toArray(Arrays2.EMPTY_STRING_ARRAY);
    }
    
    /**
     * Returns the headers.
     */
    HttpHeaders getHeaders();

    /**
     * Returns the content-type header as {@link MimeType}.
     * 
     * <p>
     * Returns <code>null</code> if the header not exists or the value is empty.
     */
    MimeType getContentType();

    /**
     * Returns the content's charset or null if not specified.
     */
    default String getCharset() {
        MimeType contentType = getContentType();
        return null == contentType ? null : contentType.getCharset();
    }
    
    /**
     * Returns the content of response as byte array.
     */
    byte[] getBytes() throws HttpIOException;

    /**
     * Returns the content of response as string.
     */
    String getString() throws HttpIOException;

    /**
     * Returns the content of response as {@link InputStream}.
     */
    InputStream getInputStream() throws HttpIOException;

    /**
     * Reads the input stream by the given reader.
     */
    default <T> T readInputStream(ContentReader<T> reader) throws HttpIOException {
        try {
            try(InputStream is = getInputStream()) {
                return null == is ? null : reader.read(is);
            }
        } catch (IOException e) {
            throw new HttpIOException(e);
        }
    }
}