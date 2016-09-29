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

package tests.model;

import app.models.api.*;
import leap.lang.New;
import leap.lang.http.HTTP;
import leap.webunit.WebTestBase;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class RestApiControllerTest extends WebTestBase {

    private static Category c1   = null;
    private static Category c2   = null;
    private static RestApi  api1 = null;
    private static RestApi  api2 = null;

    @Test
    public void testQueryOne() {
        RestApi result = get("/api/restapi/" + api1.getId()).decodeJson(RestApi.class);
        assertEquals(result.getId(), api1.getId());
        assertEquals(result.getName(), api1.getName());
        assertNotNull(result.getCreatedAt());

        get("/api/restapi/not_exists").assertNotFound();
    }

    @Test
    public void testQueryOneWithSelect() {
        Map<String,Object> record =
                get("/api/restapi/" + api1.getId() + "?select=id,name,title").decodeJsonMap();

        assertEquals(3, record.size());
        assertEquals(api1.getId(), record.get("id"));

        get("/api/restapi/" + api1.getId() + "?select=not_exists").assertBadRequest();
    }

    @Test
    public void testQueryOneWithExpand() {
        RestApi record =
                get("/api/restapi/" + api1.getId() + "?expand=categories").decodeJson(RestApi.class);

        List<Category> categories = record.getCategories();
        assertEquals(1, categories.size());
        assertEquals(c1.getId(), categories.get(0).getId());
        assertNotNull(categories.get(0).getCreatedAt());

        Map<String,Object> map =
                get("/api/restapi/" + api1.getId() + "?expand=categories(id,title)").decodeJsonMap();
        List<Map<String,Object>> categoriesListMap = (List<Map<String,Object>>)map.get("categories");
        Map<String,Object> categoriesMap = categoriesListMap.get(0);
        assertEquals(2, categoriesMap.size());
        assertEquals(c1.getId(), categoriesMap.get("id"));

        //bad request
        get("/api/restapi/" + api1.getId() + "?expand=not_exists").assertBadRequest();
        get("/api/restapi/" + api1.getId() + "?expand=categories(not_exists)").assertBadRequest();
    }

    @Test
    public void testQueryListWithSelect() {
        List<Map<String,Object>> records =
                get("/api/restapi?select=id,name,title").getJson().asList();

        Map<String,Object> record = records.get(0);

        assertEquals(3, record.size());
        assertEquals(api1.getId(), record.get("id"));

        //bad request
        get("/api/restapi?select=not_exists").assertBadRequest();
    }

    @Test
    public void testQueryListWithExpand() {
        List<Map<String,Object>> records =
                get("/api/restapi?expand=categories(id,title)").getJson().asList();

        Map<String,Object> map = records.get(0);
        List<Map<String,Object>> categoriesListMap = (List<Map<String,Object>>)map.get("categories");
        Map<String,Object> categoriesMap = categoriesListMap.get(0);
        assertEquals(2, categoriesMap.size());
        assertEquals(c1.getId(), categoriesMap.get("id"));

        //bad request
        get("/api/restapi?expand=not_exists").assertBadRequest();
        get("/api/restapi?expand=categories(not_exists)").assertBadRequest();
    }

    @Test
    public void testQueryListWithFieldFilters() {
        RestApi[] apis = get("/api/restapi?name=not_exists").decodeJsonArray(RestApi.class);
        assertEquals(0, apis.length);

        apis = get("/api/restapi?name=api1").decodeJsonArray(RestApi.class);
        assertEquals(1, apis.length);
    }

    @Test
    public void testQueryListWithRelationalFilters() {
        RestApi[] apis = get("/api/restapi?categories=not_exists").decodeJsonArray(RestApi.class);
        assertEquals(0, apis.length);

        apis = get("/api/restapi?categories=" + c1.getId()).decodeJsonArray(RestApi.class);
        assertEquals(1, apis.length);

        apis = get("/api/restapi?categories=" + c1.getId() + "," + c2.getId()).decodeJsonArray(RestApi.class);
        assertEquals(2, apis.length);

        apis = get("/api/restapi?categories=" + c1.getId() + "&categories=" + c2.getId()).decodeJsonArray(RestApi.class);
        assertEquals(2, apis.length);

        apis = get("/api/restapi?filters=categories%20eq%20" + c1.getId()).decodeJsonArray(RestApi.class);
        assertEquals(1, apis.length);

        apis = get("/api/restapi?filters=name%20eq%20api1%20and%20categories%20eq%20" + c1.getId()).decodeJsonArray(RestApi.class);
        assertEquals(1, apis.length);

        apis = get("/api/restapi?filters=name%20eq%20api1%20or%20categories%20eq%20" + c2.getId()).decodeJsonArray(RestApi.class);
        assertEquals(2, apis.length);
    }

    @Test
    public void testCreateAndDeleteCascade() {
        //create api with categories.
        Map<String, Object> data = New.hashMap("name", "test",
                                                "title", "test",
                                                "categories", new String[]{c1.getId()}
                                               );

        String apiId = (String) postJson("/api/restapi", data).decodeJsonMap().get("id");
        assertNotNull(apiId);

        //create root path.
        data = New.hashMap("apiId", apiId, "fullPath", "/");
        String rootPathId = (String)postJson("/api/restapi/" + apiId + "/paths", data).decodeJsonMap().get("id");
        assertNotNull(rootPathId);

        //create child path.
        data = New.hashMap("apiId", apiId, "fullPath", "/t", "parentId", rootPathId);
        postJson("/api/restapi/" + apiId + "/paths", data).assertSuccess().assertJsonBody();

        //create operation.
        data = New.hashMap("apiId", apiId, "httpMethod", "GET", "name", "hello");
        postJson("/api/path/" + rootPathId + "/operations", data).assertSuccess().assertJsonBody();

        delete("/api/restapi/" + apiId + "?cascade_delete=1").assertSuccess();
    }

    @Test
    public void testCreateAndUpdateRelationalProperty() {
        //create api with categories.
        Map<String, Object> data = New.hashMap("name", "test",
                                                "title", "test",
                                                "categories", new String[]{c1.getId()}
        );

        String apiId = (String) postJson("/api/restapi", data).decodeJsonMap().get("id");
        assertNotNull(apiId);

        List<RestCategory> categories = RestCategory.<RestCategory>query().whereByReference(RestApi.class, apiId).list();
        assertEquals(1, categories.size());
        assertEquals(c1.getId(), categories.get(0).getCategoryId());

        data = New.hashMap("categories", c2.getId());
        patchJson("/api/restapi/" + apiId, data).assertSuccess();
        categories = RestCategory.<RestCategory>query().whereByReference(RestApi.class, apiId).list();
        assertEquals(1, categories.size());
        assertEquals(c2.getId(), categories.get(0).getCategoryId());

        data = New.hashMap("categories", new String[]{c1.getId(), c2.getId()});
        patchJson("/api/restapi/" + apiId, data).assertSuccess();
        categories = RestCategory.<RestCategory>query().whereByReference(RestApi.class, apiId).list();
        assertEquals(2, categories.size());

        RestApi.cascadeDelete(apiId);
    }

    @BeforeClass
    public static void initData() {
        RestCategory.deleteAll();
        RestOperation.deleteAll();
        RestPath.query().update(New.hashMap("parentId", null));
        RestPath.deleteAll();
        RestModel.deleteAll();
        RestApi.deleteAll();
        Category.deleteAll();

        c1 = new Category();
        c1.setTitle("Cate1");
        c1.create();

        c2 = new Category();
        c2.setTitle("Cate2");
        c2.create();

        api1 = new RestApi();
        api1.setName("api1");
        api1.setTitle("Api1");
        api1.create();

        RestCategory rc1 = new RestCategory();
        rc1.setApiId(api1.getId());
        rc1.setCategoryId(c1.getId());
        rc1.create();

        RestPath path = new RestPath();
        path.setFullPath("/");
        path.setApiId(api1.getId());
        path.create();

        RestPath subPath = new RestPath();
        subPath.setApiId(api1.getId());
        subPath.setParentId(path.getId());
        subPath.setFullPath("/t");
        subPath.create();

        RestOperation op = new RestOperation();
        op.setApiId(api1.getId());
        op.setPathId(path.getId());
        op.setTitle("Get");
        op.setHttpMethod(HTTP.Method.GET);
        op.create();

        api2 = new RestApi();
        api2.setName("api2");
        api2.setTitle("Api2");
        api2.create();

        RestCategory rc2 = new RestCategory();
        rc1.setApiId(api2.getId());
        rc1.setCategoryId(c2.getId());
        rc1.create();
    }
}