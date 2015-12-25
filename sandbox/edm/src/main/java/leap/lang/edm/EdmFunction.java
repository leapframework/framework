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

public class EdmFunction extends EdmFunctionBase {

	private final String definingExpression;

	public EdmFunction(String name,String title,EdmType returnType,Iterable<EdmParameter> parameters,String definingExpression) {
	    super(name,title,returnType,parameters);
	    
	    this.definingExpression = definingExpression;
    }
	
	public EdmFunction(String name,String title,EdmType returnType,Iterable<EdmParameter> parameters,String definingExpression,EdmDocumentation documentation) {
		this(name,title,returnType,parameters,definingExpression);
		
		this.documentation = documentation;
	}

	public String getDefiningExpression() {
    	return definingExpression;
    }
}