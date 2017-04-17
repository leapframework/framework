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

package tests.model;

import leap.lang.http.HTTP;
import leap.lang.meta.MType;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.meta.model.MApiOperation;
import leap.web.api.meta.model.MApiPath;
import leap.web.api.meta.model.MApiResponse;
import org.junit.Test;
import tests.ApiTestCase;

public class ApiModelTest extends ApiTestCase {

    @Test
    public void testConfiguredModel() {
        //The configured model is a class not referenced by any api controllers.
        ApiMetadata md = md("testing");

        MApiModel model = md.getModel("TConfiguredModel");
        assertNotNull(model);
    }

    @Test
    public void testGenericTypeLost() {
        ApiMetadata md = md("testing");

        MApiPath      path = md.getPath("/user_crud");
        MApiOperation op   = path.getOperation(HTTP.Method.GET);
        MApiResponse resp  = op.getResponses()[0];

        MType type = resp.getType();

        assertEquals("User", type.asTypeRef().getRefTypeName());
    }
}
