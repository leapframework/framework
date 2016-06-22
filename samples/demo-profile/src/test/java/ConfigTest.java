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

import app.beans.DemoBean;
import app.beans.DemoBeanDev;
import app.beans.DemoBeanTest;
import leap.core.AppConfig;
import leap.core.annotation.Inject;
import leap.core.junit.AppTestBase;
import org.junit.Test;

public class ConfigTest extends AppTestBase {

    protected @Inject AppConfig  config;
    protected @Inject DemoBean   demoBean;
    protected @Inject DemoBean[] demoBeans;

    @Test
    public void testDemoBean() {
        System.out.println("Demo Bean : " + demoBean.getClass());

        if(isDev()) {
            assertTrue(demoBean instanceof DemoBeanDev);
            assertEquals(2, demoBeans.length);
            return;
        }

        if(isTest1()) {
            assertTrue(demoBean instanceof DemoBeanDev);
            assertEquals(2, demoBeans.length);
            return;
        }

        if(isTest2()) {
            assertTrue(demoBean instanceof DemoBeanTest);
            assertEquals(2, demoBeans.length);
            return;
        }

        assertEquals(2, demoBeans.length);
        assertTrue(demoBean instanceof DemoBeanDev);
    }

    @Test
    public void testProperty() {
        String env = config.getProperty("env");

        if(isDev()) {
            assertEquals("local", env);
            assertEquals("val", config.getProperty("prop"));
            assertNotEmpty(config.getProperty("dev"));
            assertEmpty(config.getProperty("test1"));
            assertEmpty(config.getProperty("test2"));
            return;
        }

        if(isTest1()) {
            assertEquals("dev", env);
            assertEquals("val", config.getProperty("prop"));
            assertNotEmpty(config.getProperty("test1"));
            assertNotEmpty(config.getProperty("dev"));
            assertEmpty(config.getProperty("test2"));
            return;
        }

        if(isTest2()) {
            assertEquals("test2", env);
            assertEquals("val", config.getProperty("prop"));
            assertNotEmpty(config.getProperty("test2"));
            assertNotEmpty(config.getProperty("dev"));
            assertEmpty(config.getProperty("test1"));
            return;
        }

        assertEquals("dev", env);
        assertEquals("val", config.getProperty("prop"));
        assertNotEmpty(config.getProperty("dev"));
        assertEmpty(config.getProperty("test1"));
        assertEmpty(config.getProperty("test2"));
    }

    private boolean isTest1() {
        return config.getProfile().equals("test1");
    }

    private boolean isTest2() {
        return config.getProfile().equals("test2");
    }

    private boolean isProd() {
        return config.getProfile().equals("prod");
    }

    private boolean isDev() {
        return config.getProfile().equals("dev");
    }
}
