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
import leap.lang.Try;
import leap.lang.meta.MComplexTypeRef;
import leap.lang.meta.MType;
import leap.lang.resource.Resources;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.ApiMetadataBuilder;
import leap.web.api.meta.model.*;
import leap.web.api.spec.ApiSpecContext;
import leap.web.api.spec.swagger.SwaggerJsonWriter;
import leap.web.api.spec.swagger.SwaggerSpecReader;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;

public class SwaggerPetStoreTest extends AppTestBase {

    private @Inject SwaggerSpecReader swaggerReader;
    private @Inject SwaggerJsonWriter swaggerwriter;

    @Test
    public void testReadPetStoreSpec() throws Exception {

        try(Reader reader = Resources.getResource("classpath:/swagger/petstore.json").getInputStreamReader()) {
            ApiMetadataBuilder m = swaggerReader.read(reader);

            assertSpec(m);

            assertReadAgain(m.build());
        }

    }

    private void assertSpec(ApiMetadataBuilder m) {
        assertEquals(12, m.getPaths().size());
        assertEquals(5,  m.getModels().size());

        assertPetStoreBase(m);
        assertPetStorePaths(m);
        assertPetStoreModels(m);

        m.build();
    }

    private void assertReadAgain(ApiMetadata m) throws Exception {

        StringBuilder json = new StringBuilder();

        Try.throwUnchecked(() -> {
            swaggerwriter.write(ApiSpecContext.EMPTY, m, json);
        });

        assertSpec(swaggerReader.read(new StringReader(json.toString())));
    }

    private void assertPetStoreBase(ApiMetadataBuilder m) {
        assertEquals("petstore.swagger.io", m.getHost());
        assertEquals("/v2", m.getBasePath());
        assertEquals("1.0.0", m.getVersion());
        assertTrue(m.getDescription().startsWith("This is a sample server Petstore server"));
        assertEquals("Swagger Petstore", m.getTitle());
        assertEquals("http://helloreverb.com/terms/", m.getTermsOfService());
        assertEquals(1, m.getProtocols().size());
        assertEquals("http", m.getProtocols().iterator().next());
    }

    private void assertPetStorePaths(ApiMetadataBuilder m) {
        MApiPathBuilder p = m.getPath("/pets");
        assertNotNull(p);
        assertEquals(2, p.getOperations().size());

        MApiOperationBuilder post = p.getOperation("post");
        assertNotNull(post);
        assertAddPet(post);
    }

    private void assertAddPet(MApiOperationBuilder op) {
        assertEquals("addPet", op.getName());
        assertEquals("Add a new pet to the store", op.getSummary());
        assertEquals(1, op.getParameters().size());

        MApiParameterBuilder bodyParam = op.getParameter("body");
        assertNotNull(bodyParam);
        assertEquals(MApiParameter.Location.BODY, bodyParam.getLocation());
        assertEquals("Pet object that needs to be added to the store", bodyParam.getDescription());

        MType bodyParamType = bodyParam.getType();
        assertTrue(bodyParamType instanceof MComplexTypeRef);
        String bodyParamModel = ((MComplexTypeRef)bodyParamType).getRefTypeName();
        assertEquals("Pet", bodyParamModel);

        assertEquals(1, op.getResponses().size());
        MApiResponseBuilder r = op.getResponse("405");
        assertNotNull(r);
        assertEquals(405, r.getStatus());
    }

    private void assertPetStoreModels(ApiMetadataBuilder m) {

    }


}
