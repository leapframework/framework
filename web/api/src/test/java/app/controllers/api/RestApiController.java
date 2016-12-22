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

import app.models.PartialModel;
import app.models.api.RestApi;
import app.models.api.RestModel;
import app.models.api.RestOperation;
import app.models.api.RestPath;
import leap.core.value.Record;
import leap.lang.New;
import leap.lang.Strings;
import leap.orm.mapping.EntityMapping;
import leap.orm.query.CriteriaQuery;
import leap.web.annotation.Path;
import leap.web.annotation.Produces;
import leap.web.annotation.http.DELETE;
import leap.web.annotation.http.GET;
import leap.web.annotation.http.PATCH;
import leap.web.annotation.http.POST;
import leap.web.api.mvc.ApiResponse;
import leap.web.api.mvc.ModelController;
import leap.web.api.mvc.params.DeleteOptions;
import leap.web.api.mvc.params.Partial;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.mvc.params.QueryOptionsBase;
import leap.core.security.annotation.AllowAnonymous;

import java.util.List;

@Path("restapi")
@AllowAnonymous
public class RestApiController extends ModelController<RestApi> {

    @GET("/path_decode/{path}")
    public ApiResponse<String> pathDecode(String path){
        return ApiResponse.ok(path);
    }
    @POST("/create/{id}")
    public ApiResponse<RestApi> createAndReturnWithId(Partial<RestApi> api,String id){
        ApiResponse<RestApi> response = createAndReturn(api,id);
        return response;
    }
    
    @POST("/convert")
    public ApiResponse<PartialModel> convertPartialToObject(Partial<PartialModel> api){
        PartialModel o = api.getObject();
        return ApiResponse.ok(o);
    }

    @GET
    public ApiResponse<List<RestApi>> getRestApis(QueryOptions options) {
        return queryList(options,query -> {

        });
    }

    @POST
    public ApiResponse<RestApi> createRestApi(Partial<RestApi> api) {
        return createAndReturn(api);
    }

    @AllowAnonymous
    @GET("/published")
    public ApiResponse<List<RestApi>> getPublishedApis(QueryOptions options){
        return queryList(options, New.hashMap("published", true));
    }

    @GET("/{id}")
    public ApiResponse<RestApi> getRestApi(String id, QueryOptionsBase options) {
        return get(id,options);
    }

    @DELETE("/{id}")
    public ApiResponse deleteRestApi(String id, DeleteOptions options) {
        return delete(id, options);
    }

    @PATCH("/{id}")
    public ApiResponse updateRestApi(String id, Partial<RestApi> patch) {
        return updatePartial(id, patch);
    }

    @Path("/{api_id}/paths")
    @AllowAnonymous
    public final class SubPathsController extends ModelController<RestPath> {

        @GET
        public ApiResponse<List<RestPath>> getApiPaths(String apiId, QueryOptions options, String extend) {
            ApiResponse<List<RestPath>> resp = queryList(options, New.hashMap("apiId", apiId));
            if(Strings.contains(extend,"operations")){
                List<Record> records = (List<Record>)resp.getEntity();
                for(Record record : records){
                    EntityMapping em = dao.getOrmContext().getMetadata().getEntityMapping(RestOperation.class);
                    CriteriaQuery<Record> query = dao.createCriteriaQuery(em);
                    List<Record> operations = query.select("id,pathId as \"parentId\", httpMethod as \"method\"").where("pathId = ?",record.get("id")).list();
                    record.put("operations",operations);
                }
            }
            return resp;
        }

        @POST
        public ApiResponse<RestPath> createApiPath(String apiId, Partial<RestPath> path) {
            return createAndReturn(path, New.hashMap("apiId", apiId));
        }

    }

    @Path("/{api_id}/models")
    @AllowAnonymous
    public static final class SubModelsControler extends ModelController<RestModel> {

        @GET
        public ApiResponse<List<RestModel>> getApiModels(String apiId, QueryOptions options) {
            return queryList(options, New.hashMap("apiId", apiId));
        }

        @POST
        public ApiResponse<RestModel> createApiModel(String apiId, Partial<RestModel> path) {
            return createAndReturn(path, New.hashMap("apiId", apiId));
        }

    }

}