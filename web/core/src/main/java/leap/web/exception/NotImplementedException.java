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
package leap.web.exception;

import leap.lang.http.HTTP;
import leap.web.Content;

public class NotImplementedException extends ServerErrorException {

	public NotImplementedException() {
	    super(HTTP.SC_NOT_IMPLEMENTED);
    }

	public NotImplementedException(String message) {
	    super(HTTP.SC_NOT_IMPLEMENTED, message);
    }

	public NotImplementedException(Content content) {
	    super(HTTP.SC_NOT_IMPLEMENTED, content);
    }

	public NotImplementedException(Throwable cause) {
	    super(HTTP.SC_NOT_IMPLEMENTED, cause);
    }

	public NotImplementedException(String message, Throwable cause) {
	    super(HTTP.SC_NOT_IMPLEMENTED, message, cause);
    }

	public NotImplementedException(String message, Content content) {
	    super(HTTP.SC_NOT_IMPLEMENTED, message, content);
    }

	public NotImplementedException(String message, Content content, Throwable cause) {
	    super(HTTP.SC_NOT_IMPLEMENTED, message, content, cause);
    }
}