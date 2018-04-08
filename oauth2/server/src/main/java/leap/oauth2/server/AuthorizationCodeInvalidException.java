/*
 * Copyright 2015 the original author or authors.
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
package leap.oauth2.server;

public class AuthorizationCodeInvalidException extends RuntimeException {

    private static final long serialVersionUID = 6719889622580335750L;

    public AuthorizationCodeInvalidException() {
    }

    public AuthorizationCodeInvalidException(String message) {
        super(message);
    }

    public AuthorizationCodeInvalidException(Throwable cause) {
        super(cause);
    }

    public AuthorizationCodeInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthorizationCodeInvalidException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
