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

public class OAuth2InvalidTokenException extends OAuth2Exception {

    private static final long serialVersionUID = 3709244650746287434L;

    public OAuth2InvalidTokenException() {
    }

    public OAuth2InvalidTokenException(String message) {
        super(message);
    }

    public OAuth2InvalidTokenException(Throwable cause) {
        super(cause);
    }

    public OAuth2InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public OAuth2InvalidTokenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
