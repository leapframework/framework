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
package leap.core.meta;

import java.lang.reflect.Type;

import leap.lang.meta.MType;

public interface MTypeManager {

    /**
     * Returns the {@link MType} for the java type use default {@link leap.lang.meta.MTypeFactory}.
     */
    MType getMType(Class<?> type);

    /**
     * Returns the {@link MType} for the java type use default {@link leap.lang.meta.MTypeFactory}.
     */
	MType getMType(Class<?> type, Type genericType);

    /**
     * Returns a creator for creating the {@link leap.lang.meta.MTypeFactory}.
     */
	MTypeContainerCreator factory();

}