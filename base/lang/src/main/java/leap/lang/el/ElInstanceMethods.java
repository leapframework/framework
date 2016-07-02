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

import java.util.HashMap;
import java.util.Map;

import leap.lang.reflect.ReflectMethod;

public class ElInstanceMethods implements ElFunction,ElMethod {

	protected Object				      instance;
	protected Class<?>                    cls;
	protected String					  name;
	protected Map<Integer, ReflectMethod> methods;
	protected ReflectMethod				  defaultMethod;
	
	public ElInstanceMethods(Class<?> cls, String name) {
		this.cls  = cls;
		this.name = name;
	}
	
	public ElInstanceMethods(Class<?> cls, String name, Object instance) {
		this.cls  = cls;
		this.name = name;
		this.instance = instance;
	}
	
	@Override
    public int getArgumentSize() {
	    return VAR_ARGS;
    }

	public ElInstanceMethods add(ReflectMethod m) {
		if(!m.getName().equals(name)){
			throw new IllegalArgumentException("Method name '" + m.getName() + "' must be '" + name + "'");
		}
		
		if(m.getReflectedMethod().isVarArgs()){
			defaultMethod = m;
		}else{
			Integer i = m.getParameters().length;
			if(!methods().containsKey(i)){
				methods().put(i, m);
			}
		}
		
		return this;
	}
	
	@Override
    public Object invoke(ElEvalContext context, Object instance, Object[] args) throws Throwable {
		ReflectMethod m = null == methods ? null : methods.get(args.length);
		
		if(null == m){
			m = defaultMethod;
		}
		
		if(null == m){
			throw new ElException("Invalid argument size '" + args.length + 
								  "' for invoking method '" + name + "' in class '" + cls.getName() + "'");
		}
		
	    return invoke(m, instance, args);
    }

	@Override
    public Object invoke(ElEvalContext context, Object[] args) throws Throwable {
		return invoke(context,instance,args);
    }

	protected Object invoke(ReflectMethod m,Object owner,Object[] args) throws Throwable {
		if(null == instance){
			throw new ElException("Cannot invoke instance method '" + name + "', the instance is null");
		}
		return m.invoke(owner, args);
	}
	
	protected Map<Integer, ReflectMethod> methods() {
		if(null == methods){
			methods = new HashMap<Integer, ReflectMethod>();
		}
		return methods;
	}
}

