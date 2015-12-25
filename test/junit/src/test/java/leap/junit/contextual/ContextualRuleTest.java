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
package leap.junit.contextual;

import java.util.ArrayList;
import java.util.List;

import leap.junit.contexual.ContextualProvider;
import leap.junit.contexual.ContextualRule;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.Description;

public class ContextualRuleTest extends Assert implements ContextualProvider {
	
	private static final ThreadLocal<Context> ctx = new ThreadLocal<Context>();

	@Rule
	public ContextualRule contextualRule = new ContextualRule(this);

	public Iterable<String> names(Description description) {
		List<String> list = new ArrayList<String>();
		
		list.add("name1");
		list.add("name2");
		list.add("name3");
		
	    return list;
    }

	public void beforeTest(Description description, String param) throws Exception {
		Context context = ctx.get();
		
		if(null == context){
			context = new Context();
			ctx.set(context);
		}
		
		context.param   = param;
		context.counter = context.counter + 1;
		context.before  = true;
		context.after   = false;
		context.finish  = false;
    }

	public void afterTest(Description description,String param) throws Exception {
		Context context = ctx.get();
		context.after   = true;
    }
	
	public void finishTests(Description description) throws Exception {
		Context context = ctx.get();
		context.finish  = true;
		assertTrue(ctx.get().after);
		assertEquals(new Integer(3), ctx.get().counter);
    }
	
	@Before
	public void setUp(){
		assertNotNull(ctx.get());
		assertTrue(ctx.get().before);
		assertFalse(ctx.get().after);
		assertFalse(ctx.get().finish);
	}
	
	@After
	public void tearDown(){
		assertNotNull(ctx.get());
		assertFalse(ctx.get().finish);
		assertTrue(ctx.get().before);
		assertFalse(ctx.get().after);
	}
	
	@Test
	public void testContextual(){
		assertNotNull(ctx.get());
		assertEquals("name" + ctx.get().counter, ctx.get().param);
	}
	
	private static final class Context {
		private String  param;
		private Integer counter = 0;
		private boolean before = false;
		private boolean after  = false;
		private boolean finish = false;
	}
}
