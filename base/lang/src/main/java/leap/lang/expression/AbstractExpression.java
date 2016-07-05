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
package leap.lang.expression;

import java.util.Map;

import leap.lang.convert.Converts;

@SuppressWarnings("unchecked")
public abstract class AbstractExpression implements Expression {

	@Override
    public Object getValue() {
	    return eval(null,null);
    }

	@Override
    public Object getValue(Object context) {
	    return eval(context,null);
    }

	@Override
    public Object getValue(Map<String, Object> vars) {
	    return eval(null,vars);
    }
	
	@Override
    public Object getValue(Object context, Map<String, Object> vars) {
	    return eval(context,vars);
    }
	
	@Override
    public <T> T getValue(Class<T> targetType) {
	    return (T)eval(targetType,null,null);
    }

	@Override
    public <T> T getValue(Class<T> targetType, Object context) {
	    return (T)eval(targetType,context,null);
    }
	
	@Override
    public <T> T getValue(Class<T> targetType, Map<String, Object> vars) {
	    return (T)eval(targetType,null,vars);
    }
	
	@Override
    public <T> T getValue(Class<T> targetType, Object context, Map<String, Object> vars) {
	    return (T)eval(targetType,context,vars);
    }

	protected Object eval(Class<?> targetType,Object context, Map<String, Object> vars) {
		Object v = eval(context, vars);
		
		if(null != targetType){
			return Converts.convert(v, targetType);
		}else{
			return v;
		}
	}
	
	protected abstract Object eval(Object context, Map<String, Object> vars);
}
