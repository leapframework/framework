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
package tests.core.config;

import leap.core.junit.AppTestBase;
import org.junit.Test;

public class LoaderTest extends AppTestBase {

    @Test
    public void testSimpleLoaderProperties() {
        assertEquals("value1",config.getProperty("testConfigLoader.prop1"));
        assertNull(config.getProperty("testConfigLoader.prop2"));
        assertEquals("db.val1", config.getProperty("testConfigLoader.prop3"));
    }

    @Test
    public void testDbLoaderProperties() {
        assertEquals("db.val1",config.getProperty("db.key1"));
    }

    @Test
    public void testPlaceholder() {
        assertEquals("db.val1", config.getProperty("testLoader.placeholder1"));
    }

    @Test
    public void testLoaderSortOrder() {
        assertEquals("10.1", config.getProperty("testConfigLoader.sortOrderTestProp"));
    }
}