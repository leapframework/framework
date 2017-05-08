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

import leap.core.annotation.Inject;
import leap.core.doc.DocResolver;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.meta.model.MApiOperation;
import leap.web.api.meta.model.MApiProperty;
import org.junit.Test;
import tests.ApiTestCase;

public class AnnotationDescTest extends ApiTestCase {

    protected @Inject DocResolver doc;

    @Test
    public void testMethodDesc() {
        ApiMetadata m = md("testing");

        MApiOperation o = m.getOperation("/hello/say_hello","GET");

        assertEquals("Say hello to someone", o.getSummary());
    }

    @Test
    public void testMethodDescInherited() {
        ApiMetadata m = md("testing");

        MApiOperation o = m.getOperation("/user/{id}", "GET");
        assertEquals("查询某个用户", o.getSummary());
    }

    @Test
    public void testMethodDescExternalDoc() {
        ApiMetadata m = md("testing");

        MApiOperation o = m.getOperation("/user", "GET");
        assertEquals("查询所有用户", o.getSummary());

        assertEquals("根据选项参数查询所有用户信息", o.getDescription());
        assertEquals("返回用户列表", o.getResponses()[0].getDescription());
    }

    @Test
    public void testMethodDescExternalDocWithFragment() {
        ApiMetadata m = md("testing");

        MApiOperation o = m.getOperation("/user/safe", "GET");
        assertEquals("查询所有用户(没有密码)", o.getSummary());
        assertEquals("根据选项参数查询所有用户信息(返回字段不包含密码)", o.getDescription());
    }

    @Test
    public void testParameterDesc()  {
        ApiMetadata m = md("testing");

        MApiOperation o = m.getOperation("/hello/say_hello", "GET");
        assertEquals("人名", o.tryGetParameter("who").getDescription());
    }

    @Test
    public void testParameterDescConventional()  {
        ApiMetadata m = md("testing");

        MApiOperation o = m.getOperation("/user/{id}", "DELETE");
        assertEquals("用户标识", o.tryGetParameter("id").getDescription());
    }

    @Test
    public void testParameterDescInherited() {
        ApiMetadata m = md("testing");

        MApiOperation o = m.getOperation("/user/{id}", "GET");

        assertEquals("用户id",o.tryGetParameter("id").getDescription());
    }

    @Test
    public void testModelDesc() {
        ApiMetadata m = md("testing");

        MApiModel user = m.getModel("User");
        assertEquals("用户信息", user.getSummary());
        assertEquals("用户基本信息", user.getDescription());

        //properties
        MApiProperty enabled = user.tryGetProperty("enabled");
        assertEquals(doc.resolveDoc("user_doc.md#enabled"), enabled.getDescription());

        MApiProperty password = user.tryGetProperty("password");
        assertEquals("用户密码", password.getDescription());
    }
}
