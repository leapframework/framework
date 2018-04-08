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

package app.controllers.testing;

import leap.core.security.annotation.AllowAnonymous;
import leap.web.annotation.http.GET;
import leap.web.api.mvc.ApiController;
import leap.core.security.annotation.Permissions;

public class PermsController extends ApiController {

    @GET("/test_perm")
    @Permissions("test")
    public void testPerm() {

    }

    @GET("test_anonymous")
    @AllowAnonymous
    public void testAnonymous() {

    }

}