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
        SubType1 o1 = new SubType1();

        encodeBean.type1 = o1;
        o1.p1 = "1";
        o1.p2 = "2";

        String json = JSON.encode(encodeBean);
        assertTrue(json.contains("\"@class\":\"" + SubType1.class.getName() + "\""));

        TestBean decodeBean = JSON.decode(json, TestBean.class);
        assertNotNull(decodeBean.type1);
        assertEquals(o1.p1, decodeBean.type1.p1);
        assertEquals(o1.p2, ((SubType1)decodeBean.type1).p2);
    }

    public void testTypeInfoUseTypeName() {

    }

    static final class TestBean {

        public SuperType1 type1;
        public SuperType2 type2;

    }

    @JsonType
    static abstract class SuperType1 {

        public String p1;

    }

    static final class SubType1 extends SuperType1 {

        public String p2;
    }

    static final class SubType2 extends SuperType1 {

        public String p3;

    }

    @JsonType(meta = JsonType.MetaType.TYPE_NAME, types = {
        @JsonType.SubType(type=SubType3.class, name="type1"),
        @JsonType.SubType(type=SubType4.class, name="type2")
    })
    static abstract class SuperType2 {

        public String p1;

    }

    static final class SubType3 extends SuperType2 {

        public String p2;
    }

    static final class SubType4 extends SuperType2 {

        public String p3;

    }

}