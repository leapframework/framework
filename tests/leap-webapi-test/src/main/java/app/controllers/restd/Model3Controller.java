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

package app.controllers.restd;

import app.models.restd.Model3;
import leap.web.annotation.http.DELETE;
import leap.web.annotation.http.GET;
import leap.web.annotation.http.PATCH;
import leap.web.annotation.http.POST;
import leap.web.api.mvc.ApiResponse;
import leap.web.api.mvc.ModelController;
import leap.web.api.mvc.params.FindOptions;
import leap.web.api.mvc.params.Partial;
import leap.web.api.mvc.params.QueryOptions;

public class Model3Controller extends ModelController<Model3> {

    @GET("/{id}")
    public ApiResponse find(String id, FindOptions options) {
        return ApiResponse.NOT_IMPLEMENTED;
    }

    @POST
    public ApiResponse create(Partial<Model3> model3) {
        return ApiResponse.NOT_IMPLEMENTED;
    }

    @PATCH("/{id}")
    public ApiResponse update(String id, Partial<Model3> model3) {
        return ApiResponse.NOT_IMPLEMENTED;
    }

    @DELETE("/{id}")
    public ApiResponse delete(String id) {
        return ApiResponse.NOT_IMPLEMENTED;
    }

    @GET
    public ApiResponse query(QueryOptions options) {
        return ApiResponse.NOT_IMPLEMENTED;
    }
}
