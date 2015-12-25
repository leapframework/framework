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

import java.io.InputStream;

import leap.lang.exception.NestedIOException;
import leap.lang.http.HTTP;
import leap.lang.http.HTTP.Status;
import leap.lang.http.Header;
import leap.lang.http.Headers;
import leap.lang.http.MimeType;
import leap.lang.http.MimeTypes;
import leap.lang.json.JSON;
import leap.lang.json.JsonValue;
import leap.lang.jsoup.nodes.Document;

public interface THttpResponse {
	
	/**
	 * Returns the corresponding request of this response.
	 */
	THttpRequest request();
	
	Integer getStatus();

	boolean isOk();

	boolean isSuccess();

	String getLocation();
	
	default String getRedirectUrl() {
	    return getLocation();
	}

	long getContentLength();

	/**
	 * Returns the response header value of {@link Headers#CONTENT_TYPE}
	 */
	String getContentType();
	
	MimeType getMimeType();

	String getMediaType();

	String getCharset();

	String getHeader(String name);

	Header[] getHeaders(String name);
	
	String getContent() throws NestedIOException;
	
	default JsonValue getJson() throws NestedIOException {
		return JSON.decodeToJsonValue(getContent());
	}

	InputStream getInputStream() throws NestedIOException;
	
	Document getDocument() throws NestedIOException;
	
	default THttpResponse assert400() {
		return assertStatusEquals(HTTP.SC_BAD_REQUEST);
	}
	
	default THttpResponse assert401() {
		return assertStatusEquals(HTTP.SC_UNAUTHORIZED);
	}
	
	default THttpResponse assert403() {
		return assertStatusEquals(HTTP.SC_FORBIDDEN);
	}
	
	default THttpResponse assert404() {
		return assertStatusEquals(HTTP.SC_NOT_FOUND);
	}
	
	default THttpResponse assert500() {
		return assertStatusEquals(HTTP.SC_INTERNAL_SERVER_ERROR);
	}

	THttpResponse assertStatusEquals(int status);

	THttpResponse assertContentEquals(String expectedContent);

	THttpResponse assertContentEmpty();
	
	THttpResponse assertContentContains(String containsContent);

	/**
	 * Asserts response status is 2xx
	 */
	THttpResponse assertSuccess();

	/**
	 * Asserts response status is 200
	 */
	THttpResponse assertOk();
	
	/**
	 * Asserts response status is not 200.
	 */
	THttpResponse assertNotOk();
	
	/**
	 * Asserts response status is 404
	 */
	default THttpResponse assertNotFound() {
		return assertStatusEquals(Status.NOT_FOUND.value());
	}
	
	/**
	 * Asserts response status is 302.
	 */
	default THttpResponse assertRecirect() {
	    return assertStatusEquals(Status.FOUND.value());
	}

	/**
	 * Asserts response content type is {@link MimeTypes#TEXT_HTML}
	 */
	THttpResponse assertContentTypeHtml();

	/**
	 * Asserts response content type is {@link MimeTypes#TEXT_PLAIN}
	 */
	THttpResponse assertContentTypeText();

	THttpResponse assertContentTypeEquals(String mediaType);

	THttpResponse assertContentTypeEquals(String mediaType, String charset);

	THttpResponse assertContentTypePresent();

	THttpResponse assertHeaderEquals(String headerName, String expectedValue);

	THttpResponse assertCharsetEquals(String expectedCharset);

}