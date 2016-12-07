/*
 *
 *  * Copyright 2013 the original author or authors.
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
import leap.lang.Try;
import leap.lang.resource.Resources;
import leap.web.api.meta.ApiMetadataBuilder;
import leap.web.api.spec.ApiSpecContext;
import leap.web.api.spec.swagger.SwaggerJsonWriter;
import leap.web.api.spec.swagger.SwaggerSpecReader;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Created by kael on 2016/12/7.
 */
public class SwaggerSecurityTest  extends AppTestBase {
    private @Inject SwaggerSpecReader swaggerReader;
    private @Inject SwaggerJsonWriter swaggerwriter;
    @Test
    public void testReadSecuritySpec() throws Exception {

        try(Reader reader = Resources.getResource("classpath:/swagger/security.json").getInputStreamReader()) {
            ApiMetadataBuilder m = swaggerReader.read(reader);
            assertReadSpec(m);
            readAgain(m);
        }

    }

    protected void assertReadSpec(ApiMetadataBuilder m){
        assertEquals(2,m.getSecurityDefs().size());
    }

    protected void readAgain(ApiMetadataBuilder m) throws IOException {
        StringBuilder json = new StringBuilder();

        Try.throwUnchecked(() -> swaggerwriter.write(ApiSpecContext.EMPTY, m.build(), json));

        assertReadSpec(swaggerReader.read(new StringReader(json.toString())));
    }
}
