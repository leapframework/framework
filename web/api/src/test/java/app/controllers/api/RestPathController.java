/*
 *
 *  * Copyright 2016 the original author or authors.
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

import app.models.api.RestOperation;
import app.models.api.RestPath;
import leap.lang.New;
import leap.web.annotation.Path;
import leap.web.annotation.http.DELETE;
import leap.web.annotation.http.GET;
import leap.web.annotation.http.PATCH;
import leap.web.annotation.http.POST;
import leap.web.api.mvc.ApiResponse;
import leap.web.api.mvc.ModelController;
import leap.web.api.mvc.params.Partial;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.security.annotation.AllowAnonymous;

import java.util.List;
import java.util.Map;

@Path("/path")
@AllowAnonymous
public class RestPathController extends ModelController<RestPath> {

    @DELETE("/{id}")
    public ApiResponse deletePath(String id) {
        return delete(id);
    }

    @PATCH("/{id}")
    public ApiResponse updatePath(String id, Partial<RestPath> partial) {
        return updatePartial(id, partial);
    }

    @Path("/{path_id}/operations")
    public static final class ApiOperationController extends ModelController<RestOperation> {

        @GET
        public ApiResponse<List<RestOperation>> getApiOperations(String pathId,
                                                                 QueryOptions options) {
            return queryList(options, filters(pathId));
        }

        @POST
        public ApiResponse<RestOperation> createApiOperation(String pathId,
                                                             Partial<RestOperation> operation) {

            return createAndReturn(operation, filters(pathId));

        }

        private Map<String, Object> filters(String pathId) {
            return New.hashMap("pathId", pathId);
        }
    }

}