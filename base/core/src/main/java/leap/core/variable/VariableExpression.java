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
package leap.core.variable;

import java.util.Map;

import leap.lang.Args;
import leap.lang.expression.AbstractExpression;

public class VariableExpression extends AbstractExpression {
	
	public static final String ENV_PREFIX = "env.";
	
	private final VariableEnvironment env;
	private final String      		  name;
	
	public VariableExpression(String variable){
		this(ENV.container(),variable);
	}

	public VariableExpression(VariableEnvironment env,String name) {
		Args.notNull(env,"env");
		Args.notEmpty(name,"name");
		this.env  = env;
		this.name = name;
	}
	
	@Override
    protected Object eval(Object context, Map<String, Object> vars) {
		return env.resolveVariable(name);
    }

	@Override
    public String toString() {
	    return ENV_PREFIX + name;
    }

}