/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  
 */

package app.controllers.api;

import leap.core.annotation.Inject;
import leap.core.security.annotation.AllowAnonymous;
import leap.core.validation.annotations.Required;
import leap.web.App;
import leap.web.annotation.Path;
import leap.web.annotation.http.POST;
import leap.web.api.mvc.ApiController;
import leap.web.api.mvc.ApiResponse;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by kael on 2017/2/20.
 */
@Path("arg_bind")
@AllowAnonymous
public class ArgumentBindController extends ApiController {

    private @Inject App app;
    
    @POST
    @Path("/date")
    public ApiResponse<Boolean> testDate(@Required Date DATETIME_PATTERN,
                                         @Required Date DATE_PATTERN,
                                         @Required Date TIMESTAMP_PATTERN,
                                         @Required Date RFC3339_DATE_PATTERN1,
                                         @Required Date ISO8601_DATE_PATTERN,
                                         @Required Date TIME_PATTERN,
                                         @Required Timestamp T_DATETIME_PATTERN,
                                         @Required Timestamp T_DATE_PATTERN,
                                         @Required Timestamp T_TIMESTAMP_PATTERN,
                                         @Required Timestamp T_RFC3339_DATE_PATTERN1,
                                         @Required Timestamp T_ISO8601_DATE_PATTERN,
                                         @Required Timestamp T_TIME_PATTERN){
        return ApiResponse.ok(true);
    }
}
