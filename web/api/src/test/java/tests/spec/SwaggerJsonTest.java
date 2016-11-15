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
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiParameter;
import leap.web.api.spec.swagger.SwaggerSpecReader;
import leap.webunit.WebTestBase;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

public class SwaggerJsonTest extends WebTestBase {

    private @Inject SwaggerSpecReader specReader;

    @Test
    public void testApiSwaggerJson() throws Exception {
        String swagger = get("/testing/swagger.json").getContent();

        ApiMetadata m = specReader.read(new StringReader(swagger)).build();

        boolean who = false;

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
        pageSize = false;
        boolean id = false;
        for(MApiParameter parameter : m.getPaths().get("/user/{id}").getOperation(HTTP.Method.GET).getParameters()){
            if(Strings.equals(parameter.getName(),"page_size")){
                pageSize = true;
                assertEquals("单独配置的分页大小",parameter.getDescription());
            }
            if(Strings.equals(parameter.getName(),"id")){
                id = true;
                assertEquals("用户id",parameter.getDescription());
            }
        }
        assertTrue(pageSize);
        assertTrue(id);
    }
}
