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

public abstract class EdmType extends EdmObjectWithDocumentation {

	public abstract EdmTypeKind getTypeKind();
	
	public boolean isSimple(){
		return getTypeKind().equals(EdmTypeKind.Simple);
	}
	
	public boolean isEntity(){
		return getTypeKind().equals(EdmTypeKind.Entity);
	}
	
	public boolean isEntityRef(){
		return this instanceof EdmEntityTypeRef;
	}
	
	public boolean isCollection(){
		return getTypeKind().equals(EdmTypeKind.Collection);
	}
	
	public boolean isComplex(){
		return getTypeKind().equals(EdmTypeKind.Complex);
	}
	
	public EdmCollectionType asCollection(){
		return (EdmCollectionType)this;
	}
	
	public EdmSimpleType asSimple(){
		return (EdmSimpleType)this;
	}
	
	public EdmComplexType asComplex(){
		return (EdmComplexType)this;
	}
	
	public EdmEntityType asEntity(){
		return (EdmEntityType)this;
	}
	
	public EdmEntityTypeRef asEntityRef(){
		return (EdmEntityTypeRef)this;
	}
}