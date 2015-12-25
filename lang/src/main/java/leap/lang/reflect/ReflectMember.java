/*
 * Copyright 2012 the original author or authors.
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
package leap.lang.reflect;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

import leap.lang.Named;

abstract class ReflectMember implements Named {

	protected final ReflectClass reflectiveClass;
	protected final boolean	        declared;
	protected final boolean	        isPublic;
	
	protected ReflectMember(ReflectClass reflectionClass,Member member){
		this.reflectiveClass = reflectionClass;
		this.declared        = member.getDeclaringClass() == reflectionClass.getReflectedClass();
		this.isPublic        = Modifier.isPublic(member.getModifiers());
	}

	public ReflectClass getReflectiveClass() {
    	return reflectiveClass;
    }
	
	public boolean isDeclared() {
    	return declared;
    }
	
	public boolean isPublic(){
		return isPublic;
	}
}