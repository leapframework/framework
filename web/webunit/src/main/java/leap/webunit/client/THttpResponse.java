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

import leap.lang.http.*;
import leap.lang.json.JSON;
import leap.lang.json.JsonValue;
import leap.lang.jsoup.nodes.Document;

import java.io.InputStream;
import java.util.Map;
import java.util.function.Function;

/**
 * The HTTP response in client side for testing only.
 */
public interface THttpResponse {
	
	/**
	 * Returns the corresponding request of this response.
	 */
	THttpRequest request();

    /**
     * Returns the HTTP status code.
     */
	Integer getStatus();

    /**
     * Returns true if the HTTP status is 200.
     */
	boolean isOk();

    /**
     * Returns true if the HTTP status is 2xx.
     */
	boolean isSuccess();

    /**
     * Returns true if the HTTP status is >= 300.
     */
    boolean isFailure();

    /**
     * Returns the header value of {@link Headers#LOCATION}.
     *
     * <p/>
     * If the server send a redirect response, the value of this method is the redirect uri sent by server.
     */
	String getLocation();

    /**
     * Returns the redirect uri or null if no redirection.
     *
     * <p/>
     * It's the same as {@link #getLocation()}.
     */
	default String getRedirectUrl() {
	    return getLocation();
	}

    /**
     * Returns the content length in response.
     */
	long getContentLength();

    /**
     * Returns the content type in response or null if no content type.
     */
	MimeType getContentType();

    /**
     * Returns the <code>type/subtype</code> part in the content-type, such as the media type of 'text/plain;charset=utf8' is 'text/plain';
     */
	String getMediaType();

    /**
     * Returns the charset part in the content-type or null if not exists.
     */
	String getCharset();

    /**
     * Returns the value of the given header name.
     *
     * <p/>
     * Returns null if not exists.
     */
	String getHeader(String name);

    /**
     * Returns an array of {@link Header} contains all the headers of the given header name.
     */
	Header[] getHeaders(String name);

    /**
     * Returns the content of response body as string.
     */
	String getContent();

    /**
     * Returns the content of response body as {@link JsonValue}.
     */
	default JsonValue getJson() {
		return JSON.parse(getContent());
	}

    /**
     * Parse the response content as json and decodes to map.
     */
    default Map<String,Object> decodeJsonMap() {
        return JSON.decodeMap(getContent());
    }

    default <R> R decode(Function<String,R> decoder){
    	return decoder.apply(getContent());
	}
    
    /**
     * Parse the response content as json and decodes to array.
     */
    default <T> T[] decodeJsonArray(Class<T> componentType) {
       return JSON.decodeArray(getContent(), componentType);
    }

    /**
     * Parse the response content as json and decodes to the given type.
     */
    default <T> T decodeJson(Class<T> type) {
        return JSON.decode(getContent(), type);
    }

    /**
     * Returns the content of response body as {@link InputStream}.
     */
	InputStream getInputStream();

    /**
     * Returns the content of response body as html document.
     */
	Document getDocument();

    /**
     * Asserts that the HTTP status is 400 bad request.
     */
	default THttpResponse assert400() {
		return assertStatusEquals(HTTP.SC_BAD_REQUEST);
	}

	/**
	 * Asserts that the HTTP status is 400 bad request.
     */
	default THttpResponse assertBadRequest() {
		return assert400();
	}

    /**
     * Asserts that the HTTP status is 401 unauthorized.
     */
	default THttpResponse assert401() {
		return assertStatusEquals(HTTP.SC_UNAUTHORIZED);
	}

	/**
	 * Asserts that the HTTP status is 401 unauthorized.
	 */
	default THttpResponse assertUnAuthorized() {
		return assertStatusEquals(HTTP.SC_UNAUTHORIZED);
	}

    /**
     * Asserts that the HTTP status is 403 forbidden.
     */
	default THttpResponse assert403() {
		return assertStatusEquals(HTTP.SC_FORBIDDEN);
	}

	/**
	 * Asserts that the HTTP status is 403 forbidden.
	 */
	default THttpResponse assertForbidden() {
		return assertStatusEquals(HTTP.SC_FORBIDDEN);
	}

    /**
     * Asserts that the HTTP status is 404 not found.
     */
	default THttpResponse assert404() {
		return assertStatusEquals(HTTP.SC_NOT_FOUND);
	}

    /**
     * Asserts that the HTTP status is 404 not found.
     */
    default THttpResponse assertNotFound() {
        return assertStatusEquals(HTTP.SC_NOT_FOUND);
    }

    /**
     * Asserts that the HTTP status is 500 internal server error.
     */
	default THttpResponse assert500() {
		return assertStatusEquals(HTTP.SC_INTERNAL_SERVER_ERROR);
	}

    /**
     * Asserts that the HTTP status is 500 internal server error.
     */
    default THttpResponse assertInternalServerError() {
        return assertStatusEquals(HTTP.SC_INTERNAL_SERVER_ERROR);
    }

    /**
     * Asserts that the HTTP status is 302.
     */
    default THttpResponse assertRedirect() {
        return assertStatusEquals(HTTP.SC_FOUND);
    }

    /**
     * Asserts that the HTTP status is equals to the given status.
     */
	THttpResponse assertStatusEquals(int status);

    /**
     * Asserts that the content of response body are equals to the given string.
     */
	THttpResponse assertContentEquals(String expectedContent);

    /**
     * Asserts that the content of response body is empty.
     */
	THttpResponse assertContentEmpty();

    /**
     * Asserts that the content of response body is not empty.
     */
    THttpResponse assertContentNotEmpty();

    /**
     * Asserts that content of response body are contains the given string.
     */
	THttpResponse assertContentContains(String containsContent);

	/**
	 * Asserts response status is 2xx
	 */
	THttpResponse assertSuccess();

	/**
	 * Asserts response status is >= 300
	 */
	THttpResponse assertFailure();

	/**
	 * Asserts response status is 200
	 */
	THttpResponse assertOk();
	
	/**
	 * Asserts response status is not 200.
	 */
	THttpResponse assertNotOk();
	
	/**
	 * Asserts response content type is {@link MimeTypes#TEXT_HTML}
	 */
	THttpResponse assertContentTypeHtml();

	/**
	 * Asserts response content type is {@link MimeTypes#TEXT_PLAIN}
	 */
	THttpResponse assertContentTypeText();

    /**
     * Asserts response content type equals to the given media type.
     *
     * <p/>
     * Example :
     *
     * <pre>
     *     assertContentTypeEquals("text/plain");
     * </pre>
     */
	THttpResponse assertContentTypeEquals(String mediaType);

    /**
     * Asserts response content type equals to the given media type and matches the given charset.
     *
     * <p/>
     * Example :
     *
     * <pre>
     *     assertContentTypeEquals("text/plain","utf-8);
     * </pre>
     */
	THttpResponse assertContentTypeEquals(String mediaType, String charset);

    /**
     * Asserts the content type is present in http response.
     */
	THttpResponse assertContentTypePresent();

    /**
     * Asserts the header's value equals the expected value.
     */
	THttpResponse assertHeaderEquals(String headerName, String expectedValue);

    /**
     * Asserts the charset in content type equals to the expected value.
     */
	THttpResponse assertCharsetEquals(String expectedCharset);

    /**
     * Asserts the response body is json format.
     */
    default THttpResponse assertJsonBody() {
        getJson();
        return this;
    }
}