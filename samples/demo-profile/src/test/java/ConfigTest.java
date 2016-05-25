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
import app.beans.DemoBeanProd;
import app.beans.DemoBeanTest;
import leap.core.AppConfig;
import leap.core.annotation.Inject;
import leap.core.junit.AppTestBase;
import leap.lang.Strings;
import org.junit.Test;

public class ConfigTest extends AppTestBase {

    protected @Inject DemoBean   demoBean;
    protected @Inject DemoBean[] demoBeans;

    private static String expectedProfile;
    static {
        expectedProfile = System.getProperty(AppConfig.SYS_PROPERTY_PROFILE);
        if(Strings.isEmpty(expectedProfile)) {
            expectedProfile = AppConfig.PROFILE_DEVELOPMENT;
        }
    }

    @Test
    public void testProfile() {
        System.out.println("Expected profile : " + expectedProfile);
        assertEquals(expectedProfile, config.getProfile());
    }

    @Test
    public void testDemoBean() {
        System.out.println("Demo Bean : " + demoBean.getClass());

        if(isDev()) {
            assertTrue(demoBean instanceof DemoBeanDev);
            assertEquals(2, demoBeans.length);
            return;
        }

        if(isTest()) {
            assertTrue(demoBean instanceof DemoBeanTest);
            assertEquals(1, demoBeans.length);
            return;
        }

        assertEquals(1, demoBeans.length);
        assertTrue(demoBean instanceof DemoBeanProd);
    }

    @Test
    public void testProperty() {
        String env = config.getProperty("env");

        if(isDev()) {
            assertEquals("local", env);
            assertNull(config.getProperty("prop"));
            return;
        }

        if(isTest()) {
            assertEquals("test", env);
            assertEquals("val", config.getProperty("prop"));
            return;
        }

        assertEquals("prod", env);
        assertEquals("val", config.getProperty("prop"));
    }

    private boolean isTest() {
        return expectedProfile.equals("test");
    }

    private boolean isProd() {
        return expectedProfile.equals("prod");
    }

    private boolean isDev() {
        return expectedProfile.equals("dev");
    }

}
