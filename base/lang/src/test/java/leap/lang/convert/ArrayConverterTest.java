/*
 * Copyright 2012 the original author or authors.
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
package leap.lang.convert;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;

import leap.junit.concurrent.ConcurrentTestCase;
import leap.lang.Out;

import org.junit.Test;

public class ArrayConverterTest extends ConcurrentTestCase {

	@Test
    public void testIntegerConvert() {

        // Expected results
        int[]     intArray     = new int[] {1111, 2222, 3333, 4444};
        String    stringA      = "1111,2222,3333,4444";
        String    stringB      = intArray[0]+ "," + intArray[1] + "," + intArray[2] + "," +intArray[3];
        String[]  strArray     = new String[] {""+intArray[0], ""+intArray[1], ""+intArray[2], ""+intArray[3]};
        long[]    longArray    = new long[] {intArray[0], intArray[1], intArray[2], intArray[3]};
        Long[]    LONGArray    = new Long[]    {new Long(intArray[0]),    new Long(intArray[1]),    new Long(intArray[2]),    new Long(intArray[3])};
        Integer[] IntegerArray = new Integer[] {new Integer(intArray[0]), new Integer(intArray[1]), new Integer(intArray[2]), new Integer(intArray[3])};
        ArrayList<String> strList = new ArrayList<String>();
        ArrayList<Long> longList = new ArrayList<Long>();
        for (int i = 0; i < strArray.length; i++) {
            strList.add(strArray[i]);
            longList.add(LONGArray[i]);
        }

        String msg = null;
        Object convertResult = null;

        // String --> int[]
        try {
            msg = "String --> int[]";
            convertResult = Converts.convert(stringA,int[].class);
            checkArray(msg, intArray, convertResult);
            checkConvertFrom(msg, convertResult, int[].class, null);
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }

        // String[] --> int[]
        try {
            msg = "String[] --> int[]";
            convertResult = Converts.convert(strArray,int[].class);
            checkArray(msg, intArray, convertResult);
            checkConvertFrom(msg, convertResult, int[].class, null);
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }

        // String[] --> Integer[]
        try {
            msg = "String[] --> Integer[]";
            checkArray(msg, IntegerArray, Converts.convert(strArray,Integer[].class));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }

        // long[] --> int[]
        try {
            msg = "long[] --> int[]";
            checkArray(msg, intArray, Converts.convert(longArray,int[].class));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }

        // LONG[] --> int[]
        try {
            msg = "LONG[] --> int[]";
            checkArray(msg, intArray, Converts.convert(LONGArray,int[].class));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }

        // LONG[] --> String (all)
        try {
            msg = "LONG[] --> String (all)";
            assertEquals(msg, stringB, Converts.convert(LONGArray,String.class));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }

        // Collection of Long --> String
        try {
            msg = "Collection of Long --> String";
            assertEquals(msg, stringB, Converts.convert(longList,String.class));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }

        // LONG[] --> String[]
        try {
            msg = "long[] --> String[]";
            checkArray(msg, strArray, Converts.convert(LONGArray,String[].class));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }

        // Collection of String --> Integer[]
        try {
            msg = "Collection of String --> Integer[]";
            checkArray(msg, IntegerArray, Converts.convert(strList,Integer[].class));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }

        // Collection of Long --> int[]
        try {
            msg = "Collection of Long --> int[]";
            checkArray(msg, intArray, Converts.convert(longList,int[].class));
        } catch (Exception e) {
            fail(msg + " failed " + e);
        }
        
        assertEquals(null, Converts.convert(null, int[].class));
    }
    
    private void checkArray(String msg, Object expected, Object result) {
        assertNotNull(msg + " Expected Null", expected);
        assertNotNull(msg + " Result   Null", result);
        assertTrue(msg + " Result   not array", result.getClass().isArray());
        assertTrue(msg + " Expected not array", expected.getClass().isArray());
        int resultLth = Array.getLength(result);
        assertEquals(msg + " Size", Array.getLength(expected), resultLth);
        assertEquals(msg + " Type", expected.getClass(), result.getClass());
        for (int i = 0; i < resultLth; i++) {
            Object expectElement = Array.get(expected, i);
            Object resultElement = Array.get(result, i);
            assertEquals(msg + " Element " + i, expectElement, resultElement);
        }
    }    
    
    private void checkConvertFrom(String msg, Object convertResult, Class<?> targetType, Type genericType) {
    	Out<Object> out = new Out<Object>();
    	try {
			assertTrue(msg + " ConvertFrom Check Error", new ArrayConverter().convertFrom(convertResult, targetType, genericType, out));
		} catch (Throwable e) {
			e.printStackTrace();
		}
    }
}
