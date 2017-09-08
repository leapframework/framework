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

import leap.core.doc.annotation.Doc;
import leap.core.security.annotation.AllowAnonymous;
import leap.web.annotation.Path;
import leap.web.annotation.http.GET;
import leap.web.api.mvc.ApiController;
import leap.web.api.mvc.ApiResponse;

@Path("profile")
public class ProfileController extends ApiController {

    @GET("/mobile_api")
    @Doc(profile = "mobile")
    public ApiResponse mobile(){
        return ApiResponse.ACCEPTED;
    }

    @GET("/web_api")
    @Doc(profile = "web")
    public ApiResponse web(){
        return ApiResponse.ACCEPTED;
    }

    @GET("/common_api")
    public ApiResponse common(){
        return ApiResponse.ACCEPTED;
    }
}
