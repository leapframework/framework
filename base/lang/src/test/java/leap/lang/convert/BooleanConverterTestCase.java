/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.junit.Test;

public class BooleanConverterTestCase extends ConverterTestBase {

	public static final String[] STANDARD_TRUES = new String[] { "yes", "y", "true", "on", "1" };

	public static final String[] STANDARD_FALSES = new String[] { "no", "n", "false", "off", "0" };
	
	@Override
    protected Converter<?> getConverter() {
	    return new BooleanConverter();
    }
	
	@Test
	public void testStandardValues() {
		testConversionValues(STANDARD_TRUES, STANDARD_FALSES);
	}

	@Test
	public void testCaseInsensitivity() {
		testConversionValues(new String[] { "Yes", "TRUE" }, new String[] { "NO", "fAlSe" });
	}

	@Test
	public void testInvalidString() {
		try {
			Converts.convert("bogus", Boolean.class);
			fail("Converting invalid string should have generated an exception");
		} catch (ConvertException expected) {
			// Exception is successful test
		}
	}

	@Test
	public void testAdditionalStrings() {
		String[] trueStrings = { "sure" };
		String[] falseStrings = { "nope" };
		BooleanConverter converter = new BooleanConverter(trueStrings, falseStrings);
		testConversionValues(converter, new String[] { "sure", "Sure" }, new String[] { "nope", "nOpE" });

		try {
			convertFrom(converter,Boolean.class, "true");
			fail("Converting obsolete true value should have generated an exception");
		} catch (ConvertException expected) {
			// Exception is successful test
		}
		try {
			convertFrom(converter,Boolean.class, "bogus");
			fail("Converting invalid string should have generated an exception");
		} catch (ConvertException expected) {
			// Exception is successful test
		}
	}

	protected void testConversionValues(String[] trueValues, String[] falseValues) {

		for (int i = 0; i < trueValues.length; i++) {
			assertEquals(Boolean.TRUE, Converts.convert(trueValues[i], Boolean.class));
		}
		for (int i = 0; i < falseValues.length; i++) {
			assertEquals(Boolean.FALSE, Converts.convert(falseValues[i], Boolean.class));
		}
	}
	
	protected void testConversionValues(BooleanConverter converter, String[] trueValues, String[] falseValues) {

		for (int i = 0; i < trueValues.length; i++) {
			assertEquals(Boolean.TRUE, convertFrom(converter,trueValues[i], Boolean.class));
		}
		
		for (int i = 0; i < falseValues.length; i++) {
			assertEquals(Boolean.FALSE, convertFrom(converter, falseValues[i], Boolean.class));
		}
	}	

}
