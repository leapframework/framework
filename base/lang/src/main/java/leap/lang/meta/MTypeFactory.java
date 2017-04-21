/*
 * Copyright 2015 the original author or authors.
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

import java.lang.reflect.Type;

public interface MTypeFactory {

    /**
     * Returns the {@link MType} of the java type with default {@link MTypeContext}.
     */
    default MType getMType(Class<?> type) {
		return getMType(type, null, MTypeContext.DEFAULT);
	}

    /**
     * Returns the {@link MType} of the java type with default {@link MTypeContext}.
     */
	default MType getMType(Class<?> type, Type genericType) {
		return getMType(type, genericType, MTypeContext.DEFAULT);
	}

    /**
     * Returns the {@link MType} of the java type with default {@link MTypeContext}.
     */
    default MType getMType(Class<?> declaringClass, Class<?> type, Type genericType) {
        return getMType(declaringClass, type, genericType, MTypeContext.DEFAULT);
    }

    /**
     * Returns the {@link MType} of the java type.
     * @param type required.
     * @param genericType optional.
     * @param context required.
     */
	default MType getMType(Class<?> type, Type genericType, MTypeContext context) {
        return getMType(null, type, genericType, context);
    }

    /**
     * Returns the {@link MType} of the java type.
     *
     * @param declaringClass optional. the type declaring the type and the generic type.
     * @param type required.
     * @param genericType optional.
     * @param context required.
     */
    MType getMType(Class<?> declaringClass, Class<?> type, Type genericType, MTypeContext context);

}