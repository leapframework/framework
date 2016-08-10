/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.web.api.mvc;

import leap.web.Response;

public interface ApiErrorHandler {

    void unauthorized(Response response);

    void unauthorized(Response response, String message);

    void forbidden(Response response);

    void forbidden(Response response, String message);

    void notFound(Response response);

    void notFound(Response response, String message);

    void badRequest(Response response);

    void badRequest(Response response, String message);

    void internalServerError(Response response, String message);

    void internalServerError(Response response, Throwable cause);

    void responseError(Response response, int status, String message);

    void responseError(Response response, int status, ApiError error);

    void responseError(Response response, int status, String code, String message);

}
