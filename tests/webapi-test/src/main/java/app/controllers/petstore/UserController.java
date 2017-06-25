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

package app.controllers.petstore;

import app.models.petstore.User;
import leap.core.validation.annotations.Required;
import leap.web.annotation.http.DELETE;
import leap.web.annotation.http.GET;
import leap.web.annotation.http.POST;
import leap.web.annotation.http.PUT;
import leap.web.api.mvc.ApiController;
import leap.web.api.mvc.ApiResponse;

import java.util.List;

public class UserController extends ApiController {

    @POST
    public ApiResponse createUser(@Required User user) {
        return ApiResponse.OK;
    }

    @POST("/createWithArray")
    public ApiResponse createUsersWithArrayInput(User[] users) {
        return ApiResponse.OK;
    }

    @POST("/createWithList")
    public ApiResponse createUsersWithListInput(List<User> users) {
        return ApiResponse.OK;
    }

    @GET("/login")
    public ApiResponse<String> loginUser(@Required String username,@Required String password) {
        return ApiResponse.OK;
    }

    @GET("/logout")
    public ApiResponse logoutUser() {
        return ApiResponse.OK;
    }

    @DELETE("/{username}")
    public ApiResponse deleteUser(String username) {
        return ApiResponse.OK;
    }

    @GET("/{username}")
    public ApiResponse<User> getUserByName(String username) {
        return ApiResponse.OK;
    }

    @PUT("/{username}")
    public ApiResponse updateUser(String username, User user) {
        return ApiResponse.OK;
    }

}