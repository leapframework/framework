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
package tests;

import leap.core.annotation.Inject;
import leap.web.api.Apis;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiOperation;
import leap.web.api.meta.model.MApiPath;
import leap.web.api.spec.swagger.SwaggerSpecReader;
import leap.webunit.WebTestBase;
import leap.webunit.WebTestBaseContextual;
import org.junit.Test;

public class ApiMetadataTest extends WebTestBase {

    private @Inject SwaggerSpecReader swaggerReader;

	@Test
    public void testSwaggerSpec() {
        ApiMetadata m =
                swaggerReader.read(get("/api/swagger.json").getContent()).build();

        assertPaths(m);

        assertContextualArgument(m);
    }

    private void assertPaths(ApiMetadata m) {
        MApiPath path = m.getPaths().get("/rest_test/set_string");
        assertNotNull(path);
    }

    private void assertContextualArgument(ApiMetadata m) {
        MApiPath path = m.getPaths().get("/args/context_argument_only");
        MApiOperation op = path.getOperations()[0];
        assertEquals(0, op.getParameters().length);
    }

}
