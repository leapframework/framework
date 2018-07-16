/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package leap.web.api.mvc;

import leap.web.exception.ResponseException;

public class ApiResponseException extends ResponseException {

    protected final String code;

    public ApiResponseException(int status, String code) {
        super(status);
        this.code = code;
    }

    public ApiResponseException(int status, String code, String message) {
        super(status, message);
        this.code = code;
    }

    public ApiResponseException(int status, String code, Throwable cause) {
        super(status, cause);
        this.code = code;
    }

    public ApiResponseException(int status, String code, String message, Throwable cause) {
        super(status, message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
