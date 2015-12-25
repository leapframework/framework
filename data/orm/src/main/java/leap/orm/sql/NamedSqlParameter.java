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
package leap.orm.sql;

import leap.lang.Args;
import leap.lang.expression.Expression;
import leap.lang.params.Params;

public class NamedSqlParameter implements SqlParameter {
	
	private final int        index;
	private final String     name;
	private final Expression defaultValue;
	
	public NamedSqlParameter(int index,String name) {
		this(index,name,null);
	}
	
	public NamedSqlParameter(int index,String name,Expression defaultValue) {
		Args.notEmpty(name,"name");
		this.index 		  = index;
		this.name  		  = name;
		this.defaultValue = defaultValue;
	}

	@Override
	public Object getValue(SqlContext context, Params parameters) {
		Object v = null;
		if(parameters.isIndexed()){
			v = parameters.get(index);
		}else{
			v = parameters.get(name);	
		}
		
		if(null == v && null != defaultValue) {
			v = defaultValue.getValue(context, parameters.map());
		}
		
		return v;
	}

	@Override
    public String toString() {
		return "param[name=" + name + ",index=" + index + "]";
    }

}