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

import java.math.BigDecimal;
import java.math.BigInteger;

import leap.junit.concurrent.ConcurrentTestCase;

import org.junit.Test;

public class NumberConverterTest extends ConcurrentTestCase {

	@Test
    public void testSimpleIntegerConversion() throws Exception {
        String[] message= { 
            "from String",
            "from String",
            "from String",
            "from String",
            "from String",
            "from String",
            "from String",
            "from Byte",
            "from Short",
            "from Integer",
            "from Long",
            "from Float",
            "from Double"
        };
        
        Object[] input = { 
            String.valueOf(Integer.MIN_VALUE),
            "-17",
            "-1",
            "0",
            "1",
            "17",
            String.valueOf(Integer.MAX_VALUE),
            new Byte((byte)7),
            new Short((short)8),
            new Integer(9),
            new Long(10),
            new Float(11.1),
            new Double(12.2)
        };
        
        Integer[] expected = { 
            new Integer(Integer.MIN_VALUE),
            new Integer(-17),
            new Integer(-1),
            new Integer(0),
            new Integer(1),
            new Integer(17),
            new Integer(Integer.MAX_VALUE),
            new Integer(7),
            new Integer(8),
            new Integer(9),
            new Integer(10),
            new Integer(11),
            new Integer(12)
        };
        
        for(int i=0;i<expected.length;i++) {
            assertEquals(message[i] + " to Integer",expected[i],Converts.convert(input[i],Integer.class));
            assertEquals(message[i] + " to int",expected[i],Converts.convert(input[i],Integer.TYPE));
        }
    }
	
	@Test
    public void testSimpleShortConversion() throws Exception {
        String[] message= { 
            "from String",
            "from String",
            "from String",
            "from String",
            "from String",
            "from String",
            "from String",
            "from Byte",
            "from Short",
            "from Integer",
            "from Long",
            "from Float",
            "from Double"
        };
        
        Object[] input = { 
            String.valueOf(Short.MIN_VALUE),
            "-17",
            "-1",
            "0",
            "1",
            "17",
            String.valueOf(Short.MAX_VALUE),
            new Byte((byte)7),
            new Short((short)8),
            new Integer(9),
            new Long(10),
            new Float(11.1),
            new Double(12.2)
        };
        
        Short[] expected = { 
            new Short(Short.MIN_VALUE),
            new Short((short)-17),
            new Short((short)-1),
            new Short((short)0),
            new Short((short)1),
            new Short((short)17),
            new Short(Short.MAX_VALUE),
            new Short((short)7),
            new Short((short)8),
            new Short((short)9),
            new Short((short)10),
            new Short((short)11),
            new Short((short)12)
        };
        
        for(int i=0;i<expected.length;i++) {
            assertEquals(message[i] + " to Short",expected[i],Converts.convert(input[i],Short.class));
            assertEquals(message[i] + " to short",expected[i],Converts.convert(input[i],Short.TYPE));
        }
        
        assertEquals(Short.valueOf("0"), Converts.convert(0, Short.class));
    }	
	
	@Test
    public void testSimpleLongConversion() throws Exception {
        String[] message= { 
            "from String",
            "from String",
            "from String",
            "from String",
            "from String",
            "from String",
            "from String",
            "from Byte",
            "from Short",
            "from Integer",
            "from Long",
            "from Float",
            "from Double"
        };
        
        Object[] input = { 
            String.valueOf(Long.MIN_VALUE),
            "-17",
            "-1",
            "0",
            "1",
            "17",
            String.valueOf(Long.MAX_VALUE),
            new Byte((byte)7),
            new Short((short)8),
            new Integer(9),
            new Long(10),
            new Float(11.1),
            new Double(12.2)
        };
        
        Long[] expected = { 
            new Long(Long.MIN_VALUE),
            new Long(-17),
            new Long(-1),
            new Long(0),
            new Long(1),
            new Long(17),
            new Long(Long.MAX_VALUE),
            new Long(7),
            new Long(8),
            new Long(9),
            new Long(10),
            new Long(11),
            new Long(12)
        };
        
        for(int i=0;i<expected.length;i++) {
            assertEquals(message[i] + " to Long",expected[i],Converts.convert(input[i],Long.class));
            assertEquals(message[i] + " to long",expected[i],Converts.convert(input[i],Long.TYPE));
        }
    }	
	
	@Test
    public void testSimpleFloatConversion() throws Exception {
        String[] message= { 
            "from String",
            "from String",
            "from String",
            "from String",
            "from String",
            "from String",
            "from String",
            "from Byte",
            "from Short",
            "from Integer",
            "from Integer Zero",
            "from Long",
            "from Float",
            "from Double",
            "from BigDecimal"
        };
        
        Object[] input = { 
            String.valueOf(Float.MIN_VALUE),
            "-17.2",
            "-1.1",
            "0.0",
            "1.1",
            "17.2",
            String.valueOf(Float.MAX_VALUE),
            new Byte((byte)7),
            new Short((short)8),
            new Integer(9),
            new Integer(0),
            new Long(10),
            new Float(11.1),
            new Double(12.2),
            new BigDecimal("0.5")
        };
        
        Float[] expected = { 
            new Float(Float.MIN_VALUE),
            new Float(-17.2),
            new Float(-1.1),
            new Float(0.0),
            new Float(1.1),
            new Float(17.2),
            new Float(Float.MAX_VALUE),
            new Float(7),
            new Float(8),
            new Float(9),
            new Float(0.0),
            new Float(10),
            new Float(11.1),
            new Float(12.2),
            new Float("0.5")
        };
        
        for(int i=0;i<expected.length;i++) {
            assertEquals(
                message[i] + " to Float",
                expected[i].floatValue(),
                ((Float)(Converts.convert(input[i],Float.class))).floatValue(),
                0.00001);
            assertEquals(
                message[i] + " to float",
                expected[i].floatValue(),
                ((Float)(Converts.convert(input[i],Float.TYPE))).floatValue(),
                0.00001);
        }
    }	
	
	@Test
    public void testSimpleDoubleConversion() throws Exception {
        String[] message= { 
            "from String",
            "from String",
            "from String",
            "from String",
            "from String",
            "from String",
            "from String",
            "from Byte",
            "from Short",
            "from Integer",
            "from Long",
            "from Float",
            "from Double",
            "from BigDecimal"
        };
        
        Object[] input = { 
            String.valueOf(Double.MIN_VALUE),
            "-17.2",
            "-1.1",
            "0.0",
            "1.1",
            "17.2",
            String.valueOf(Double.MAX_VALUE),
            new Byte((byte)7),
            new Short((short)8),
            new Integer(9),
            new Long(10),
            new Float(11.1),
            new Double(12.2),
            new BigDecimal("0.5")
        };
        
        Double[] expected = { 
            new Double(Double.MIN_VALUE),
            new Double(-17.2),
            new Double(-1.1),
            new Double(0.0),
            new Double(1.1),
            new Double(17.2),
            new Double(Double.MAX_VALUE),
            new Double(7),
            new Double(8),
            new Double(9),
            new Double(10),
            new Double(11.1),
            new Double(12.2),
            new Double("0.5")
        };
        
        assertEquals(new Double("0.5"),Converts.convert(new BigDecimal("0.5"), Double.class));
        
        for(int i=0;i<expected.length;i++) {
            assertEquals(
                message[i] + " to Double",
                expected[i].doubleValue(),
                ((Double)(Converts.convert(input[i],Double.class))).doubleValue(),
                0.00001D);
            assertEquals(
                message[i] + " to double",
                expected[i].doubleValue(),
                ((Double)(Converts.convert(input[i],Double.TYPE))).doubleValue(),
                0.00001D);
        }
        
        assertEquals(new Double(0.0d), Converts.convert(0, Double.class));
    }	
	
	@Test
    public void testSimpleByteConversion() throws Exception {
        String[] message= { 
            "from String",
            "from String",
            "from String",
            "from String",
            "from String",
            "from String",
            "from String",
            "from Byte",
            "from Short",
            "from Integer",
            "from Long",
            "from Float",
            "from Double"
        };
        
        Object[] input = { 
            String.valueOf(Byte.MIN_VALUE),
            "-17",
            "-1",
            "0",
            "1",
            "17",
            String.valueOf(Byte.MAX_VALUE),
            new Byte((byte)7),
            new Short((short)8),
            new Integer(9),
            new Long(10),
            new Float(11.1),
            new Double(12.2)
        };
        
        Byte[] expected = { 
            new Byte(Byte.MIN_VALUE),
            new Byte((byte)-17),
            new Byte((byte)-1),
            new Byte((byte)0),
            new Byte((byte)1),
            new Byte((byte)17),
            new Byte(Byte.MAX_VALUE),
            new Byte((byte)7),
            new Byte((byte)8),
            new Byte((byte)9),
            new Byte((byte)10),
            new Byte((byte)11),
            new Byte((byte)12)
        };
        
        for(int i=0;i<expected.length;i++) {
            assertEquals(message[i] + " to Byte",expected[i],Converts.convert(input[i],Byte.class));
            assertEquals(message[i] + " to byte",expected[i],Converts.convert(input[i],Byte.TYPE));
        }
        
        assertEquals(Byte.valueOf("0"), Converts.convert(0, Byte.class));
    }	
	
	@Test
    public void testSimpleBigDecimalConversion() throws Exception {
        String[] message= { 
            "from String",
            "from String",
            "from String",
            "from String",
            "from String",
            "from Byte",
            "from Short",
            "from Integer",
            "from Long",
            "from Float",
            "from Double"
        };
        
        Object[] input = { 
            "-17.2",
            "-1.1",
            "0.0",
            "1.1",
            "17.2",
            new Byte((byte)7),
            new Short((short)8),
            new Integer(9),
            new Long(10),
            new Float("11.1"),
            new Double("12.2")
        };
        
        BigDecimal[] expected = { 
            new BigDecimal("-17.2"),
            new BigDecimal("-1.1"),
            new BigDecimal("0.0"),
            new BigDecimal("1.1"),
            new BigDecimal("17.2"),
            new BigDecimal("7"),
            new BigDecimal("8"),
            new BigDecimal("9"),
            new BigDecimal("10"),
            new BigDecimal("11.1"),
            new BigDecimal("12.2")
        };
        
        for(int i=0;i<expected.length;i++) {
            assertEquals(
                message[i] + " to BigDecimal",
                expected[i],
                Converts.convert(input[i],BigDecimal.class));
        }
        
    }
	
	@Test
    public void testSimpleBigIntegerConversion() throws Exception {
        String[] message= { 
            "from String",
            "from String",
            "from String",
            "from String",
            "from String",
            "from String",
            "from String",
            "from Byte",
            "from Short",
            "from Integer",
            "from Long",
            "from Float",
            "from Double"
        };
        
        Object[] input = { 
            String.valueOf(Long.MIN_VALUE),
            "-17",
            "-1",
            "0",
            "1",
            "17",
            String.valueOf(Long.MAX_VALUE),
            new Byte((byte)7),
            new Short((short)8),
            new Integer(9),
            new Long(10),
            new Float(11.1),
            new Double(12.2)
        };

        BigInteger[] expected = { 
            BigInteger.valueOf(Long.MIN_VALUE),
            BigInteger.valueOf(-17),
            BigInteger.valueOf(-1),
            BigInteger.valueOf(0),
            BigInteger.valueOf(1),
            BigInteger.valueOf(17),
            BigInteger.valueOf(Long.MAX_VALUE),
            BigInteger.valueOf(7),
            BigInteger.valueOf(8),
            BigInteger.valueOf(9),
            BigInteger.valueOf(10),
            BigInteger.valueOf(11),
            BigInteger.valueOf(12)
        };
        
        for(int i=0;i<expected.length;i++) {
            assertEquals(message[i] + " to BigInteger",expected[i],Converts.convert(input[i],BigInteger.class));
        }
    }

    @Test
    public void testNumberConvert() {
	    Number num = Converts.convert("0", Number.class);
	    assertTrue(num instanceof Long && num.equals(0L));

        num = Converts.convert("1", Number.class);
        assertTrue(num instanceof Long && num.equals(1L));

        num = Converts.convert("0.0", Number.class);
        assertTrue(num instanceof Double && num.equals(0.0d));
    }
}
