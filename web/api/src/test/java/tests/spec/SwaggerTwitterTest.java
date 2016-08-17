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
import leap.core.junit.AppTestBase;
import leap.lang.resource.Resources;
import leap.web.api.meta.ApiMetadataBuilder;
import leap.web.api.spec.swagger.SwaggerSpecReader;
import org.junit.Test;

import java.io.Reader;

public class SwaggerTwitterTest extends AppTestBase {

    private @Inject SwaggerSpecReader swaggerReader;

    @Test
    public void testReadTwitterSpec() throws Exception {

        try(Reader reader = Resources.getResource("classpath:/swagger/twitter.yaml").getInputStreamReader()) {
            ApiMetadataBuilder m = swaggerReader.read(reader);

            m.build();
        }

    }
}
