/*
 * Copyright 2013 the original author or authors.
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
package leap.web.annotation;

import java.lang.annotation.*;

/**
 * Annotates a bean (complex type) that wraps the request parameters.
 *
 * <p/>
 *
 * The type of annotated parameter must be complex type.
 */
@Target({ElementType.PARAMETER,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RequestBean {

    @Target({ElementType.FIELD,ElementType.METHOD,ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @interface BodyParams {

    }

    /**
     * Validates the bean or not.
     */
    boolean valid() default true;

    /**
     * Returns true if the bean is also a request body argument.
     */
    boolean requestBody() default false;

}