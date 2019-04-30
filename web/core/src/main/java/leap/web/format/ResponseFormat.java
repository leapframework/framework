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
package leap.web.format;

import leap.lang.Named;
import leap.lang.http.MimeType;
import leap.web.Content;
import leap.web.action.Action;
import leap.web.action.ActionContext;

public interface ResponseFormat extends Named {

	String HTML = "html";
	String JSON = "json";
	String TEXT = "text";

    /**
     * Returns the primary mime type of this format.
     */
    MimeType getPrimaryMimeType();
	
	/**
	 * Returns <code>true</code> if this format supports the given {@link Action}.
	 */
	boolean supports(Action action);
	
	/**
	 * Returns <code>true</code> if this format supports the given media type.
	 */
	boolean supports(MimeType mediaType);

	/**
	 * Returns a {@link Content} object for rendering the given result value of action.
	 */
	Content getContent(ActionContext context, Object value) throws Exception;

}