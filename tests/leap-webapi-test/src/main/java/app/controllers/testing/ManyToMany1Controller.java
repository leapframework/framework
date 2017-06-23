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

package app.controllers.testing;

import app.models.testing.ManyToManyModel1;
import leap.core.security.annotation.AllowAnonymous;
import leap.web.annotation.http.GET;
import leap.web.annotation.http.POST;
import leap.web.api.mvc.ApiResponse;
import leap.web.api.mvc.ModelController;
import leap.web.api.mvc.params.Partial;
import leap.web.api.mvc.params.QueryOptions;

import java.util.List;

@AllowAnonymous
public class ManyToMany1Controller extends ModelController<ManyToManyModel1> {

    @POST
    public ApiResponse create(Partial<ManyToManyModel1> model) {
        return super.create(model);
    }

    @GET
    public ApiResponse<List<ManyToManyModel1>> getAll(QueryOptions options) {
        return super.queryList(options);
    }

}