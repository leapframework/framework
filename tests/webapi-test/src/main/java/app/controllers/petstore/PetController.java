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

import app.models.petstore.Pet;
import leap.core.validation.annotations.Required;
import leap.web.annotation.HeaderParam;
import leap.web.annotation.QueryParam;
import leap.web.annotation.http.DELETE;
import leap.web.annotation.http.GET;
import leap.web.annotation.http.POST;
import leap.web.api.mvc.ApiController;
import leap.web.api.mvc.ApiResponse;
import leap.web.multipart.MultipartFile;

import java.util.List;

public class PetController extends ApiController{

    @POST
    public ApiResponse addPet(Pet pet) {
        return ApiResponse.OK;
    }

    @GET("/{petId}")
    public ApiResponse<Pet> getPetById(Long petId) {
        return ApiResponse.OK;
    }

    @POST("/{petId}")
    public ApiResponse updatePetWithForm(Long petId, String name, Pet.Status status) {
        return ApiResponse.OK;
    }

    @DELETE("/{petId}")
    public ApiResponse deletePet(@HeaderParam String apiKey, Long petId) {
        return ApiResponse.OK;
    }

    @GET("/findByStatus")
    public ApiResponse<List<Pet>> findPetsByStatus(@QueryParam @Required List<Pet.Status> status) {
        return ApiResponse.OK;
    }

    @GET("/findByTags")
    public ApiResponse<List<Pet>> findPetsByTags(@QueryParam @Required List<String> tags) {
        return ApiResponse.OK;
    }

    @POST("/{petId}/uploadImage")
    public ApiResponse uploadFile(Long petId, String additionalMetadata, MultipartFile file) {
        return ApiResponse.OK;
    }

}