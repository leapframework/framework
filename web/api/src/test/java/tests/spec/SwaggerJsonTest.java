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
import leap.lang.Strings;
import leap.lang.http.HTTP;
import leap.lang.json.JSON;
import leap.lang.resource.Resources;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.ApiMetadataBuilder;
import leap.web.api.meta.model.MApiParameter;
import leap.web.api.meta.model.MApiSecurityDef;
import leap.web.api.spec.ApiSpecContext;
import leap.web.api.spec.swagger.SwaggerConstants;
import leap.web.api.spec.swagger.SwaggerJsonWriter;
import leap.web.api.spec.swagger.SwaggerSpecReader;
import leap.webunit.WebTestBase;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

public class SwaggerJsonTest extends WebTestBase {

    private @Inject SwaggerSpecReader specReader;
    private @Inject SwaggerJsonWriter specWriter;

    @Test
    public void testOpenFormat() throws Exception {

        try(Reader reader = Resources.getResource("classpath:/swagger/format.json").getInputStreamReader()) {
            ApiMetadata m = specReader.read(reader).build();

            StringBuilder out = new StringBuilder();
            specWriter.write(m, out);
            String json = out.toString();

            assertContains(json, "uuid");
            assertContains(json, "email");
        }
    }

    @Test
    public void testApiSwaggerJson() throws Exception {
        String swagger = get("/testing/swagger.json").getContent();

        ApiMetadata m = specReader.read(new StringReader(swagger)).build();

        assertTrue(m.getModels().containsKey("ListOnlyModel"));

        assertEquals("hello方法",m.getPaths().get("/hello/say_hello").getOperation(HTTP.Method.GET).getSummary());
        assertEquals("返回参数加上 Hello的字符串",m.getPaths().get("/hello/say_hello").getOperation(HTTP.Method.GET).getDescription());
        
    }

    @Test
    public void testApiDescSwagger() throws IOException {
        String swagger = get("/testing/swagger.json").getContent();
        ApiMetadata m = specReader.read(new StringReader(swagger)).build();

        assertEquals("登录账号",m.getModel("User").tryGetProperty("loginName").getDescription());

        boolean who = false;
        for(MApiParameter parameter : m.getPaths().get("/hello/say_hello").getOperation(HTTP.Method.GET).getParameters()){
            if(Strings.equals(parameter.getName(),"who")){
                who =true;
                assertEquals("人名",parameter.getDescription());
                break;
            }
        }
        assertTrue(who);
    }

    @Test
    public void testApiCommonDesc() throws IOException {
        String swagger = get("/testing/swagger.json").getContent();
        ApiMetadata m = specReader.read(new StringReader(swagger)).build();

        boolean pageSize = false;
        for(MApiParameter parameter : m.getPaths().get("/user").getOperation(HTTP.Method.GET).getParameters()){
            if(Strings.equals(parameter.getName(),"page_size")){
                pageSize = true;
                assertEquals("分页的每页大小",parameter.getDescription());
                break;
            }
        }
        assertTrue(pageSize);

        boolean id = false;
        for(MApiParameter parameter : m.getPaths().get("/user/{id}").getOperation(HTTP.Method.GET).getParameters()){
            if(Strings.equals(parameter.getName(),"id")){
                id = true;
                assertEquals("用户id",parameter.getDescription());
            }
        }
        assertTrue(id);
    }

    @Test
    public void testXSecuritySwaggerJson(){
        String swagger = get("/api/swagger.json").getContent();
        Map<String, Object> map = JSON.decodeMap(swagger);
        Map<String, Object> path = getAsMap(getAsMap(map,"paths"),"/xs/anonymous");
        Map<String, Object> op = getAsMap(path,"get");
        Map<String, Object> xs = getAsMap(op,"x-security");
        assertNull(xs);

        path = getAsMap(getAsMap(map,"paths"),"/xs/client_only");
        op = getAsMap(path,"get");
        xs = getAsMap(op,"x-security");
        Object userRequired = xs.get("userRequired");
        assertEquals(Boolean.TRUE,userRequired);
        
    }

    @Test
    public void testSecurityDef() throws IOException {
        String swagger = get("/basepackage/swagger.json").getContent();
        Map<String, Object> map = JSON.decodeMap(swagger);
        Map<String, Object> secDef = getAsMap(map,SwaggerConstants.SECURITY_DEFINITIONS);
        Map<String, Object> oauth = getAsMap(secDef,SwaggerConstants.OAUTH2);
        Object flow = oauth.get(SwaggerConstants.FLOW);
        assertEquals(SwaggerConstants.ACCESS_CODE,flow);
        ApiMetadata m = specReader.read(new StringReader(swagger)).build();
        boolean assertFlow = false;
        for(MApiSecurityDef def : m.getSecurityDefs()){
            if(def.isOAuth2()){
                assertEquals(SwaggerConstants.ACCESS_CODE,def.getFlow());
                assertFlow = true;
            }
        }
        assertTrue(assertFlow);
    }
    
    protected Map<String, Object> getAsMap(Map<String, Object> map, String key){
        return (Map<String, Object>)map.get(key);
    }
}
