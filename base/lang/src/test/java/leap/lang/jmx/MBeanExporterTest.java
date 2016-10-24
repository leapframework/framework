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

package leap.lang.jmx;

import leap.junit.TestBase;
import org.junit.Test;

import javax.management.*;
import java.lang.management.ManagementFactory;

public class MBeanExporterTest extends TestBase {

    private MBeanServer   server = ManagementFactory.getPlatformMBeanServer();
    private MBeanExporter exporter;

    @Override
    protected void setUp() throws Exception {
        exporter = new MBeanExporter(server);
    }

    @Test
    public void testSimpleExport() throws Exception {
        ObjectName name = exporter.objectName("simpleTestBean");

        exporter.export(name, new Bean1());

        MBeanInfo info = server.getMBeanInfo(name);
        assertNotNull(info);
        assertEquals(Bean1.class.getName(), info.getClassName());
        assertEmpty(info.getDescription());

        assertEquals(1, info.getAttributes().length);
        assertEquals(1, info.getOperations().length);

        MBeanAttributeInfo a = info.getAttributes()[0];
        MBeanOperationInfo o = info.getOperations()[0];

        assertEquals("count", a.getName());
        assertEquals(Integer.TYPE.getName(), a.getType());
        assertTrue(a.isReadable());
        assertFalse(a.isWritable());

        exporter.unexportAll();

        try {
            server.getMBeanInfo(name);
            fail();
        }catch(InstanceNotFoundException e) {

        }
    }

    public static final class Bean1 {

        private @Managed int count;

        public int getCount() {
            return count;
        }

        @Managed
        public String name() {
            return this.getClass().getSimpleName();
        }
    }
}