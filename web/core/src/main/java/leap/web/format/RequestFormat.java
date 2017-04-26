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
package leap.web.format;

import leap.lang.Named;
import leap.lang.http.MimeType;
import leap.web.Request;
import leap.web.action.Action;
import leap.web.action.Argument;

import java.io.IOException;

public interface RequestFormat extends Named {
	
	/**
	 * Returns <code>true</code> if this format supports the given action.
	 */
	boolean supports(Action action);
	
	/**
	 * Returns <code>true</code> if this format supports the given media type.
	 */
	boolean supports(MimeType mediaType);

	/**
	 * Returns <code>true</code> if this format supports reading request body message.
	 */
	boolean supportsRequestBody();
	
	/**
	 * Reads the raw body content of the given {@link Request}.
	 * 
	 * @throws IllegalStateException if this format does not supports request body.
	 */
	Object readRequestBody(Request request, Argument argument) throws IOException, IllegalStateException;
}