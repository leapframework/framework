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

package tests.log;

import app.models.api.TestLogModel;
import auth.AuthTestData;
import leap.lang.codec.Base64;
import leap.webunit.WebTestBase;
import org.junit.Test;

/**
 * Created by kael on 2016/10/11.
 */
public class OpLogControllerTest extends WebTestBase {
    @Test
    public void testSaveLog(){
        TestLogModel.deleteAll();
        forGet("/api/log/operation1").addQueryParam("name","testlog").send().assertSuccess();
        assertEquals(1,TestLogModel.count());
        assertEquals("测试操作testlog",TestLogModel.<TestLogModel>first().getDescription());
    }
    @Test
    public void testQueryLog(){
        String at = forPost("http://127.0.0.1:8080/auth/oauth2/token")
                .addQueryParam("grant_type","password")
                .addQueryParam("username", AuthTestData.USERNAME1)
                .addQueryParam("password",AuthTestData.PASSWORD1)
                .addHeader("Authorization", encodeBasicAuthcHeader("client1","client1_secret"))
                .send().getJson().asJsonObject().get("access_token");
        assertNotNull(at);
        TestLogModel.deleteAll();
        forGet("/api/log/operation1").addQueryParam("name","testlog").send().assertSuccess();

        forGet("/api/log").addHeader("Authorization","Bearer " + at).addQueryParam("name","testlog").send().assertSuccess();
        assertEquals(1,TestLogModel.count());
        assertEquals("测试操作testlog",TestLogModel.<TestLogModel>first().getDescription());
    }
    @Test
    public void testControllerOpLog(){
        TestLogModel.deleteAll();
        forGet("/api/log/operation2").addQueryParam("name","testlog").send().assertSuccess();
        assertEquals(1,TestLogModel.count());
        assertEquals("testtestlog",TestLogModel.<TestLogModel>first().getDescription());
    }

    protected String encodeBasicAuthcHeader(String clientId, String clientSecret){
        return "Basic " + Base64.encode(clientId+":"+clientSecret);
    }
    
}
