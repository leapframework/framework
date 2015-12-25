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

public final class EdmBuilders {
	
	public static EdmSchemaBuilder schema(String namespace,String alias){
		return new EdmSchemaBuilder(namespace, alias);
	}
	
	public static EdmEnumTypeBuilder enumType(String name){
		return new EdmEnumTypeBuilder(name);
	}
	
	public static EdmEntityTypeBuilder entityType(String name,String fullQualifiedName) {
		return new EdmEntityTypeBuilder(name,fullQualifiedName);
	}
	
	public static EdmEntityTypeBuilder entityType(EdmSchema schema,String name) {
		return new EdmEntityTypeBuilder(name,EdmUtils.fullQualifiedName(schema, name));
	}
	
	public static EdmComplexTypeBuilder complexType(String name) {
		return new EdmComplexTypeBuilder(name);
	}

	public static EdmPropertyBuilder property(String name) {
		return new EdmPropertyBuilder(name);
	}
	
	public static EdmPropertyBuilder dynamicProperty(EdmType type,String name) {
		return new EdmPropertyBuilder(name).setType(type).setNullable(true);
	}
	
	public static EdmNavigationPropertyBuilder navigationProperty(String name){
		return new EdmNavigationPropertyBuilder(name);
	}
	
	public static EdmAssociationBuilder association(String name) {
		return new EdmAssociationBuilder(name);
	}
	
	public static EdmEntityContainerBuilder entityContainer(String name){
		return new EdmEntityContainerBuilder(name);
	}
	
	public static EdmEntityContainerBuilder entityContainer(String name,boolean isDefault){
		return new EdmEntityContainerBuilder(name).setDefault(isDefault);
	}
	
	public static EdmEntitySetBuilder entitySet(String name){
		return new EdmEntitySetBuilder(name);
	}
	
	public static EdmAssociationSetBuilder associationSet(String name) {
		return new EdmAssociationSetBuilder(name);
	}
	
	public static EdmFunctionImportBuilder functionImport(String name) {
		return new EdmFunctionImportBuilder(name);
	}
	
	private EdmBuilders(){
		
	}
}
