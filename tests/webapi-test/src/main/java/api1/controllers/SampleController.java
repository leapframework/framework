/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package api1.controllers;

import leap.core.security.annotation.AllowAnonymous;
import leap.web.annotation.RequestBody;
import leap.web.annotation.http.POST;
import leap.web.api.mvc.ApiController;
import leap.web.api.mvc.ApiResponse;

import java.util.List;

@AllowAnonymous
public class SampleController extends ApiController {

    @POST("/list_string1")
    public ApiResponse<List<String>> listString1(List<String> ids) {
        return ApiResponse.ok(ids);
    }

    @POST("/list_string2")
    public ApiResponse<List<String>> listString2(@RequestBody  List<String> ids) {
        return ApiResponse.ok(ids);
    }

}