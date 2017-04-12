/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.lang.json;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JsonType {

    /**
     * Definition of different type of sub-type's metadata that can be included in JSON during serialization,
     * and used for deserialization.
     */
    enum MetaType {

        /**
         * Use full class name.
         *
         * <p/>
         * Example :
         * <pre>
         *     {
         *         "@class" : "com.example.SubClass1"
         *     }
         * </pre>
         */
        CLASS_NAME("@class"),

        /**
         * Use type name.
         *
         * <p/>
         * Example :
         * <pre>
         *     {
         *         "@type" : "subType1"
         *     }
         * </pre>
         */
        TYPE_NAME("@type");

        private final String defaultPropertyName;

        MetaType(String defaultPropertyName) {
            this.defaultPropertyName = defaultPropertyName;
        }

        public String getDefaultPropertyName() {
            return defaultPropertyName;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD,ElementType.METHOD})
    @interface TypeProperty {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.ANNOTATION_TYPE)
    @interface SubType {

        /**
         * The class of sub type.
         */
        Class<?> type();

        /**
         * The name of sub type.
         */
        String name();
    }

    /**
     * Specifies the type of sub-type's metadata.
     */
    MetaType meta() default MetaType.CLASS_NAME;

    /**
     * Specifies the property name to save the sub-type's metadata.
     */
    String property() default "";

    /**
     * Defines the sub-types if use {@link MetaType#TYPE_NAME}.
     */
    SubType[] types() default {};
}