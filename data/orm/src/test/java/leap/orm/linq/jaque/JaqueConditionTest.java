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
package leap.orm.linq.jaque;

import leap.junit.TestBase;
import leap.orm.linq.Condition;
import leap.orm.linq.jaque.JaqueConditionTest.O.Status;

import org.junit.Test;

public class JaqueConditionTest extends TestBase {

	@Test
	public void testFieldWithOperator() {
		assertEquals("name = :p1", 	 sql(o -> o.name == "x"));
		assertEquals("status = :p1", sql(o -> o.status == Status.ENALBED));
		assertEquals("age > :p1", 	 	 sql(o -> o.age > 100));
		assertEquals("age < :p1", 	 	 sql(o -> o.age < 100));
		assertEquals("age >= :p1", 	 sql(o -> o.age >= 100));
		assertEquals("age <= :p1", 	 sql(o -> o.age <= 100));
		assertEquals("age <> :p1", 	 sql(o -> o.age != 100));
		
		assertEquals("name = :p1", sql(o -> o.name.equals("x")));
	}
	
	@Test
	public void testGetterWithOperator() {
		assertEquals("name = :p1", 	 sql(o -> o.getName() == "x"));
		assertEquals("status = :p1", sql(o -> o.getStatus() == Status.ENALBED));
		assertEquals("age > :p1", 	 	 sql(o -> o.getAge() > 100));
		assertEquals("age < :p1", 	 	 sql(o -> o.getAge() < 100));
		assertEquals("age >= :p1", 	 sql(o -> o.getAge() >= 100));
		assertEquals("age <= :p1", 	 sql(o -> o.getAge() <= 100));
		assertEquals("age <> :p1", 	 sql(o -> o.getAge() != 100));
		
		assertEquals("name = :p1", sql(o -> o.getName().equals("x")));
	}
	
	@Test
	public void testSimpleFieldWithArg() {
		assertEquals("name = :p1", sqlWithArg("x"));
		assertEquals("name = :p1", sqlWithArg1("x"));
	}
	
	/*TODO : bug
	@Test
	public void testAndOr() {
		assertEquals("name = :p1 and age < :p2", sql(o -> o.name == "x" && o.age < 100));
		assertEquals("name = :p1 or age < :p2", 	sql(o -> o.name == "x" || o.age < 100));
		assertEquals("name = :p1 or (age > :p2 and age < :p3)",sql(o -> o.name == "x" || (o.age > 10 && o.age < 100)));
	}
	*/
	
	private static String sqlWithArg(String s) {
		return sql(o -> o.name == s);
	}
	
	private static String sqlWithArg1(String s) {
		return sql(o -> o.name.equals(s));
	}
	
	private static String sql(Condition<O> cnd) {
		return JaqueConditionParser.sql(cnd);
	}
	
	static class O  {
		
		enum Status  {
			
			ENALBED("E"),
			DISLABED("D");

			private final String value;
			
			private Status(String s) {
				this.value = s;
			}

			public String getValue() {
				return value;
			}
		}
		
		public String name;
		public int	  age;
		public Status status;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public Status getStatus() {
			return status;
		}

		public void setStatus(Status status) {
			this.status = status;
		}
	}
}