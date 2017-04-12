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

package leap.lang.json;

import junit.framework.TestCase;
import org.junit.Test;

public class JsonSubTypeTest extends TestCase {

    @Test
    public void testClassNameWithDefaultProperty() {
        TestBean encodeBean = new TestBean();
        SubClassNameDefault o1 = new SubClassNameDefault();

        encodeBean.classNameDefault = o1;
        o1.p1 = "1";
        o1.p2 = "2";

        String json = JSON.encode(encodeBean);
        assertTrue(json.contains("\"@class\":\"" + SubClassNameDefault.class.getName() + "\""));

        TestBean decodeBean = JSON.decode(json, TestBean.class);
        assertNotNull(decodeBean.classNameDefault);
        assertEquals(o1.p1, decodeBean.classNameDefault.p1);
        assertEquals(o1.p2, ((SubClassNameDefault)decodeBean.classNameDefault).p2);
    }

    @Test
    public void testTypeNameWithDefaultProperty() {
        TestBean encodeBean = new TestBean();
        SubTypeNameDefault o1 = new SubTypeNameDefault();

        encodeBean.typeNameDefault = o1;
        o1.p1 = "1";
        o1.p2 = "2";

        String json = JSON.encode(encodeBean);
        assertTrue(json.contains("\"@type\":\"type1\""));

        TestBean decodeBean = JSON.decode(json, TestBean.class);
        assertNotNull(decodeBean.typeNameDefault);
        assertEquals(o1.p1, decodeBean.typeNameDefault.p1);
        assertEquals(o1.p2, ((SubTypeNameDefault)decodeBean.typeNameDefault).p2);
    }

    @Test
    public void testTypeNameNotDefined() {
        TestBean encodeBean = new TestBean();
        SubTypeNameDefault2 o1 = new SubTypeNameDefault2();

        encodeBean.typeNameDefault = o1;
        o1.p1 = "1";
        o1.p2 = "2";

        try {
            JSON.encode(encodeBean);
            fail();
        }catch (JsonException e) {

        }
    }

    @Test
    public void testClassNameWithCustomProperty() {
        TestBean encodeBean = new TestBean();
        SubClassNameCustom o1 = new SubClassNameCustom();

        encodeBean.classNameCustom = o1;
        o1.p1 = "1";
        o1.p2 = "2";

        String json = JSON.encode(encodeBean);
        assertTrue(json.contains("\"__class\":\"" + SubClassNameCustom.class.getName() + "\""));

        TestBean decodeBean = JSON.decode(json, TestBean.class);
        assertNotNull(decodeBean.classNameCustom);
        assertEquals(o1.p1, decodeBean.classNameCustom.p1);
        assertEquals(o1.p2, ((SubClassNameCustom)decodeBean.classNameCustom).p2);
    }

    @Test
    public void testTypeNameWithCustomProperty() {
        TestBean encodeBean = new TestBean();
        SubTypeNameCustom o1 = new SubTypeNameCustom();

        encodeBean.typeNameCustom = o1;
        o1.p1 = "1";
        o1.p2 = "2";

        String json = JSON.encode(encodeBean);
        assertTrue(json.contains("\"type\":\"type1\""));

        TestBean decodeBean = JSON.decode(json, TestBean.class);
        assertNotNull(decodeBean.typeNameCustom);
        assertEquals(o1.p1, decodeBean.typeNameCustom.p1);
        assertEquals(o1.p2, ((SubTypeNameCustom)decodeBean.typeNameCustom).p2);
    }

    static final class TestBean {

        public SuperClassNameDefault classNameDefault;
        public SuperTypeNameDefault  typeNameDefault;

        public SuperClassNameCustom classNameCustom;
        public SuperTypeNameCustom  typeNameCustom;

    }


    //=================default property name==============================

    @JsonType(meta = JsonType.MetaType.CLASS_NAME)
    static abstract class SuperClassNameDefault {
        public String p1;
    }
    static final class SubClassNameDefault extends SuperClassNameDefault {
        public String p2;
    }



    @JsonType(meta = JsonType.MetaType.TYPE_NAME, types = {
        @JsonType.SubType(type=SubTypeNameDefault.class, name="type1")
    })
    static abstract class SuperTypeNameDefault {
        public String p1;
    }
    static final class SubTypeNameDefault extends SuperTypeNameDefault {
        public String p2;
    }
    static final class SubTypeNameDefault2 extends SuperTypeNameDefault {
        public String p2;
    }

    //=================custom property name==============================

    @JsonType(meta = JsonType.MetaType.CLASS_NAME, property = "__class")
    static abstract class SuperClassNameCustom {
        public String p1;
    }
    static final class SubClassNameCustom extends SuperClassNameCustom {
        public String p2;
    }


    @JsonType(meta = JsonType.MetaType.TYPE_NAME, property = "type", types = {
            @JsonType.SubType(type=SubTypeNameCustom.class, name="type1")
    })
    static abstract class SuperTypeNameCustom {
        public String p1;
    }
    static final class SubTypeNameCustom extends SuperTypeNameCustom {
        public String p2;
    }

}