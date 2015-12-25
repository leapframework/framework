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
package leap.core.el;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Set;

import leap.core.AppConfigException;
import leap.lang.Classes;
import leap.lang.Strings;
import leap.lang.el.ElFunction;
import leap.lang.el.ElStaticMethod;
import leap.lang.reflect.Reflection;

class ElConfigFunctions {
	
	private final Set<ElConfigFunction> funcs = new LinkedHashSet<ElConfigFunctions.ElConfigFunction>();
	
	void add(ElConfigFunction func) {
		funcs.add(func);
	}
	
	Set<ElConfigFunction> all() {
		return funcs;
	}
	
	static class ElConfigFunction {
		
		String 	   funcPrefix;
		String 	   funcName;
		String 	   className;
		String 	   methodDesc;
		Object     source;
		ElFunction function;
		
		void resolve() {
			Class<?> cls = Classes.tryForName(className);
			if(null == cls) {
				throw new AppConfigException("Class '" + className + "' not found, check xml '" + source + "'");
			}
			
			Method m = null;
			try {
		        m = Reflection.getMethodByNameOrDesc(cls, methodDesc);
	        } catch (Exception e) {
	        	throw new AppConfigException("Method name '" + methodDesc + "', check xml '" + source + "'", e);
	        }
			
			if(Strings.isEmpty(funcName)){
				funcName = m.getName();
			}
			
			if(!Modifier.isPublic(m.getModifiers()) || !Modifier.isStatic(m.getModifiers())){
				throw new AppConfigException("Function method '" + methodDesc + 
											 "' must be 'public static', check xml '" + source + "'");
			}
			
			function = new ElStaticMethod(m);
		}
		
	}
}