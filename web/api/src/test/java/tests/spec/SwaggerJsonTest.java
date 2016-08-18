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

package tests.spec;

import leap.core.annotation.Inject;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.spec.swagger.SwaggerSpecReader;
import leap.webunit.WebTestBase;
import org.junit.Test;

import java.io.StringReader;

public class SwaggerJsonTest extends WebTestBase {

    private @Inject SwaggerSpecReader specReader;

    @Test
    public void testApiSwaggerJson() throws Exception {
        String swagger = get("/api/swagger.json").getContent();

        ApiMetadata m = specReader.read(new StringReader(swagger)).build();

        assertTrue(m.getModels().containsKey("ListOnlyModel"));
    }

}
