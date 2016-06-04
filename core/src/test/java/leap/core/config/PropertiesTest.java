/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.core.config;

import leap.core.AppConfig;
import leap.core.AppContext;
import leap.core.junit.AppTestBase;
import org.junit.Test;

public class PropertiesTest extends AppTestBase {

    @Test
    public void testProperties() {
        assertEquals("sys.val1", config.getProperty("sys.prop1"));
        assertEquals("imp.val1", config.getProperty("imp.prop1"));
        assertEquals("test.val1",config.getProperty("test.prop1"));
        assertEquals("test.val2",config.getProperty("test.prop2"));
        assertEquals("test.val3",config.getProperty("test.prop3"));
    }

    @Test
    public void testPropertiesPrefix() {
        AppConfig config = AppContext.config();
        assertEquals("test1.val1",config.getProperty("test1.prop1"));
        assertEquals("test2.val1",config.getProperty("test2_prop1"));
    }

    @Test
    public void testPropertiesFile() {
        assertEquals("a", config.getProperty("props.prop"));
        assertEquals("b", config.getProperty("props.prop1"));
        assertEquals("c", config.getProperty("props.prop2"));
    }

    @Test
    public void testLoadOrderAndOverride() {
        assertEquals("prop1_override", config.getProperty("testOrder.prop1"));
        assertEquals("prop2_override", config.getProperty("testOrder.prop2"));
        assertEquals("prop3_override", config.getProperty("testOrder.prop3"));
    }

    @Test
    public void testKeyValuePropertiesFromXml() {
        assertEquals("1", config.getProperty("testKeyValue1"));
        assertEquals("2", config.getProperty("testKeyValue2"));
        assertEquals("3", config.getProperty("testKeyValue3"));
        assertEquals("",  config.getProperty("testKeyValue4"));
        assertNull(config.getProperty("testKeyValue5"));

        assertEquals("1", config.getProperty("testKeyValue.prop1"));
        assertEquals("2", config.getProperty("testKeyValue.prop2"));
    }

    @Test
    public void testPropertiesRoot() {
        assertEquals("a", config.getProperty("props_root.prop1"));
        assertEquals("b", config.getProperty("props_root.prop2"));
    }

    @Test
    public void testXmlElementProperties() {
        assertEquals("a", config.getProperty("testElementProps.prop1"));
        assertEquals("b", config.getProperty("testElementProps.prop2"));
        assertEquals("c", config.getProperty("testElementProps.prop3"));
        assertEquals("d", config.getProperty("testElementProps.prop4"));
    }
}