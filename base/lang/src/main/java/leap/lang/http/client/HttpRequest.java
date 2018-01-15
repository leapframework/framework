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
import leap.lang.convert.Converts;
import leap.lang.http.ContentTypes;
import leap.lang.http.HTTP;
import leap.lang.http.Headers;
import leap.lang.json.JSON;
import leap.lang.json.JsonSettings;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * A simple http request interface.
 */
public interface HttpRequest {

    /**
     * Returns true if the request has been aborted.
     */
    boolean isAborted();

    /**
     * Aborts the request.
     */
    void abort();

    /**
     * Sets the {@link Headers#X_REQUESTED_WITH} header.
     */
    default HttpRequest ajax() {
        return setHeader(Headers.X_REQUESTED_WITH, "XMLHttpRequest");
    }

    /**
     * Encodes the json value Sets the json content
     */
    default HttpRequest json(Object o) {
        return setJson(JSON.encode(o, JsonSettings.MIN));
    }

    /**
     * Adds a header value.
     */
    default HttpRequest header(String name, Object value) {
        return addHeader(name, Converts.toString(value));
    }

    /**
     * Adds a form param.
     */
    default HttpRequest form(String name, Object value) {
        return addFormParam(name, Converts.toString(value));
    }

    /**
     * Adds a query param.
     */
    default HttpRequest query(String name, Object value) {
        return addQueryParam(name, Converts.toString(value));
    }

    /**
     * Same as {@link #setContentType(String)}.
     */
    default HttpRequest contentType(String contentType) {
        return setContentType(contentType);
    }

    /**
     * Sets the http method.
     */
    default HttpRequest method(String method) {
        return setMethod(HTTP.Method.valueOf(method));
    }

    /**
     * Auto set body.
     */
    default HttpRequest body(Object body) {
        if(null == body) {
            return this;
        }
        if(body instanceof byte[]) {
            return setBody((byte[])body);
        }
        if(body instanceof CharSequence) {
            return setBody(body.toString());
        }
        if(body instanceof InputStream) {
            return setBody((InputStream)body);
        }
        throw new IllegalStateException("Unsupported body type '" + body.getClass() + "'");
    }

    /**
     * Sets the json content type and body.
     */
    default HttpRequest setJson(String json) {
        setContentType(ContentTypes.APPLICATION_JSON_UTF8);
        return setBody(json);
    }

    /**
     * Sets the content-type header.
     */
    default HttpRequest setContentType(String contentType) {
        return setHeader(Headers.CONTENT_TYPE, contentType);
    }

    /**
     * Sets the content of request body.
     */
    default HttpRequest setBody(String data) {
        return setBody(Strings.getBytesUtf8(data));
    }

    /**
     * Sets a http cookie.
     */
    HttpRequest setCookie(String name, String value);

    /**
     * Sets a http header.
     */
    HttpRequest setHeader(String name, String value);

    /**
     * Adds a http header.
     */
    HttpRequest addHeader(String name, String value);

    /**
     * Sets the request method.
     */
    HttpRequest setMethod(HTTP.Method method);
    
    /**
     * Sets the content of request body.
     */
    HttpRequest setBody(byte[] data);

    /**
     * Sets the content of request body.
     */
    HttpRequest setBody(InputStream is);

    /**
     * Appends a query parameter (name=value) to the url.
     */
    HttpRequest addQueryParam(String name, String value);
    
    /**
     * Adds a form parameter (must use POST to send the request).
     */
    HttpRequest addFormParam(String name, String value);
    
    /**
     * Sends a GET request.
     */
    default HttpResponse get() {
        return send(HTTP.Method.GET);
    }
    
    /**
     * Sends a POST request.
     */
    default HttpResponse post() {
        return send(HTTP.Method.POST);
    }

    /**
     * Sends a PATCH request.
     */
    default HttpResponse patch() {
        return send(HTTP.Method.PATCH);
    }

    /**
     * Sends a DELETE request.
     */
    default HttpResponse delete() {
        return send(HTTP.Method.DELETE);
    }

    /**
     * Sends a PUT request.
     */
    default HttpResponse put() {
        return send(HTTP.Method.PUT);
    }

    /**
     * Sends the request with http method.
     */
    default HttpResponse send(HTTP.Method method) {
        return setMethod(method).send();
    }

    /**
     * Sends the request with http method.
     */
    default HttpResponse send(String method) {
        return method(method).send();
    }

    /**
     * Sends the request.
     */
    HttpResponse send();

    /**
     * Sends the request and callback the function later.
     */
    default void sendAsync(Consumer<HttpResponse> func) {
        sendAsync((request, response) -> {
            func.accept(response);
        });
    }

    /**
     * Sends the request and callback the function later.
     */
    default void sendAsync(String method, Consumer<HttpResponse> func) {
        sendAsync(method, (request, response) -> {
            func.accept(response);
        });
    }

    /**
     * Sends the request and callback the function later.
     */
    default void sendAsync(HTTP.Method method, Consumer<HttpResponse> func) {
        sendAsync(method, (request, response) -> {
            func.accept(response);
        });
    }

    /**
     * Sends the request and callback the handler later.
     */
    void sendAsync(HttpHandler handler);

    /**
     * Sends the request and callback the handler later.
     */
    default void sendAsync(HTTP.Method method, HttpHandler handler) {
        setMethod(method).sendAsync(handler);
    }

    /**
     * Sends the request and callback the handler later.
     */
    default void sendAsync(String method, HttpHandler handler) {
        method(method).sendAsync(handler);
    }
}