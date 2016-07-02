/*
 * Copyright 2014 the original author or authors.
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
package leap.lang;

import leap.lang.meta.MTypeKind;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * The type info of java type.
 */
public class TypeInfo {

	private final Class<?>   type;
	private final Type       genericType;
	private final MTypeKind	 typeKind;
	private final Class<?>	 elementType;
	private final TypeInfo   elementTypeInfo;
	
	public TypeInfo(Class<?> type, Type genericType, MTypeKind kind,Class<?> elementType, TypeInfo elementTypeInfo) {
		Args.notNull(type,"type");
		Args.notNull(kind,"kind");
		
		if(null != elementType){
			Args.assertTrue(kind == MTypeKind.COLLECTION, "The element type must not be null if the kind is 'COLLECTION'");
			Args.assertTrue(elementTypeInfo != null,"The element type info must not be null if the element type is not null");
		}else{
			Args.assertFalse(kind == MTypeKind.COLLECTION, "The element type must be null if the kind is not 'COLLECTION'");
			Args.assertTrue(elementTypeInfo == null, "The element type info must be null if the element type is null");
		}
		
		this.type			 = type;
		this.genericType     = genericType;
		this.typeKind        = kind;
		this.elementType     = elementType;
		this.elementTypeInfo = elementTypeInfo;
	}
	
	public Class<?> getType() {
		return type;
	}

	public Type getGenericType() {
		return genericType;
	}
	
	public MTypeKind getTypeKind() {
		return typeKind;
	}

	public Class<?> getElementType() {
		return elementType;
	}

	public TypeInfo getElementTypeInfo() {
		return elementTypeInfo;
	}

	public boolean isSimpleType() {
		return typeKind == MTypeKind.SIMPLE;
	}

	public boolean isComplexType(){
		return typeKind == MTypeKind.COMPLEX;
	}

    /**
     * Returns true if the type is {@link Map}.
     *
     * <p/>
     * The {@link Map} is also a complex type.
     */
	public boolean isMap() {
        return type == Map.class;
    }
	
	public boolean isSimpleElementType() {
	    return null != elementTypeInfo && elementTypeInfo.isSimpleType();
	}
	
	public boolean isComplexElementType() {
		return null != elementTypeInfo && elementTypeInfo.isComplexType();
	}
	
	public boolean isCollectionType() {
		return typeKind == MTypeKind.COLLECTION;
	}
}