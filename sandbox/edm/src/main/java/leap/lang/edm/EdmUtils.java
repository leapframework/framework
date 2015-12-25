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

import leap.lang.Named;
import leap.lang.Strings;

public class EdmUtils {

	protected EdmUtils(){
		
	}
	
	public static String fullQualifiedName(EdmSchema schema,EdmType type) {
		if(type instanceof EdmNamedStructualType){
			return fullQualifiedName(schema, ((EdmNamedStructualType) type).getName());
		}else if(type instanceof EdmSimpleType){
			return ((EdmSimpleType)type).getFullQualifiedName();
		}else if(type instanceof EdmTypeRef){
			return fullQualifiedName(schema,((EdmTypeRef)type).getName());
		}else if(type instanceof Named){
			return fullQualifiedName(schema,((Named)type).getName());		
		}else if(type instanceof EdmCollectionType){
			return "Collection(" +  fullQualifiedName(schema, ((EdmCollectionType)type).getElementType()) + ")";
		}
		return Strings.EMPTY;
	}
	
	public static String fullQualifiedName(EdmSchema schema,EdmNamedObject named){
		return fullQualifiedName(schema,named.getName());
	}
	
	public static String fullQualifiedName(EdmSchema schema,String name) {
		return Strings.join(new String[]{schema.getNamespaceName(),name}, ".",true);
	}
	
	public static String fullQualifiedName(EdmSchemaBuilder schema,EdmNamedObject named) {
		return Strings.join(new String[]{schema.getNamespace(),named.getName()}, ".",true);
	}
	
	public static String fullQualifiedName(EdmSchemaBuilder schema,String name) {
		return Strings.join(new String[]{schema.getNamespace(),name}, ".", true);
	}
}