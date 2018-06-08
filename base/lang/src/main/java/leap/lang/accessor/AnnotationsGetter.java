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

import leap.lang.Classes;

import java.lang.annotation.Annotation;

public interface AnnotationsGetter {

    /**
     * Required. Returns the annotation array.
     */
    Annotation[] getAnnotations();

    /**
     * Returns the annotation of the given type or null if not exists.
     */
    default <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return Classes.getAnnotation(getAnnotations(), annotationType);
    }

    /**
     * Returns true if the annotation type exists.
     */
    default boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return Classes.isAnnotationPresent(getAnnotations(), annotationType);
    }

}