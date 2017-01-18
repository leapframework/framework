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

import leap.core.security.annotation.AllowAnonymous;
import leap.lang.Strings;
import leap.lang.http.HTTP;
import leap.web.annotation.Consumes;
import leap.web.annotation.Path;
import leap.web.annotation.http.POST;
import leap.web.api.mvc.ApiController;
import leap.web.api.mvc.ApiResponse;
import leap.web.multipart.MultipartFile;

/**
 * Created by kael on 2016/11/11.
 */
@Path("/upload")
public class UploadController extends ApiController {
    @POST("/{id}/file")
    @Consumes("multipart")
    @AllowAnonymous
    public ApiResponse<String> uploadFileWithExpressionPath(String id, MultipartFile file){
        if(Strings.isNotEmpty(id) && file != null){
            return ApiResponse.ok("ok");
        }
        return ApiResponse.err(HTTP.Status.INTERNAL_SERVER_ERROR,"fail");
    }

}
