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
package leap.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@AEntity
public @interface Table {
	
	/**
	 * The name of the table.
	 */
	String value() default "";

    /**
     * The name of the table.
     */
    String name() default "";

    /**
     * The config's property name of table's name.
     *
     * <p/>
     * If both the {@link #name()} and {@link #configName()} are specified,
     *
     * the config property will be checked and used first,
     *
     * if the property does not exists, the {@link #name()} will be used.
     */
    String configName() default "";

    /**
     * If true will auto create the table if not exists.
     */
    boolean autoCreate() default false;

}