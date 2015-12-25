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
package leap.web;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import leap.lang.Classes;
import leap.lang.Objects2;
import leap.lang.http.HTTP;

//some codes from spring framework

public class RenderableRedirect implements Renderable {
	
	public static final String ROOT_PATH_PREFIX = "^/";
	
	public static final int STATUS_MOVED_PERMANENTLY  = HTTP.SC_MOVED_PERMANENTLY;
	public static final int STATUS_FOUND              = HTTP.SC_FOUND;
	public static final int STATUS_SEE_OTHER          = HTTP.SC_SEE_OTHER;
	public static final int STATUS_TEMPORARY_REDIRECT = HTTP.SC_TEMPORARY_REDIRECT;
	
	public static final int STATUS_DEFAULT            = STATUS_FOUND;
	
	private final String 	     location;
	private final Map<String, ?> params;
	
	public RenderableRedirect(String uri){
		this.location        = uri;
		this.params = null;
	}
	
	public RenderableRedirect(String uri,Map<String, ?> params){
		this.location       = uri;
		this.params = params;
	}

	@Override
    public void render(Request request, Response response) throws Exception {
		StringBuilder redirectUrl = new StringBuilder();
		
		if(location.startsWith(ROOT_PATH_PREFIX)){
			redirectUrl.append(location.substring(1));
		}else if(location.startsWith("/")){
			redirectUrl.append(request.getContextPath()).append(location);
		}else{
			redirectUrl.append(location);
		}
		
		if(null != params && !params.isEmpty()){
			appendQueryParameters(redirectUrl, params, request.getCharacterEncoding());
		}
		
		response.getServletResponse().sendRedirect(redirectUrl.toString());
    }
	
	/**
	 * Append query properties to the redirect URL.
	 * Stringifies, URL-encodes and formats model attributes as query properties.
	 * @param targetUrl the StringBuilder to append the properties to
	 * @param model Map that contains model attributes
	 * @param encodingScheme the encoding scheme to use
	 * @throws UnsupportedEncodingException if string encoding failed
	 * @see #queryProperties
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void appendQueryParameters(StringBuilder targetUrl, Map<String, ?> params, String encodingScheme) throws UnsupportedEncodingException {

		// Extract anchor fragment, if any.
		String fragment = null;
		int anchorIndex = targetUrl.indexOf("#");
		if (anchorIndex > -1) {
			fragment = targetUrl.substring(anchorIndex);
			targetUrl.delete(anchorIndex, targetUrl.length());
		}

		// If there aren't already some parameters, we need a "?".
		boolean first = (targetUrl.toString().indexOf('?') < 0);
		for (Map.Entry<String, ?> entry : params.entrySet()) {
			if (isEligibleParameter(entry.getKey(), entry.getValue())) {
				Object rawValue = entry.getValue();
				Iterator<Object> valueIter;
				if (rawValue != null && rawValue.getClass().isArray()) {
					valueIter = Arrays.asList(Objects2.toObjectArray(rawValue)).iterator();
				}
				else if (rawValue instanceof Collection) {
					valueIter = ((Collection) rawValue).iterator();
				}
				else {
					valueIter = Collections.singleton(rawValue).iterator();
				}
				while (valueIter.hasNext()) {
					Object value = valueIter.next();
					if (first) {
						targetUrl.append('?');
						first = false;
					}
					else {
						targetUrl.append('&');
					}
					String encodedKey = urlEncode(entry.getKey(), encodingScheme);
					String encodedValue = (value != null ? urlEncode(value.toString(), encodingScheme) : "");
					targetUrl.append(encodedKey).append('=').append(encodedValue);
				}
			}
		}

		// Append anchor fragment, if any, to end of URL.
		if (fragment != null) {
			targetUrl.append(fragment);
		}
	}

	/**
	 * Determine whether the given model element should be exposed
	 * as a query property.
	 * <p>The default implementation considers Strings and primitives
	 * as eligible, and also arrays and Collections/Iterables with
	 * corresponding elements. This can be overridden in subclasses.
	 * @param key the key of the model element
	 * @param value the value of the model element
	 * @return whether the element is eligible as query property
	 */
	@SuppressWarnings("rawtypes")
    protected boolean isEligibleParameter(String key, Object value) {
		if (value == null) {
			return false;
		}
		if (isEligibleValue(value)) {
			return true;
		}

		if (value.getClass().isArray()) {
			int length = Array.getLength(value);
			if (length == 0) {
				return false;
			}
			for (int i = 0; i < length; i++) {
				Object element = Array.get(value, i);
				if (!isEligibleValue(element)) {
					return false;
				}
			}
			return true;
		}

		if (value instanceof Collection) {
			Collection coll = (Collection) value;
			if (coll.isEmpty()) {
				return false;
			}
			for (Object element : coll) {
				if (!isEligibleValue(element)) {
					return false;
				}
			}
			return true;
		}

		return false;
	}

	/**
	 * Determine whether the given model element value is eligible for exposure.
	 * <p>The default implementation considers primitives, Strings, Numbers, Dates,
	 * URIs, URLs and Locale objects as eligible. This can be overridden in subclasses.
	 * @param value the model element value
	 * @return whether the element value is eligible
	 */
	protected boolean isEligibleValue(Object value) {
		return (value != null && Classes.isSimpleValueType(value.getClass()));
	}

	/**
	 * URL-encode the given input String with the given encoding scheme.
	 * <p>The default implementation uses {@code URLEncoder.encode(input, enc)}.
	 * @param input the unencoded input String
	 * @param encodingScheme the encoding scheme
	 * @return the encoded output String
	 * @throws UnsupportedEncodingException if thrown by the JDK URLEncoder
	 * @see java.net.URLEncoder#encode(String, String)
	 * @see java.net.URLEncoder#encode(String)
	 */
	protected String urlEncode(String input, String encodingScheme) throws UnsupportedEncodingException {
		return (input != null ? URLEncoder.encode(input, encodingScheme) : null);
	}

	@Override
    public String toString() {
		return "{Redirect:'" + location + "'}";
    }
}