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

import app.models.petstore.Order;
import leap.web.annotation.http.DELETE;
import leap.web.annotation.http.GET;
import leap.web.annotation.http.POST;
import leap.web.api.controller.ApiController;
import leap.web.api.controller.ApiResponse;

import java.util.Map;

public class StoreController extends ApiController {

    @GET("/inventory")
    public ApiResponse<Map<String,Integer>> getInventory() {
        return ApiResponse.OK;
    }

    @POST("/order")
    public ApiResponse<Order> placeOrder(Order order) {
        return ApiResponse.OK;
    }

    @GET("/order/{orderId}")
    public ApiResponse<Order> getOrderById(Long orderId) {
        return ApiResponse.OK;
    }

    @DELETE("/order/{orderId}")
    public ApiResponse deleteOrder(Long orderId) {
        return ApiResponse.OK;
    }

}
