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

import leap.lang.Strings;
import leap.lang.http.ContentTypes;
import leap.lang.http.Headers;

/**
 * A simple http request interface.
 */
public interface HttpRequest {
    
    /**
     * Sets a http cookie.
     */
    HttpRequest setCookie(String name, String value);
    
    /**
     * Sets a http header.
     */
    HttpRequest setHeader(String name, String value);
    
    /**
     * Sets the content-type header.
     */
    default HttpRequest setContentType(String contentType) {
        return setHeader(Headers.CONTENT_TYPE, contentType);
    }
    
    /**
     * Sets the {@link Headers#X_REQUESTED_WITH} header.
     */
    default HttpRequest ajax() {
        return setHeader(Headers.X_REQUESTED_WITH, "XMLHttpRequest");
    }
    
    /**
     * Sets the json content type and body.
     */
    default HttpRequest json(String json) {
        setContentType(ContentTypes.APPLICATION_JSON_UTF8);
        return setBody(json);
    }
    
    /**
     * Sets the content of request body.
     */
    default HttpRequest setBody(String data) {
        return setBody(Strings.getBytesUtf8(data));
    }
    
    /**
     * Sets the content of request body.
     */
    HttpRequest setBody(byte[] data);
    
    /**
     * Appends a query parameter (name=value) to the url.
     */
    HttpRequest addQueryParam(String name, String value);
    
    /**
     * Adds a form prameter (must use POST to send the reqest).
     */
    HttpRequest addFormParam(String name, String value);
    
    /**
     * Sends a GET reqeust.
     */
    HttpResponse get();
    
    /**
     * Sends a POST request.
     */
    HttpResponse post();
    
}