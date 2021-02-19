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

import leap.core.annotation.Inject;
import leap.lang.html.HTML;
import leap.web.Response;
import leap.web.json.JsonConfig;

public class DefaultApiErrorHandler implements ApiErrorHandler {

    @Inject
    protected JsonConfig jc;

    @Override
    public void responseError(Response response, int status, ApiError error) {
        response.setStatus(status);
        if (jc.isHtmlEscape()) {
            error.setMessage(HTML.escape(error.getMessage()));
        }
        error.response(response);
    }

}