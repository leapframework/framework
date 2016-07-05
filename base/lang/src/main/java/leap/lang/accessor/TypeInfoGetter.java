/*
 * Copyright 2016 the original author or authors.
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
package leap.lang.accessor;

import leap.lang.TypeInfo;

import java.lang.reflect.Type;

public interface TypeInfoGetter {

    /**
     * Required. Returns the type.
     */
    Class<?> getType();

    /**
     * Optional. Returns the generic type of null.
     */
    Type getGenericType();

    /**
     * Required. Returns the type info.
     */
    TypeInfo getTypeInfo();

    /**
     * Optional. Returns the type of element if the type is a collection type.
     */
    default Class<?> getElementType() {
        return getTypeInfo().getElementType();
    }

    /**
     * Returns true if the type is a simple type.
     */
    default boolean isSimpleType() {
        return getTypeInfo().isSimpleType();
    }

    /**
     * Returns true if the type is a complex type.
     */
    default boolean isComplexType() {
       return getTypeInfo().isComplexType();
    }

    /**
     * Returns true if the type is a collection type.
     */
    default boolean isCollectionType() {
       return getTypeInfo().isCollectionType();
    }

}
