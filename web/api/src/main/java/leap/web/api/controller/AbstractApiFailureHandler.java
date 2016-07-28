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

package leap.web.api.controller;

import leap.lang.http.HTTP;
import leap.web.Result;

public abstract class AbstractApiFailureHandler implements ApiFailureHandler {

    protected void badRequest(Result result, String message) {
        result.setStatus(HTTP.SC_BAD_REQUEST);
        result.setRenderable(new ApiError(message));
    }

    protected void notFound(Result result, String message) {
        result.setStatus(HTTP.SC_NOT_FOUND);
        result.setRenderable(new ApiError(message));
    }

    protected void errResponse(Result result, int status, String message) {
        result.setStatus(status);
        result.setRenderable(new ApiError(message));
    }

    protected void internalServerError(Result result, String message) {
        result.setStatus(HTTP.SC_INTERNAL_SERVER_ERROR);
        result.setRenderable(new ApiError(message));
    }

}
