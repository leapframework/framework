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

package app.controllers.api;

import leap.lang.New;
import leap.web.annotation.RequestBody;
import leap.web.annotation.http.GET;
import leap.web.annotation.http.POST;
import leap.web.api.mvc.ApiController;
import leap.web.api.mvc.ApiResponse;

import java.util.List;

public class RestTestController extends ApiController {

    @POST("/set_string")
    public ApiResponse setString(@RequestBody String s) {
        return ApiResponse.OK;
    }

    @GET("/get_string")
    public ApiResponse<String> getString() {
        return ApiResponse.ok("hello");
    }

    @GET("/get_strings")
    public ApiResponse<List<String>> getStrings() {
        return ApiResponse.ok(New.arrayList("hello1", "hello2"));
    }

}