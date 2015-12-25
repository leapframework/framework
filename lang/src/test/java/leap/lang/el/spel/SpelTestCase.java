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

import java.util.Map;

import leap.junit.concurrent.ConcurrentTestCase;
import leap.lang.el.DefaultElEvalContext;
import leap.lang.el.ElParseContext;
import leap.lang.el.spel.ast.AstExpr;
import leap.lang.el.spel.parser.Parser;

public abstract class SpelTestCase extends ConcurrentTestCase {

	protected static Object eval(String expr) {
		return Parser.parse(expr).eval(new DefaultElEvalContext(null));
	}
	
	protected static Object eval(String expr,Object context) {
		return Parser.parse(expr).eval(new DefaultElEvalContext(context));
	}
	
	protected static Object eval(String expr,Map<String, Object> vars) {
		return Parser.parse(expr).eval(new DefaultElEvalContext(vars));
	}
	
	
	protected static Object eval(ElParseContext context, String expr) {
		return Parser.parse(context,expr).eval(new DefaultElEvalContext());
	}
	
	protected static Object eval(ElParseContext context, String expr,Object root) {
		return Parser.parse(context,expr).eval(new DefaultElEvalContext(root));
	}
	
	@SuppressWarnings("unchecked")
    protected static <T extends AstExpr> T parse(String expr) {
		return (T)Parser.parse(expr);
	}
	
}
