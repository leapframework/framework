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
package leap.core.el;

import leap.core.variable.VariableEnvironment;
import leap.lang.el.ElEvalContext;
import leap.lang.el.ElException;
import leap.lang.el.ElPropertyResolver;

public class EnvPropertyResolver implements ElPropertyResolver {

	protected VariableEnvironment env;
	
	public EnvPropertyResolver(VariableEnvironment env) {
		this.env = env;
	}

	@Override
	public Object resolveProperty(String name, ElEvalContext context) {
		Object v = env.resolveVariable(name, context);
		
		if(null == v && !env.checkVariableExists(name)){
			throw new ElException("Environment variable 'env." + name + "' cannot be resolved");
		}
		
		return v;
	}

}
