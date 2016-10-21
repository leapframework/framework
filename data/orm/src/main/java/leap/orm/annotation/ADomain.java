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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import leap.lang.Ordered;
import leap.lang.enums.Bool;

@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ADomain {

	/**
	 * The name of domain. Default is the simple name of annotation type.
	 */
	String name() default "";
	
	/**
	 * The column name.
	 */
	String column() default "";
	
	/**
	 * The type name of column.
	 */
	ColumnType type() default ColumnType.AUTO;
	
	/**
	 * The length property of column.
	 */
	int length() default -1;
	
	/**
	 * The precision property of column
	 */
	int precision() default -1;
	
	/**
	 * The scale property of column.
	 */
	int scale() default -1;
	
	/**
	 * The nullable property of column.
	 */
	Bool nullable() default Bool.NONE;

	/**
	 * The default value of column.
	 */
	String defaultValue() default "";
	
	/**
	 * Is the column insertable.
	 */
	Bool insert() default Bool.NONE;
	
	/**
	 * Is the column updatable.
	 */
	Bool update() default Bool.NONE;
	
	String insertValue() default "";
	
	String updateValue() default "";

    /**
     * The sort order.
     */
	float order() default Ordered.MINIMUM_SORT_ORDER;
	
	boolean override() default false;
}