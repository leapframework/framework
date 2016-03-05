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

public class ForbiddenException extends ClientErrorException {

	public ForbiddenException() {
	    super(HTTP.SC_FORBIDDEN, "Forbidden");
    }

	public ForbiddenException(String message) {
	    super(HTTP.SC_FORBIDDEN, message);
    }

	public ForbiddenException(Content content) {
	    super(HTTP.SC_FORBIDDEN, content);
    }

	public ForbiddenException(Throwable cause) {
	    super(HTTP.SC_FORBIDDEN, cause);
    }

	public ForbiddenException(String message, Throwable cause) {
	    super(HTTP.SC_FORBIDDEN, message, cause);
    }

	public ForbiddenException(String message, Content content) {
	    super(HTTP.SC_FORBIDDEN, message, content);
    }

	public ForbiddenException(String message, Content content, Throwable cause) {
	    super(HTTP.SC_FORBIDDEN, message, content, cause);
    }
}
