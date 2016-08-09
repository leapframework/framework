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
package leap.lang.meta;


public abstract class MType extends ImmutableMDescribed {

	public MType(String summary, String description) {
	    super(summary, description);
    }

	/**
	 * Returns <code>true</code> if this type is a simple type.
	 * 
	 * @see MSimpleType
	 */
	public boolean isSimpleType(){
		return getTypeKind() == MTypeKind.SIMPLE;
	}

    /**
     * Returns true if this type is an object type.
     */
    public boolean isObjectType() {
        return getTypeKind() == MTypeKind.OBJECT;
    }

	/**
	 * Returns <code>true</code> if this type is a collection type.
	 */
	public boolean isCollectionType(){
		return getTypeKind() == MTypeKind.COLLECTION;
	}
	
	/**
	 * Returns <code>true</cod> if this type is a complex type.
	 */
	public boolean isComplexType(){
		return getTypeKind() == MTypeKind.COMPLEX;
	}

    /**
     * Returns true if this type is a dictionary type.
     *
     * <p/>
     * Note : a dictionary type is also a complex type.
     */
    public boolean isDictionaryType() {
        return false;
    }

    /**
	 * Returns <code>true</code> if this type is a reference type.
	 */
	public boolean isTypeRef() {
		return getTypeKind() == MTypeKind.REFERENCE;
	}
	
	/**
	 * Returns the kind of this type.
	 */
	public abstract MTypeKind getTypeKind();
	
	/**
	 * Returns this object as {@link MSimpleType}.
	 */
	public final MSimpleType asSimpleType() {
		return (MSimpleType)this;
	}
	
	/**
	 * Returns this object as {@link MComplexType}.
	 */
	public final MComplexType asComplexType() {
		return (MComplexType)this;
	}
	
	/**
	 * Returns this object as {@link MCollectionType}.
	 */
	public final MCollectionType asCollectionType() {
		return (MCollectionType)this;
	}
	
	/**
	 * Returns this object as {@link MTypeRef}.
	 */
	public final MTypeRef asTypeRef() {
		return (MTypeRef)this;
	}

    /**
     * Returns this object as {@link MDictionaryType}.
     */
    public final MDictionaryType asDictionaryType() {
        return (MDictionaryType)this;
    }
}