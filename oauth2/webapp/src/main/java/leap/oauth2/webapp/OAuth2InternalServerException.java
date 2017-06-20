/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package leap.oauth2.webapp;

import leap.lang.http.HTTP;

public class OAuth2InternalServerException extends OAuth2ResponseException {

    private static final long serialVersionUID = -4785207376547225125L;

    public OAuth2InternalServerException(Throwable cause) {
        super(HTTP.SC_INTERNAL_SERVER_ERROR, OAuth2Errors.ERROR_SERVER_ERROR, cause.getMessage());
    }

    public OAuth2InternalServerException(String message) {
        super(HTTP.SC_INTERNAL_SERVER_ERROR, OAuth2Errors.ERROR_SERVER_ERROR, message);
    }

}