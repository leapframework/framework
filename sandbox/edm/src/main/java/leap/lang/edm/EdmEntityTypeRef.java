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


public class EdmEntityTypeRef extends EdmTypeRef {
	
	public static EdmEntityTypeRef of(EdmSchema schema,EdmEntityType type){
		return new EdmEntityTypeRef(type.getName(), EdmUtils.fullQualifiedName(schema,(EdmType)type));
	}	
	
	public static EdmEntityTypeRef of(EdmSchemaBuilder schema,EdmEntityTypeBuilder type){
		return new EdmEntityTypeRef(type.getName(), EdmUtils.fullQualifiedName(schema, type.getName()));
	}
	
	public EdmEntityTypeRef(String name) {
	    super(name,name);
    }

	public EdmEntityTypeRef(String name, String fullQualifiedName) {
	    super(name, fullQualifiedName);
    }

	@Override
    public final EdmTypeKind getRefTypeKind() {
	    return EdmTypeKind.Entity;
    }
}