/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tests;

import app.beans.LeapBean;
import app.beans.ListType;
import app.beans.SpringBean;
import leap.core.AppConfig;
import leap.core.el.ExpressionLanguage;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.util.List;

public class SimpleSpringTest extends AbstractTest {

    @Autowired
    protected ExpressionLanguage el;

    @Autowired
    protected SpringBean springBean;

    @Autowired
    protected LeapBean leapBean;

    @Autowired
    protected AppConfig config;

    @Autowired
    protected Environment env;

    @Test
    public void testStartup() {
        //do nothing.
    }

    @Test
    public void testVariable() {
        Object hello = el.createExpression("env.hello").getValue();
        assertEquals("hello", hello);
    }

    @Test
    public void testAutowire(){
        assertNotNull(springBean.getAutowireFactory());
        assertNotNull(leapBean.getAutowireFactory());
        assertSame(springBean.getAutowireFactory(), leapBean.getAutowireFactory());
    }

    @Test
    public void testCyclicAutowired() {
        assertNotNull(springBean);
        assertNotNull(springBean.getLeapBean());
        assertNotNull(leapBean);
        assertNotNull(leapBean.getSpringBean());
        assertSame(springBean.getLeapBean(), leapBean);
        assertSame(leapBean.getSpringBean(), springBean);
    }

    @Test
    public void testLeapInjectList() {
        List<ListType> beans = leapBean.getBeans();
        assertEquals(2, beans.size());
    }

    @Test
    public void testInitialProfile() {
        assertEquals(config.getProfile(), env.getActiveProfiles()[0]);
    }
}
