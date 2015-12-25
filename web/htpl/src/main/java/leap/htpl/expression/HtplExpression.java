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
package leap.htpl.expression;

import java.util.Map;

import leap.htpl.escaping.EscapableExpression;
import leap.lang.Args;
import leap.lang.convert.Converts;
import leap.lang.expression.AbstractExpression;
import leap.lang.expression.Expression;

public class HtplExpression extends AbstractExpression implements EscapableExpression {
	
	private final Expression real;
	private final boolean    autoEscape;

	public HtplExpression(Expression real, Parameters parameters) {
		Args.notNull(real,"real");
		Args.notNull(parameters,"prameters");
		this.real       = real;
		this.autoEscape = parameters.isAutoEscape();
	}

	@Override
    public boolean isAutoEscape() {
	    return autoEscape;
    }

	@Override
    protected Object eval(Object context, Map<String, Object> vars) {
	    return real.getValue(context, vars);
    }
	
	public static final class Parameters {
		public static final String ESCAPE = "escape";
		
		public static final Parameters UN_ESCAPED = new Parameters();
		static {
			UN_ESCAPED.set(ESCAPE, "off");
		}
		
		private boolean autoEscape = true;
		
		public boolean isAutoEscape() {
			return autoEscape;
		}

		public boolean set(String key,String value) {
			if(ESCAPE.equals(key)) {
				if("off".equalsIgnoreCase(value)) {
					autoEscape = false;
				}else if("on".equalsIgnoreCase(value)) {
					autoEscape = true;
				}else{
					autoEscape = Converts.toBoolean(value);
				}
				return true;
			}
			return false;
		}
	}
}