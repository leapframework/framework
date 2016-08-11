/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
package tests;

import leap.lang.expression.Expression;

import org.junit.Test;

public class ExpressionTest extends HtplTestCase {

	@Test
	public void testUrlExpression(){
		context.setContextPath("/t");
		context.setLocalVariable("v", "b");
		
		Expression expression = engine.getExpressionManager().parseAttributeExpression(engine, "@{/}/a/${v}/c");
		
		String path = context.evalString(expression);
		assertEquals("/t/a/b/c",path);
	}
	
	@Test
	public void testCompositeExpression(){
		context.setLocalVariable("v", "1");
		Expression expression = engine.getExpressionManager().parseCompositeExpression(engine, "${v}");
		assertEquals("1", context.evalString(expression));
		
		expression = engine.getExpressionManager().parseCompositeExpression(engine, "${v}.${v}.1");
		assertEquals("1.1.1", context.evalString(expression));
		
		expression = engine.getExpressionManager().parseCompositeExpression(engine, "\\${v}.${v}.1");
		assertEquals("${v}.1.1", context.evalString(expression));
	}
	
	@Test
	public void testInlineExpressions() {
		context.setLocalVariable("v", "1");

		assertRender("<div>1</div>","<div>${v}</div>");
		assertRender("<div>${v}</div>","<div>\\${v}</div>");
		assertRender("<div>\\1</div>","<div>${'\\\\' + v}</div>");
		assertRender("<div>${v}</div>","<div ht-inline-el='off'>${v}</div>");
		
		assertRender("<div><div a=${v}>${v}</div></div>","<div ht-inline-el='off'><div a=${v}>${v}</div></div>");
		
		assertRender("<div>1<div>${v}</div>1</div>","<div>${v}<div ht-inline-el=false>${v}</div>${v}</div>");
		assertRender("<div>${v}<div>1</div></div>","<div ht-inline-el=false>${v}<div ht-inline-el=true>${v}</div></div>");
	}
	
	@Test
	public void testAttrExpressions() {
		context.setVariable("i", 10);
		context.setVariable("s", "-");

		assertRender("<div a=\"1\"></div>","<div a=\"${i > 9 ? 1 : 0}\"></div>");
		assertRender("<div a=\"1\"></div>","<div a=\"${i < 11 ? 1 : 0}\"></div>");
		assertRender("<div a=\"1\"></div>","<div a=\"${i >= 10 ? 1 : 0}\"></div>");
		assertRender("<div a=\"1\"></div>","<div a=\"${i <= 10 ? 1 : 0}\"></div>");

        assertRender("<div><div a=#{msg}></div></div>","<div ht-inline-el=off><div a=#{msg}></div></div>");
	}
	
	@Test
	public void testFunction() {
		assertRender("1","${json:stringify(1)}");
	}
}
