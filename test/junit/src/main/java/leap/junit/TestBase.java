/*
 * Copyright 2013 the original author or authors.
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
package leap.junit;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

public abstract class TestBase extends Assert {
	
	@Rule
	public final TestName testName = new TestName();	
	
	@Before
	public final void runBefore() throws Exception {
		this.setUp();
	}
	
	@After
	public final void runAfter() throws Exception {
		this.tearDown();
	}
	
	protected void setUp() throws Exception{
		
	}
	
	protected void tearDown() throws Exception {
		
	}
	
	protected final String getTestMethodName(){
		return testName.getMethodName();
	}
	
	public static void assertContains(String text,String contains){
		if(null == text || null == contains || !text.contains(contains)){
			throw new AssertionError("The given string must contains the text : '" + contains + "', actual is : " + text);
		}
	}
	
	public static void assertEmpty(String string){
		if(null != string && string.length() > 0){
			throw new AssertionError("The given string must be null or empty");
		}
	}
	
	public static void assertNotEmpty(String string){
		if(null == string || string.equals("")){
			throw new AssertionError("The given string must not be null or empty");
		}
	}
	
	public static void assertNotEmpty(Object[] array){
		if(null == array || array.length == 0){
			throw new AssertionError("The given array must not be null or empty");
		}
	}	
	
	public static void assertNotEmpty(Iterable<?> iterable){
		if(null == iterable || !iterable.iterator().hasNext()){
			throw new AssertionError("The given iterable must not be null or empty");
		}
	}
	
	public static void assertNotEmpty(@SuppressWarnings("rawtypes") Map map){
		if(null == map || map.isEmpty()){
			throw new AssertionError("The given map must not be null or empty");
		}
	}	
	
	public static <T> void assertFieldsEquals(T obj1,T obj2){
		if(null == obj1 && null == obj2){
			return;
		}
		if(null == obj1 || null == obj2){
			throw new AssertionError("Cannot compares the fields of null ojbects");
		}
		try {
	        Class<?> clazz = obj1.getClass();
	        while(!clazz.equals(Object.class)){
	        	for(Field field : clazz.getDeclaredFields()){
	        		field.setAccessible(true);
	        		Object val1 = field.get(obj1);
	        		Object val2 = field.get(obj2);
	        		
	        		if(!equals(val1, val2)){
	        			throw new AssertionError("The values of field '" + field.getName() + 
								 "' are not equals between the given objects. " + 
								 "value1:[" + val1 + "], value2[" + val2 + "]");
	        		}
	        	}
	        	clazz = clazz.getSuperclass();
	        }
        } catch (Exception e) {
        	throw new RuntimeException("Error comparing the fields : " + e.getMessage(),e);
        }
	}
	
	public static void assertEquals(BigDecimal n1,BigDecimal n2){
		if(null == n1 && null == n2){
			return;
		}
		if(null == n1 || null == n2){
			throw new AssertionError("Cannot compares the values of null nummbers");
		}
		if(n1.compareTo(n2) != 0){
			throw new AssertionError("Expected : <" + n1 + ">, actual : <" + n2 + ">");
		}
	}
	
	public static void assertMapEquals(Map<?,?> map1,Map<?,?> map2){
		if(null == map1 && null == map2){
			return;
		}
		if(null == map1 || null == map2){
			throw new AssertionError("Cannot comparse the values of null maps");
		}
		
		for(Entry<?,?> entry : map1.entrySet()){
			Object val1 = entry.getValue();
			Object val2 = map2.get(entry.getKey());
			
			if(!equals(val1,val2)){
    			throw new AssertionError("The values of key '" + entry.getKey() + 
						 "' are not equals between the given maps. " + 
						 "value1:[" + val1 + "], value2[" + val2 + "]");
			}
		}
	}
	
	private static boolean equals(Object a, Object b) {
		return (a == b) || (a != null && a.equals(b));
	}
}