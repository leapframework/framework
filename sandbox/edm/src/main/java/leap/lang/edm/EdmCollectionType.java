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

public class EdmCollectionType extends EdmType {
	
	public static EdmCollectionType of(EdmSimpleType elementType) {
		return new EdmCollectionType(elementType);
	}
	
	public static EdmCollectionType of(EdmEntityTypeRef elementType) {
		return new EdmCollectionType(elementType);
	}
	
	public static EdmCollectionType of(EdmComplexTypeRef elementType) {
		return new EdmCollectionType(elementType);
	}

	private final EdmType elementType;
	
	public EdmCollectionType(EdmType elementType){
		this.elementType = elementType;
	}
	
	@Override
    public final EdmTypeKind getTypeKind() {
	    return EdmTypeKind.Collection;
    }

	public EdmType getElementType() {
    	return elementType;
    }
}