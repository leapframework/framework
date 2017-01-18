/*
 *
 *  * Copyright 2016 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package tests.core.ioc;

import leap.core.annotation.Inject;
import leap.core.junit.AppTestBase;
import leap.lang.jmx.MBeanExporter;
import org.junit.Test;
import tested.beans.jmx.TJmxBean1;

import javax.management.MBeanInfo;
import javax.management.ObjectName;

public class BeanJmxTest extends AppTestBase {

    protected @Inject MBeanExporter exporter;

    @Test
    public void testSimpleJmxBean() throws Exception {
        assertNotNull(exporter);

        ObjectName name = exporter.createObjectName("testJmxBean");
        MBeanInfo mbean = exporter.getServer().getMBeanInfo(name);
        assertNotNull(mbean);
    }

    @Test
    public void testSimpleJmxBean1() throws Exception {
        ObjectName name = exporter.createObjectName(TJmxBean1.class.getName() + "#testJmxBean");
        MBeanInfo mbean = exporter.getServer().getMBeanInfo(name);
        assertNotNull(mbean);
    }

}