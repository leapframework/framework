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

import leap.web.annotation.Produces;
import leap.web.annotation.http.GET;
import leap.web.api.mvc.ApiController;
import leap.web.api.mvc.ApiResponse;
import leap.core.security.annotation.AllowAnonymous;

import java.util.Date;

@AllowAnonymous
public class TypesController extends ApiController {

    @GET("/date")
    public Date date() {
        return new Date();
    }

    @GET("/plain_text")
    @Produces("text")
    public String plainText() {
        return "Hello";
    }

    @GET("plain_text1")
    @Produces("text")
    public ApiResponse<String> plainText1() {
        return ApiResponse.of("Hello1");
    }

    @GET("/plain_text_long")
    @Produces("text")
    public String plainTextLong() {
        StringBuilder s = new StringBuilder();

        for(int i=0;i<10000;i++) {
            s.append('0');
        }

        return s.toString();
    }

    @GET("/json_text")
    public String jsonText() {
        return "Hello";
    }

    @GET("/json_text_long")
    public String jsonTextLong() {
        StringBuilder s = new StringBuilder();

        for(int i=0;i<10000;i++) {
            s.append('0');
        }

        return s.toString();
    }

    @GET("json_text1")
    public ApiResponse<String> jsonText1() {
        return ApiResponse.of("Hello1");
    }
}