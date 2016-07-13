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

package tests.core.ioc;

import leap.core.AppConfig;
import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import org.junit.BeforeClass;
import org.junit.Test;
import tested.beans.TAutoInjectBean;
import tests.core.CoreTestCase;

public class BeanAutoInjectTest extends CoreTestCase {

    protected static @Inject BeanFactory factory;

    protected @Inject AppConfig       config;
    protected @Inject TAutoInjectBean autoInjectBean;

    @BeforeClass
    public static void checkStaticInjection() {
        assertNotNull(factory);
    }

    @Test
    public void testTestCaseInject() {
        assertNotNull(config);
    }

    @Test
    public void testInjectPrivateField() {
        assertNotNull(autoInjectBean.nonGetterGetPrivateInjectPrimaryBean());
        assertNull(autoInjectBean.nonGetterGetNotInjectPrimaryBean());
    }

    @Test
    public void testInjectNotWritableProperty() {
        assertNotNull(autoInjectBean.getPrimaryBean1());
    }

    @Test
    public void testInjectStaticField() {
        assertSame(config, autoInjectBean.config);
    }

}
