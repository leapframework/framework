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

public class EdmFunctionImport extends EdmFunctionBase {

	private final String  entitySet;
	private final String  httpMethod;
	private final boolean sideEffecting;
	
	public EdmFunctionImport(String name,String title,String entitySet, EdmType returnType,Iterable<EdmParameter> parameters) {
	    super(name,title,returnType,parameters);
	    
	    this.entitySet     = entitySet;
	    this.httpMethod    = null;
	    this.sideEffecting = true;
    }
	
	public EdmFunctionImport(String name,String title,String entitySet, EdmType returnType,Iterable<EdmParameter> parameters, 
							 String httpMethod, boolean sideEffecting, 
							 EdmDocumentation documentation) {
		
		super(name,title,returnType,parameters);

		this.entitySet     = entitySet;
	    this.httpMethod    = httpMethod;
	    this.sideEffecting = sideEffecting;
	    this.documentation = documentation;
    }
	
	public boolean isSideEffecting() {
		return sideEffecting;
	}

	public String getHttpMethod() {
    	return httpMethod;
    }

	public String getEntitySet() {
    	return entitySet;
    }
}