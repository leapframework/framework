/*
 * Copyright 2016 the original author or authors.
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

import leap.web.Content;

/**
 * A base runtime application exception indicating a client request error (HTTP 4xx status codes).
 */
public class ClientErrorException extends ResponseException {

    public ClientErrorException(int status) {
        super(status);
    }

    public ClientErrorException(int status, Content content) {
        super(status, content);
    }

    public ClientErrorException(int status, String message) {
        super(status, message);
    }

    public ClientErrorException(int status, Throwable cause) {
        super(status, cause);
    }

    public ClientErrorException(int status, String message, Throwable cause) {
        super(status, message, cause);
    }

    public ClientErrorException(int status, String message, Content content) {
        super(status, message, content);
    }

    public ClientErrorException(int status, String message, Content content, Throwable cause) {
        super(status, message, content, cause);
    }

}
