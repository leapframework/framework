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
import leap.lang.Enumerables;
import leap.lang.New;
import leap.lang.Threads;
import org.junit.Test;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class MBeanExporterTest extends TestBase {

    private MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    private SimpleMBeanExporter exporter;

    private ComplexBean complexBean;
    private ObjectName  complexBeanName;
    private MBeanInfo   complexBeanInfo;

    @Override
    protected void setUp() throws Exception {
        exporter = new SimpleMBeanExporter(server);

        complexBean = new ComplexBean();
        complexBeanName = exporter.createObjectName("complexTestBean");
        exporter.export(complexBeanName, complexBean);
        complexBeanInfo = server.getMBeanInfo(complexBeanName);
    }

    @Override
    protected void tearDown() throws Exception {
        exporter.unexport(complexBeanName);
    }

    @Test
    public void testSimpleBean() throws Exception {
        ObjectName name = exporter.createObjectName("simpleTestBean");

        exporter.export(name, new SimpleBean());

        MBeanInfo info = server.getMBeanInfo(name);
        assertNotNull(info);
        assertEquals(SimpleBean.class.getName(), info.getClassName());
        assertEmpty(info.getDescription());

        assertEquals(1, info.getAttributes().length);
        assertEquals(1, info.getOperations().length);

        MBeanAttributeInfo a = info.getAttributes()[0];
        MBeanOperationInfo o = info.getOperations()[0];

        assertEquals("count", a.getName());
        assertEquals(Integer.class.getName(), a.getType());
        assertTrue(a.isReadable());
        assertFalse(a.isWritable());

        assertEquals("name", o.getName());

        assertEquals(new Integer(1),server.getAttribute(name, "count"));
        assertEquals(SimpleBean.class.getSimpleName(), server.invoke(name, "name", new Object[0],new String[0]));

        exporter.unexportAll();

        try {
            server.getMBeanInfo(name);
            fail();
        }catch(InstanceNotFoundException e) {

        }
    }

    @Test
    public void testSimpleCollectionAttribute() throws Exception {
        Object value = server.getAttribute(complexBeanName, "stringArray");
        assertArrayEquals(complexBean.getStringArray(), Enumerables.of(value).toArray());

        value = server.getAttribute(complexBeanName, "stringList");
        assertArrayEquals(complexBean.getStringList().toArray(), Enumerables.of(value).toArray());

        value = server.getAttribute(complexBeanName, "complexData");
        CompositeData cd = (CompositeData)value;
        assertEquals(complexBean.getComplexData().getIntValue(), cd.get("intValue"));
        assertEquals(complexBean.getComplexData().getStrValue(), cd.get("strValue"));
    }

    public static final class SimpleBean {

        private @Managed int count = 1;

        public int getCount() {
            return count;
        }

        @Managed
        public String name() {
            return this.getClass().getSimpleName();
        }
    }

    public static final class ComplexData {

        private int    intValue = 100;
        private String strValue = "test";

        public int getIntValue() {
            return intValue;
        }

        public void setIntValue(int intValue) {
            this.intValue = intValue;
        }

        public String getStrValue() {
            return strValue;
        }

        public void setStrValue(String strValue) {
            this.strValue = strValue;
        }
    }

    public static final class ComplexBean {

        private int[]             intArray        = new int[]{1,2};
        private String[]          stringArray     = new String[]{"hello1"};
        private List<String>      stringList      = New.arrayList("hello2");
        private List<String[]>    stringArrayList = new ArrayList<>();
        private ComplexData       complexData     = new ComplexData();
        private List<ComplexData> complexDataList = New.arrayList(new ComplexData(), new ComplexData());

        public ComplexBean() {
            stringArrayList.add(new String[]{"hello"});
        }

        @Managed
        public int[] getIntArray() {
            return intArray;
        }

        public void setIntArray(int[] intArray) {
            this.intArray = intArray;
        }

        @Managed
        public String[] getStringArray() {
            return stringArray;
        }

        public void setStringArray(String[] stringArray) {
            this.stringArray = stringArray;
        }

        @Managed
        public List<String> getStringList() {
            return stringList;
        }

        public void setStringList(List<String> stringList) {
            this.stringList = stringList;
        }

        @Managed
        public List<String[]> getStringArrayList() {
            return stringArrayList;
        }

        public void setStringArrayList(List<String[]> stringArrayList) {
            this.stringArrayList = stringArrayList;
        }

        @Managed
        public ComplexData getComplexData() {
            return complexData;
        }

        public void setComplexData(ComplexData complexData) {
            this.complexData = complexData;
        }

        @Managed
        public List<ComplexData> getComplexDataList() {
            return complexDataList;
        }

        public void setComplexDataList(List<ComplexData> complexDataList) {
            this.complexDataList = complexDataList;
        }

        @Managed
        public void noReturnTypeOp() {

        }

        @Managed
        public int intReturnTypeOp() {
            return 100;
        }

        @Managed
        public ComplexData complexReturnTypeOp() {
            return new ComplexData();
        }
    }

//    public static void main(String[] args) {
//        SimpleMBeanExporter exporter = new SimpleMBeanExporter();
//
//        exporter.export("Hello", new ComplexBean());
//
//        Threads.sleep(1000000000L);
//    }
}