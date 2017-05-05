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

package tests.doc;

import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.meta.model.MApiOperation;
import org.junit.Test;
import tests.ApiTestCase;

public class ConfigDescTest extends ApiTestCase {

    @Test
    public void testModelPropertyDesc() throws Exception {
        ApiMetadata m = md("testing");

        MApiModel model = m.getModel("User");

        assertEquals("登录账号", model.tryGetProperty("loginName").getDescription());
    }

    @Test
    public void testWrappedParameterDesc() throws Exception {
        ApiMetadata m = md("testing");

        MApiOperation o = m.getOperation("/user", "GET");

        assertEquals("每页显示的记录数", o.tryGetParameter("page_size").getDescription());
    }
}
