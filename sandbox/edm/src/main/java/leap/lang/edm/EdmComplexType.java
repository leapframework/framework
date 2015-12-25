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

public class EdmComplexType extends EdmNamedStructualType {
	
	private final EdmComplexType baseType;

	public EdmComplexType(String name,Iterable<EdmProperty> properties,EdmComplexType baseType) {
	    super(name,properties);
	    this.baseType = baseType;
    }

	public EdmComplexType(String name,Iterable<EdmProperty> properties,boolean isAbstract,EdmComplexType baseType) {
	    super(name, properties, isAbstract);
	    this.baseType = baseType;
    }

	public EdmComplexType(String name, String title, Iterable<EdmProperty> properties, boolean isAbstract,EdmComplexType baseType) {
	    super(name, title, properties, isAbstract);
	    this.baseType = baseType;
    }

	@Override
    public final EdmTypeKind getTypeKind() {
	    return EdmTypeKind.Complex;
    }

	public EdmComplexType getBaseType() {
		return baseType;
	}
}
