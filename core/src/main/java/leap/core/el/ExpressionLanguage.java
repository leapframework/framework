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
package leap.core.el;

import leap.lang.expression.Expression;

public interface ExpressionLanguage {
	
	/**
	 * The expression must not prefix with '${' and suffix with '}'
	 */
	Expression createExpression(String expression);
	
	/**
	 * The expression must not prefix with '${' and suffix with '}'
	 */
	Expression createExpression(ElConfig config,String expression);
	
	/**
	 * The expression must not prefix with '${' and suffix with '}'
	 */
	default Expression createExpression(Object parseContext,String expression) {
		throw new IllegalStateException("El '" + this.getClass().getSimpleName() + "' does not supports parse context");
	}
	
}