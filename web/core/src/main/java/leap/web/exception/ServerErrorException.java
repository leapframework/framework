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

public class ServerErrorException extends ResponseException {

	public ServerErrorException() {
	    super(HTTP.SC_INTERNAL_SERVER_ERROR);
    }

	public ServerErrorException(String message) {
	    super(HTTP.SC_INTERNAL_SERVER_ERROR, message);
    }

	public ServerErrorException(Content content) {
	    super(HTTP.SC_INTERNAL_SERVER_ERROR, content);
    }

	public ServerErrorException(Throwable cause) {
	    super(HTTP.SC_INTERNAL_SERVER_ERROR, cause);
    }

	public ServerErrorException(String message, Throwable cause) {
	    super(HTTP.SC_INTERNAL_SERVER_ERROR, message, cause);
    }

	public ServerErrorException(String message, Content content) {
	    super(HTTP.SC_INTERNAL_SERVER_ERROR, message, content);
    }

	public ServerErrorException(String message, Content content, Throwable cause) {
	    super(HTTP.SC_INTERNAL_SERVER_ERROR, message, content, cause);
    }
}
