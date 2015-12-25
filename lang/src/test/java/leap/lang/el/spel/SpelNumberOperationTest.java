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
package leap.lang.el.spel;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class SpelNumberOperationTest extends SpelTestCase {

	@Test
	public void testAdd() {
		BigDecimal bd1 = new BigDecimal(1);
		Integer i1 = new Integer(1);
		Long l1 = new Long(1);
		Float f1 = new Float(1);
		Double d1 = new Double(1);
		String e1 = "1e0";
		String s1 = "1";
		BigInteger bi1 = new BigInteger("1");
		
		Long l2 = new Long(2);
		BigDecimal bd2 = new BigDecimal(2);
		Double d2 = new Double(2);
		BigInteger bi2 = new BigInteger("2");
		
		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("l1", l1);
		vars.put("bd1", bd1);
		vars.put("f1", f1);
		vars.put("bi1", bi1);
		vars.put("d1", d1);
		vars.put("f1", f1);
		vars.put("e1", e1);
		vars.put("i1", i1);
		vars.put("s1", s1);

		assertEquals(bd2, (BigDecimal)eval("l1 + bd1",vars));
		assertEquals(bd2, eval("bd1 + l1",vars));

		assertEquals(bd2, (BigDecimal)eval("f1 + bi1",vars));
		assertEquals(bd2, (BigDecimal)eval("bi1 + f1",vars));

		assertEquals(d2, eval("f1 + l1",vars));
		assertEquals(d2, eval("l1 + f1",vars)); 

		assertEquals(bd2, (BigDecimal)eval("d1 + bi1",vars));
		assertEquals(bd2, (BigDecimal)eval("bi1 + d1",vars));

		assertEquals(d2, eval("d1 + l1",vars));
		assertEquals(d2, eval("l1 + d1",vars));

		assertEquals(bd2, eval("e1 + bi1",vars));
		assertEquals(bd2, eval("bi1 + e1",vars));

		assertEquals(d2, eval("e1+ l1",vars));
		assertEquals(d2, eval("l1 + e1",vars));

		assertEquals(bi2, eval("l1 + bi1",vars));
		assertEquals(bi2, eval("bi1 + l1",vars));

		assertEquals(l2, eval("i1 + l1",vars));
		assertEquals(l2, eval("l1 + i1",vars));

		assertEquals(l2, eval("i1 + s1",vars));
		assertEquals(l2, eval("s1 + i1",vars));
	}
	
	@Test
	public void testSub() {
		assertEquals(new Long(0), eval("null - null"));

		BigDecimal bd1 = new BigDecimal(1);
		Integer i1 = new Integer(1);
		Long l1 = new Long(1);
		Float f1 = new Float(1);
		Double d1 = new Double(1);
		String e1 = "1e0";
		String s1 = "1";
		BigInteger bi1 = new BigInteger("1");

		Long l2 = new Long(0);
		BigDecimal bd2 = new BigDecimal(0);
		Double d2 = new Double(0);
		BigInteger bi2 = new BigInteger("0");
		
		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("l1", l1);
		vars.put("bd1", bd1);
		vars.put("f1", f1);
		vars.put("bi1", bi1);
		vars.put("d1", d1);
		vars.put("f1", f1);
		vars.put("e1", e1);
		vars.put("i1", i1);
		vars.put("s1", s1);

		assertEquals(bd2, eval("l1 - bd1",vars));
		assertEquals(bd2, eval("bd1 - l1",vars));

		assertEquals(bd2, (BigDecimal)eval("f1 - bi1",vars));
		assertEquals(bd2, (BigDecimal)eval("bi1 - f1",vars));

		assertEquals(d2, eval("f1 - l1",vars));
		assertEquals(d2, eval("l1 - f1",vars));

		assertEquals(bd2, (BigDecimal)eval("d1 - bi1",vars));
		assertEquals(bd2, (BigDecimal)eval("bi1 - d1",vars));

		assertEquals(d2, eval("d1 - l1",vars));
		assertEquals(d2, eval("l1 - d1",vars));

		assertEquals(bd2, eval("e1 - bi1",vars));
		assertEquals(bd2, eval("bi1 - e1",vars));

		assertEquals(d2, eval("e1 - l1",vars));
		assertEquals(d2, eval("l1 - e1",vars));

		assertEquals(bi2, eval("l1 - bi1",vars));
		assertEquals(bi2, eval("bi1 - l1",vars));

		assertEquals(l2, eval("i1 - l1",vars));
		assertEquals(l2, eval("l1 - i1",vars));

		assertEquals(l2, eval("i1 - s1",vars));
		assertEquals(l2, eval("s1 - i1",vars));
	}
	
	@Test
	public void testMul() {
		assertEquals(new Long(0), eval("null * null"));

		BigDecimal bd1 = new BigDecimal(1);
		Integer i1 = new Integer(1);
		Long l1 = new Long(1);
		Float f1 = new Float(1);
		Double d1 = new Double(1);
		String e1 = "1e0";
		String s1 = "1";
		BigInteger bi1 = new BigInteger("1");

		Long l2 = new Long(1);
		BigDecimal bd2 = new BigDecimal(1);
		Double d2 = new Double(1);
		BigInteger bi2 = new BigInteger("1");
		
		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("l1", l1);
		vars.put("bd1", bd1);
		vars.put("f1", f1);
		vars.put("bi1", bi1);
		vars.put("d1", d1);
		vars.put("f1", f1);
		vars.put("e1", e1);
		vars.put("i1", i1);
		vars.put("s1", s1);

		assertEquals(bd2, eval("l1 * bd1",vars));
		assertEquals(bd2, eval("bd1 * l1",vars));

		assertEquals(bd2, (BigDecimal)eval("f1 * bi1",vars));
		assertEquals(bd2, (BigDecimal)eval("bi1 * f1",vars));

		assertEquals(d2, eval("f1 * l1",vars));
		assertEquals(d2, eval("l1 * f1",vars));

		assertEquals(bd2, (BigDecimal)eval("d1 * bi1",vars));
		assertEquals(bd2, (BigDecimal)eval("bi1 * d1",vars));

		assertEquals(d2, eval("d1 * l1",vars));
		assertEquals(d2, eval("l1 * d1",vars));

		assertEquals(bd2, eval("e1 * bi1",vars));
		assertEquals(bd2, eval("bi1 * e1",vars));

		assertEquals(d2, eval("e1 * l1",vars));
		assertEquals(d2, eval("l1 * e1",vars));

		assertEquals(bi2, eval("l1 * bi1",vars));
		assertEquals(bi2, eval("bi1 * l1",vars));

		assertEquals(l2, eval("i1 * l1",vars));
		assertEquals(l2, eval("l1 * i1",vars));

		assertEquals(l2, eval("i1 * s1",vars));
		assertEquals(l2, eval("s1 * i1",vars));
	}
	
	@Test
	public void testDiv() {
		assertEquals(new Long(0), eval("null / null"));

		BigDecimal bd1 = new BigDecimal(1);
		Integer i1 = new Integer(1);
		Long l1 = new Long(1);
		Float f1 = new Float(1);
		Double d1 = new Double(1);
		String e1 = "1e0";
		String s1 = "1";
		BigInteger bi1 = new BigInteger("1");

		BigDecimal bd2 = new BigDecimal(1);
		Double d2 = new Double(1);
		
		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("l1", l1);
		vars.put("bd1", bd1);
		vars.put("f1", f1);
		vars.put("bi1", bi1);
		vars.put("d1", d1);
		vars.put("f1", f1);
		vars.put("e1", e1);
		vars.put("i1", i1);
		vars.put("s1", s1);

		assertEquals(bd2, eval("l1 / bd1",vars));
		assertEquals(bd2, eval("bd1 / l1",vars));

		assertEquals(bd2, (BigDecimal)eval("f1 / bi1",vars));
		assertEquals(bd2, eval("bi1 / f1",vars));

		assertEquals(d2, eval("f1 / l1",vars));
		assertEquals(d2, eval("l1 / f1",vars));

		assertEquals(d2, eval("d1 / l1",vars));
		assertEquals(d2, eval("l1 / d1",vars));

		assertEquals(d2, eval("e1 / l1",vars));
		assertEquals(d2, eval("l1 / e1",vars));

		assertEquals(d2, eval("i1 / l1",vars));
		assertEquals(d2, eval("l1 / i1",vars));

		assertEquals(d2, eval("i1 / s1",vars));
		assertEquals(d2, eval("s1 / i1",vars));
	}
	
	@Test
	public void testMod() {
		assertEquals(new Long(0), eval("null % null"));

		BigDecimal bd1 = new BigDecimal(1);
		Integer i1 = new Integer(1);
		Long l1 = new Long(1);
		Float f1 = new Float(1);
		Double d1 = new Double(1);
		String e1 = "1e0";
		String s1 = "1";
		BigInteger bi1 = new BigInteger("1");

		Long l2 = new Long(0);
		Double d2 = new Double(0);
		BigInteger bi2 = new BigInteger("0");
		
		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("l1", l1);
		vars.put("bd1", bd1);
		vars.put("f1", f1);
		vars.put("bi1", bi1);
		vars.put("d1", d1);
		vars.put("f1", f1);
		vars.put("e1", e1);
		vars.put("i1", i1);
		vars.put("s1", s1);

		assertEquals(d2, eval("l1 % bd1",vars));
		assertEquals(d2, eval("bd1 % l1",vars));

		assertEquals(d2, eval("f1 % bi1",vars));
		assertEquals(d2, eval("bi1 % f1",vars));

		assertEquals(d2, eval("f1 % l1",vars));
		assertEquals(d2, eval("l1 % f1",vars));

		assertEquals(d2, eval("d1 % l1",vars));
		assertEquals(d2, eval("l1 % d1",vars));

		assertEquals(d2, eval("d1 % bi1",vars));
		assertEquals(d2, eval("bi1 % d1",vars));

		assertEquals(d2, eval("e1 % bi1",vars));
		assertEquals(d2, eval("bi1 % e1",vars));

		assertEquals(d2, eval("e1 % l1",vars));
		assertEquals(d2, eval("l1 % e1",vars));

		assertEquals(bi2, eval("l1 % bi1",vars));
		assertEquals(bi2, eval("bi1 % l1",vars));

		assertEquals(l2, eval("i1 % l1",vars));
		assertEquals(l2, eval("l1 % i1",vars));

		assertEquals(l2, eval("i1 % s1",vars));
		assertEquals(l2, eval("s1 % i1",vars));
	}
	
	@Test
	public void testMinus() {
		assertEquals(new Long(0), eval("-null"));

		BigDecimal bd1 = new BigDecimal(1);
		Integer i1 = new Integer(1);
		Long l1 = new Long(1);
		Float f1 = new Float(1);
		Double d1 = new Double(1);
		String e1 = "1e0";
		String s1 = "1";
		BigInteger bi1 = new BigInteger("1");

		BigDecimal bd2 = new BigDecimal(-1);
		Integer i2 = new Integer(-1);
		Long l2 = new Long(-1);
		Float f2 = new Float(-1);
		Double d2 = new Double(-1);
		BigInteger bi2 = new BigInteger("-1");
		
		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("l1", l1);
		vars.put("bd1", bd1);
		vars.put("f1", f1);
		vars.put("bi1", bi1);
		vars.put("d1", d1);
		vars.put("f1", f1);
		vars.put("e1", e1);
		vars.put("i1", i1);
		vars.put("s1", s1);

		assertEquals(bd2, eval("-bd1",vars));
		assertEquals(bi2, eval("-bi1",vars));
		assertEquals(d2, eval("-e1",vars));
		assertEquals(l2, eval("-s1",vars));
		assertEquals(i2, eval("-i1",vars));
		assertEquals(l2, eval("-l1",vars));
		assertEquals(d2, eval("-d1",vars));
		assertEquals(f2, eval("-f1",vars));
	}
}
