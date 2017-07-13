/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tests;

import leap.core.annotation.Inject;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.spec.swagger.SwaggerSpecReader;
import leap.webunit.WebTestBase;
import org.junit.Test;

public class DynaApiTest extends WebTestBase {

    protected @Inject SwaggerSpecReader swaggerReader;

    protected ApiMetadata md(String api) {
        String path = "/" + api + "/swagger.json";
        return swaggerReader.read(get(path).getContent()).build();
    }

    @Test
    public void testDynaCreateApi() {
        //create api.
        get("/create_api").assertOk();

        ApiMetadata md = md("test");
        MApiModel model1 = md.getModel("Entity1");
        assertNotNull(model1);

        get("/test/entity1").getJson().asArray();

        //destroy api.
        get("/destroy_api").assertOk();
        get("/test/swagger.json").assertNotFound();
    }

}
