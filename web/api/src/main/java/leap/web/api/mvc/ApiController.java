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

import leap.web.annotation.Consumes;
import leap.web.annotation.Failure;
import leap.web.annotation.Produces;
import leap.web.annotation.Restful;
import leap.web.api.config.ApiConfig;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.spec.swagger.SwaggerConstants;
import leap.web.json.JsonSerialize;
import leap.core.security.annotation.AllowClientOnly;

@Consumes("json")
@Produces("json")
@AllowClientOnly
@Restful
@Failure(handler = ApiFailureHandler.class)
@JsonSerialize(dateFormat = SwaggerConstants.DATE_TIME_FORMAT)
public abstract class ApiController {

    protected ApiConfig   apiConfig;
    protected ApiMetadata apiMetadata;

}