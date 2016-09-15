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

public class RestApiControllerTest extends WebTestBase {

    private static Category c1  = null;
    private static Category c2  = null;
    private static RestApi  api = null;

//    @Test
//    public void testQueryOne() {
//        RestApi result = get("/api/restapi/" + api.getId()).decodeJson(RestApi.class);
//        assertEquals(result.getId(), api.getId());
//        assertEquals(result.getName(), api.getName());
//    }

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

        api = new RestApi();
        api.setName("api1");
        api.setTitle("Api1");
        api.create();

        RestPath path = new RestPath();
        path.setFullPath("/");
        path.setApiId(api.getId());
        path.create();

        RestPath subPath = new RestPath();
        subPath.setApiId(api.getId());
        subPath.setParentId(path.getId());
        subPath.setFullPath("/t");
        subPath.create();

        RestOperation op = new RestOperation();
        op.setApiId(api.getId());
        op.setPathId(path.getId());
        op.setTitle("Get");
        op.setHttpMethod(HTTP.Method.GET);
        op.create();
    }
}