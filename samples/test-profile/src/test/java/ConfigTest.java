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

import app.beans.*;
import leap.core.AppConfig;
import leap.core.AppContext;
import leap.core.AppContextInitializer;
import leap.core.BeanFactory;
import leap.junit.TestBase;
import leap.junit.contexual.Contextual;
import leap.junit.contexual.ContextualProvider;
import leap.junit.contexual.ContextualRule;
import leap.lang.Strings;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.Description;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Contextual
public class ConfigTest extends TestBase {

    protected static AppConfig   config;
    protected static BeanFactory factory;
    protected static MetaBean1   metaBean1;
    protected static MetaBean2   metaBean2;
    protected static DemoBean    demoBean;
    protected static DemoBean[]  demoBeans;
    protected static String      expectedProfile;

    private static final Set<String> profiles = new HashSet<>();
    private static final Map<String, AppContext> contexts = new HashMap<>();

    static {
        String sysProfile = System.getProperty(AppConfig.SYS_PROPERTY_PROFILE);
        if(!Strings.isEmpty(sysProfile)) {
            System.out.println("App profile : " + sysProfile);
            profiles.add(sysProfile);
        }else{
            profiles.add("dev");
            profiles.add("test");
            profiles.add("prod");
        }

        for(String profile : profiles) {
            System.setProperty(AppConfig.SYS_PROPERTY_PROFILE, profile);

            System.out.println("set profile : " + profile);
            contexts.put(profile, AppContextInitializer.newStandalone());
        }
    }

    @Rule
    public ContextualRule contextualRule = new ContextualRule(new ContextualProvider() {

        @Override
        public Iterable<String> names(Description description) {
            return profiles;
        }

        @Override
        public void beforeTest(Description description, String name) throws Exception {
            expectedProfile = name;

            AppContext context = contexts.get(name);

            config    = context.getConfig();
            factory   = context.getBeanFactory();
            metaBean1 = factory.getBean(MetaBean1.class);
            metaBean2 = factory.tryGetBean(MetaBean2.class);
            demoBean  = factory.getBean(DemoBean.class);
            demoBeans = factory.getBeans(DemoBean.class).toArray(new DemoBean[0]);
        }
    });

    @Test
    public void testProfile() {
        System.out.println("Expected profile : " + expectedProfile);
        assertEquals(expectedProfile, config.getProfile());
    }

    @Test
    public void testMetaInf() {
        assertNotNull(metaBean1);
        assertEquals("val", config.getProperty("meta.prop1"));

        if(isDev()) {
            assertNotNull(metaBean2);
            assertEquals("val", config.getProperty("meta.prop2"));
        }
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
