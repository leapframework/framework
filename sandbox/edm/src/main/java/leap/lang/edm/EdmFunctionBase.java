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
package leap.lang.edm;

import leap.lang.Enumerable;
import leap.lang.Enumerables;
import leap.lang.Predicates;

public abstract class EdmFunctionBase extends EdmNamedObject {

	private final Enumerable<EdmParameter> parameters;
	
	private final EdmType returnType;

	protected EdmFunctionBase(String name,String title,EdmType returnType,Iterable<EdmParameter> parameters){
		super(name,title);
		this.parameters = Enumerables.of(parameters);
		this.returnType = returnType;
	}
	
	public Enumerable<EdmParameter> getParameters() {
    	return Enumerables.of(parameters);
    }
	
	public EdmParameter getParameter(String name){
		return parameters.firstOrNull(Predicates.<EdmParameter>nameEquals(name));
	}

	public EdmType getReturnType() {
    	return returnType;
    }
}
