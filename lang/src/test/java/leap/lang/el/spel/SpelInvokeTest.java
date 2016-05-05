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

import leap.lang.Strings;
import leap.lang.el.AbstractElFunction;
import leap.lang.el.DefaultElParseContext;
import leap.lang.el.ElEvalContext;
import leap.lang.el.ElParseException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class SpelInvokeTest extends SpelTestCase {

    public static String t() {
        return "ok";
    }

	@Test
	public void testMemberMethod() {
		TestedBean bean = new TestedBean();
		
		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("bean",    bean);
		vars.put("osworld", new OString("world"));
		
		assertEquals(bean.sayHello("world"),eval("bean.sayHello('world')",vars));
		assertEquals(bean.sayHello(new OString("world")), eval("bean.sayHello(osworld)",vars));
		
		vars.put("s", "Hello");
		assertEquals(1, eval("s.indexOf('e')",vars));
	}

    @Test
    public void testStaticMethod() {
        assertEquals("ok", eval("T(" + SpelInvokeTest.class.getName() + ").t()"));
    }

    @Test
    public void testUnsafeMothod() {
        try{
            eval("T(System).gc()");
            fail();
        }catch(ElParseException e) {
            assertTrue(e.getMessage().contains("unsafe"));
        }
    }
	
	@Test
	public void testBeanProperty() {
		TestedBean bean = new TestedBean();
		bean.setStr("hello");
		
		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("bean", bean);
		
		assertEquals(bean.getStr(),eval("bean.str",vars));
		assertEquals(new Boolean(bean.isOk()),eval("bean.ok",vars));
	}
	
	@Test
	public void testClassField() {
		assertEquals('x', eval("c",new TestedBean()));
		assertEquals(10, eval("num",new TestedBean()));
	}
	
	@Test
	public void testFunction() {
		DefaultElParseContext c = new DefaultElParseContext();
		c.setFunction("test", new AbstractElFunction() {
			@Override
			public Object invoke(ElEvalContext context, Object[] args) throws Throwable {
				return Strings.join(args,',');
			}
		});
		
		assertEquals("1,2", eval(c,"test(1,2)"));
	}
	
	public static class TestedBean {
		public static char c = 'x';
		
		public int num = 10;
		
		private String str;

		public String getStr() {
			return str;
		}

		public void setStr(String str) {
			this.str = str;
		}
		
		public String sayHello(String who){
			return "Hello " + who + "!";
		}
		
		public String sayHello(O o) {
			return "O Hello " + o + "!";
		}
		 
		public boolean isOk() {
			return true;
		}
		
		public int getIntValue() {
			return 1;
		}
	}
	
	public static interface O {
		
	}
	
	public static class OString implements O  {
		
		private final String s;
		
		public OString(String s) {
			this.s = s;
		}

		@Override
        public String toString() {
			return s;
		}
	}
}
