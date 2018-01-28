/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.lang;

import java.util.Map;

import leap.junit.TestBase;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;

import org.junit.Test;

public class BeansTest extends TestBase {

	@Test
	public void testBeanPropertyWithoutField() {
		BeanType bt = BeanType.of(TestedBean.class);
		
		BeanProperty ok = bt.getProperty("ok");
		assertEquals(Boolean.TRUE,ok.getValue(new TestedBean()));
		
		BeanProperty intValue = bt.getProperty("intValue");
		assertEquals(new Integer(100), intValue.getValue(new TestedBean()));
	}

	@Test
	public void testSetBeanPropertyWithArray(){
		BeanType bt = BeanType.of(TestedBean.class);
		TestedBean tb = new TestedBean();
		Map<String, Object> properties = New.hashMap();
		String[] strArray = new String[3];
		strArray[0] = "str0";
		strArray[1] = "str1";
		strArray[2] = "str2";
		Integer[] intArray = new Integer[3];
		intArray[0] = 0;
		intArray[1] = 1;
		intArray[2] = 2;
		properties.put("strArray[]", strArray);
		properties.put("intArray[]", intArray);
		
		properties.put("strArray1[0]", strArray[2]);
		properties.put("strArray1[1]", strArray[1]);
		properties.put("strArray1[2]", strArray[0]);
		
		properties.put("intArray1[0]", intArray[2]);
		properties.put("intArray1[1]", intArray[1]);
		properties.put("intArray1[2]", intArray[0]);
		
		Beans.setPropertiesNestable(bt,tb,properties);
		
		assertEquals(strArray[0], tb.getStrArray()[0]);
		assertEquals(strArray[1], tb.getStrArray()[1]);
		assertEquals(strArray[2], tb.getStrArray()[2]);
		
		assertEquals(intArray[0], tb.getIntArray()[0]);
		assertEquals(intArray[1], tb.getIntArray()[1]);
		assertEquals(intArray[2], tb.getIntArray()[2]);
		
		assertEquals(strArray[0], tb.getStrArray1()[2]);
		assertEquals(strArray[1], tb.getStrArray1()[1]);
		assertEquals(strArray[2], tb.getStrArray1()[0]);
		
		assertEquals(intArray[0], tb.getIntArray1()[2]);
		assertEquals(intArray[1], tb.getIntArray1()[1]);
		assertEquals(intArray[2], tb.getIntArray1()[0]);
	}

    @Test
    public void tetBeanPropertyOrder() {
        BeanType bt = BeanType.of(Bean2.class);

        assertEquals(6, bt.getProperties().length);
        assertEquals("p01", bt.getProperties()[0].getName());
        assertEquals("p02", bt.getProperties()[1].getName());
        assertEquals("p11", bt.getProperties()[2].getName());
        assertEquals("p12", bt.getProperties()[3].getName());
        assertEquals("p21", bt.getProperties()[4].getName());
        assertEquals("p22", bt.getProperties()[5].getName());

        assertEquals(Bean0.class, bt.getProperties()[0].getDeclaringClass());
        assertEquals(Bean0.class, bt.getProperties()[1].getDeclaringClass());
        assertEquals(Bean1.class, bt.getProperties()[2].getDeclaringClass());
        assertEquals(Bean1.class, bt.getProperties()[3].getDeclaringClass());
        assertEquals(Bean2.class, bt.getProperties()[4].getDeclaringClass());
        assertEquals(Bean2.class, bt.getProperties()[5].getDeclaringClass());

        bt = BeanType.of(Bean3.class);
        assertEquals("p2", bt.getProperties()[0].getName());
        assertEquals("p1", bt.getProperties()[1].getName());
    }

    protected static class Bean0 {
        public String p01;
        public String p02;
    }

    protected static class Bean1 extends Bean0 {
        public String p11;
        public String p12;
    }

    protected static final class Bean2 extends Bean1 {
        public String p21;
        public String p22;
    }

    protected static final class Bean3 {
        private String p2;
        private String p1;

        public String getP2() {
            return p2;
        }

        public void setP2(String p2) {
            this.p2 = p2;
        }

        public String getP1() {
            return p1;
        }

        public void setP1(String p1) {
            this.p1 = p1;
        }
    }
	
	protected static final class TestedBean {
		
		private String[] 	strArray;
		private Integer[] 	intArray;
		private String[] 	strArray1;
		private Integer[]	intArray1;
		
		public boolean isOk() {
			return true;
		}
		
		public int getIntValue() {
			return 100;
		}

		public String[] getStrArray() {
			return strArray;
		}

		public void setStrArray(String[] strArray) {
			this.strArray = strArray;
		}

		public Integer[] getIntArray() {
			return intArray;
		}

		public void setIntArray(Integer[] intArray) {
			this.intArray = intArray;
		}

		public String[] getStrArray1() {
			return strArray1;
		}

		public void setStrArray1(String[] strArray1) {
			this.strArray1 = strArray1;
		}

		public Integer[] getIntArray1() {
			return intArray1;
		}

		public void setIntArray1(Integer[] intArray1) {
			this.intArray1 = intArray1;
		}
		
	}
	
}
