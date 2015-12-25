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

import leap.core.web.ResponseBase;
import leap.lang.json.JsonWriter;

/**
 * Indicates a http response.
 */
public abstract class Response implements ResponseBase {
	
	/**
	 * Returns <code>true</code> indicates this response had status setted or content written.
	 */
	public abstract boolean isHandled();

	/**
	 * Marks the response was handled.
	 */
	public abstract void markHandled();
	
	/**
	 * Returns the {@link JsonWriter}.
	 */
	public abstract JsonWriter getJsonWriter();
}