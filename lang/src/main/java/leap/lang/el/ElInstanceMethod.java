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
package leap.lang.el;

import java.lang.reflect.Method;

import leap.lang.reflect.ReflectClass;
import leap.lang.reflect.ReflectMethod;

public class ElInstanceMethod implements ElFunction,ElMethod {

	protected final Object        instance;
	protected final ReflectMethod m;
	protected final int			  size;
	
	public ElInstanceMethod(ReflectMethod m) {
		this(m,null);
	}
	
	public ElInstanceMethod(Method m) {
		this(m,null);
	}
	
	public ElInstanceMethod(ReflectMethod m,Object instance) {
		this.m    = m;
		this.size = m.getReflectedMethod().isVarArgs() ? VAR_ARGS : m.getParameters().length;
		this.instance = instance;
	}
	
	public ElInstanceMethod(Method m,Object instance) {
		this.m    = ReflectClass.of(m.getDeclaringClass()).getMethod(m);
		this.size = m.isVarArgs() ? VAR_ARGS : m.getParameterTypes().length; 
		this.instance = instance;
	}

	@Override
    public int getArgumentSize() {
	    return size;
    }

	@Override
    public Object invoke(ElEvalContext context, Object instance, Object[] args) throws Throwable {
	    return m.invoke(instance, args);
    }

	@Override
    public Object invoke(ElEvalContext context, Object[] args) throws Throwable {
		if(null == instance){
			throw new IllegalStateException("Instance is null,cannot invoke method '" + m.getName() + "'");
		}
		return m.invoke(instance, args);
	}
}