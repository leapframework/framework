/*
 * Copyright 2015 the original author or authors.
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
package leap.orm.sql.ast;

import leap.lang.Args;
import leap.lang.params.Params;
import leap.orm.sql.Sql;
import leap.orm.sql.SqlStatementBuilder;

public abstract class NamedParamNode extends ParamBase {
	
	protected final String name;

	public NamedParamNode(Sql.Scope scope, String name) {
        super(scope);

		Args.notEmpty(name);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public Object eval(SqlStatementBuilder stm, Params params){
		if(params.contains(name)) {
			return params.get(name);
		}

		if(params.isArray()){
			return params.get(stm.increaseAndGetParameterIndex());
		}else{
			return null;
		}
	}

	@Override
	protected Object getParameterValue(SqlStatementBuilder stm, Params params) {
		return params.get(name);
	}
}
