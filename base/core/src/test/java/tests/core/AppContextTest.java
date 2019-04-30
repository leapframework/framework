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
package tests.core;

import leap.core.AppContext;
import leap.core.AppHome;
import leap.core.i18n.MessageSource;
import leap.core.junit.AppTestBase;
import leap.lang.Locales;
import leap.lang.Strings;
import org.junit.Test;

import java.util.Locale;

public class AppContextTest extends AppTestBase {

    @Test
    public void testServletEnvironment() {
        assertFalse(AppContext.current().isServletEnvironment());

        try {
            AppContext.current().getServletContext();
            fail("Should throw IllegalStateException");
        } catch (IllegalStateException e) {

        }
    }

    @Test
    public void testAppHome() {
        AppHome home = context.getHome();

        assertNotNull(home);
        assertNotNull(home.dir());
        assertTrue(home.dir().getAbsolutePath().startsWith(System.getProperty("user.dir")));
    }

    @Test
    public void testMessageSource() {
        MessageSource ms = AppContext.current().getMessageSource();

        String message = ms.getMessage("test.errors.notNull");
        assertNotEmpty(message);

        message = ms.getMessage(Locales.forName("zh_CN"), "test.errors.notNull");
        System.out.println("-------------------message:" + message);
        assertTrue(Strings.contains(message, "不能"));

        message = ms.getMessage(Locales.forName("zh_TW"), "test.errors.notNull");
        assertTrue(Strings.contains(message, "不能"));

        message = ms.getMessage(Locales.forName("en"), "test.errors.notNull");
        assertTrue(Strings.contains(message, "cannot"));

        message = ms.getMessage("test.prop.message");
        assertTrue(Strings.contains(message, "prop"));

        message = ms.getMessage(Locales.forName("en"), "test.prop.message1");
        assertTrue(Strings.contains(message, "prop1"));

        message = ms.getMessage(Locales.forName("zh_CN"), "test.prop.message1");
        assertTrue(Strings.contains(message, "属性"));
    }

    @Test
    public void testMessageSourceDefault() {
        MessageSource ms = AppContext.current().getMessageSource();

        assertEquals("a", ms.getMessage(Locale.FRANCE,  "defaults.key1"));
        assertEquals("b", ms.getMessage(Locale.CHINA,   "defaults.key1"));
        assertEquals("c", ms.getMessage(Locale.ENGLISH, "defaults.key1"));

        assertEquals("a", ms.getMessage(Locale.FRANCE,  "defaults.key2"));
        assertEquals("a", ms.getMessage(Locale.CHINA,   "defaults.key2"));
        assertEquals("a", ms.getMessage(Locale.ENGLISH, "defaults.key2"));

        assertEquals("b", ms.getMessage(Locale.FRANCE,  "defaults.key3"));
        assertEquals("b", ms.getMessage(Locale.CHINA,   "defaults.key3"));
        assertEquals("b", ms.getMessage(Locale.ENGLISH, "defaults.key3"));
    }

    @Test
    public void testMessageSourceWithExpr() {
        MessageSource ms = AppContext.current().getMessageSource();

        String message = ms.getMessage("test.expr.simple");
        assertEquals("The name: hello", message);
    }
}
