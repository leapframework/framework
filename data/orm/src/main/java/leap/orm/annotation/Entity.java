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

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@AEntity
public @interface Entity {

    @Target({ElementType.ANNOTATION_TYPE,ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(Listeners.class)
    @Inherited
    @interface Listener {

        /**
         * The listener's class.
         */
        Class<?> type();

        /**
         * Set to <code>true</code> will invokes the listener in a transaction.
         */
        boolean transactional() default false;
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @interface Listeners {
        Listener[] value();
    }
	
	/**
	 * (Optional) The name of the entity.
	 */
	String value() default "";
	
	/**
	 * (Optional) The name of the entity.
	 */
	String name() default "";
	
	/**
	 * (Optional) The database schema name of the entity.
	 */
	String schema() default "";
	
	/**
	 * (Optional) The database table name of the entity.
	 */
	String table() default "";

    /**
     * Is a extended entity?
     */
    boolean extended() default false;

    /**
     * (Optional). the extended class.
     */
    Class<?> extendsOf() default Void.class;

    /**
     * (Optional) The secondary table name of the entity.
     */
    String secondaryTable() default "";

    /**
     * The entity listeners.
     */
    Listener[] listeners() default {};
}