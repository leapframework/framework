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
package leap.web.json;

import leap.lang.enums.Bool;

import java.lang.annotation.*;

@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface JsonSerialize {
	
	Bool keyQuoted() default Bool.NONE;

	Bool ignoreNull() default Bool.NONE;
	
	Bool ignoreEmpty() default Bool.NONE;

	Bool nullToEmptyString() default Bool.FALSE;

	String namingStyle() default "";

    String dateFormat() default "";

    /**
     * Formats the {@link java.util.Date} with gmt time zone.
     */
    boolean gmt() default false;
}