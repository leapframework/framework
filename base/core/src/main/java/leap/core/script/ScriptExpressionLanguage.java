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
package leap.core.script;

import leap.core.el.ElConfig;
import leap.core.el.ExpressionLanguage;
import leap.lang.expression.Expression;

public class ScriptExpressionLanguage implements ExpressionLanguage,ScriptLanguage {

	protected final String engineName;
	
	public ScriptExpressionLanguage(String engineName){
		this.engineName = engineName;
	}
	
	@Override
    public Expression createExpression(String expression) {
	    return new ScriptExpression(engineName, expression);
    }

	@Override
    public Expression createExpression(ElConfig config, String expression) {
		throw new IllegalStateException("Not implemented");
	}
}	