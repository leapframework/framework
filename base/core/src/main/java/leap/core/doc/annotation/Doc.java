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

package leap.core.doc.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Doc {

    /**
     * The summary, same as {@link #summary()}.
     */
    String value() default "";

    /**
     * The summary.
     */
    String summary() default "";

    /**
     * The description.
     */
    String desc() default "";

    /**
     * The profile.
     *
     * request profile example: http://xxxx/swagger.json?profile=profileA
     */
    String[] profile() default "";

    /**
     * The name of tags.
     */
    String[] tags() default {};

}
