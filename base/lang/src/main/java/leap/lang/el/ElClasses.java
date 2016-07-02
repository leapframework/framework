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
import java.util.HashMap;
import java.util.Map;

import leap.lang.Strings;
import leap.lang.reflect.ReflectClass;
import leap.lang.reflect.ReflectMethod;
import leap.lang.reflect.Reflection;

public class ElClasses {
	
	/**
	 * Returns a string indicates the fullname of a function combines the prefix and short name.
	 */
	private static String fullName(String prefix,String name) {
		return Strings.isEmpty(prefix) ? name : (prefix + ":" + name);
	}
	
	
	public static ElFunction createFunction(Class<?> cls, String methodNameOrDesc) {
		return new ElStaticMethod(Reflection.getMethodByNameOrDesc(cls, methodNameOrDesc));
	}
	
	/**
	 * Finds all the public static methods in the given class and create as {@link ElFunction}.
	 */
	public static Map<String, ElFunction> createFunctions(String prefix, Class<?> cls){
		return createFunctions(prefix, cls, false);
	}

	/**
	 * Finds all the public static methods in the given class and create as {@link ElFunction}.
	 * 
	 * <p>
	 * If <code>annotatedOnly</code> is <code>true</code>, only the methods annotated with {@link ElFriendly} will be processed.
	 */
	public static Map<String, ElFunction> createFunctions(String prefix, Class<?> cls, boolean annotatedOnly){
		ReflectClass rc = ReflectClass.of(cls);
		
		Map<String, ElFunction> funcs = new HashMap<String, ElFunction>();
		
		for(ReflectMethod m : rc.getMethods()){
			if(m.isPublic() && m.isStatic()) {
				
				if(annotatedOnly && !isElFriendly(m.getReflectedMethod())){
					continue;
				}
				
				String fullName = fullName(prefix, m.getName());
				
				ElStaticMethods f = (ElStaticMethods)funcs.get(fullName);
				
				if(null == f){
					f = new ElStaticMethods(cls, m.getName());
					funcs.put(fullName, f);
				}
				
				f.add(m);
			}
		}
		
		return funcs;
	}
	
	public static boolean isElFriendly(Method m) {
		ElFriendly a = m.getAnnotation(ElFriendly.class);
		if(a != null && a.value()){
			return true;
		}
		
		a = m.getDeclaringClass().getAnnotation(ElFriendly.class);
		if(a != null && a.value()){
			return false;
		}
		
		return true;
	}
}
