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
package leap.orm.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import leap.lang.enums.Bool;

@Target({ElementType.TYPE,ElementType.FIELD})
@Retention(RUNTIME)
@Repeatable(ManyToOnes.class)
@ARelation
public @interface ManyToOne {
	
	/**
	 * The relation name, i.e. <code>belongTo</code>.
	 */
	String name() default "";

    /**
     * The entity class that is the target of the association.
     *
     * <p> Defaults to the type of the field or property that stores the association.
     */
	Class<?> targetEntityType() default void.class;
	
	/**
	 * Whether the association is optional. If set to false then a non-null relationship must always exist.
	 */
    Bool optional() default Bool.NONE;
	
    /**
     * The definitions of {@link JoinField} in this relation.
     */
    JoinField[] fields() default {};

}