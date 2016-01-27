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
package leap.web.action;

import java.lang.annotation.Annotation;
import java.util.List;

import leap.lang.Buildable;
import leap.lang.Classes;

public interface ActionBuilder extends Buildable<Action> {

	/**
	 * Required.
     *
     * Returns the name of action.
     */
	String getName();

    /**
     * Returns a mutable list contains the argument of action.
     */
	List<ArgumentBuilder> getArguments();

    /**
     * Adds a argument.
     */
	void addArgument(ArgumentBuilder arg);

    /**
     * Returns the type of return value or null if no return value.
     */
    Class<?> getReturnType();

    /**
     * Returns true if the action has return type.
     */
    default boolean hasReturnType() { return null != getReturnType(); }

    /**
     * Returns the annotations of action.
     */
	default Annotation[] getAnnotations() {
        return Classes.EMPTY_ANNOTATION_ARRAY;
    }

    /**
     * Returns a mutable list contains {@link ActionInterceptor} of the action.
     */
    List<ActionInterceptor> getInterceptors();

    /**
     * Adds an {@link ActionInterceptor} for the action.
     */
    void addInterceptor(ActionInterceptor i);

    /**
     * Returns the instance of controller if exists.
     */
    default Object getController() {
        return null;
    }

    /**
     * Returns true if the given annotation type exists in the action's annotations array.
     */
	default boolean isAnnotationPresent(Class<? extends Annotation> t) {
		return Classes.isAnnotatioinPresent(getAnnotations(), t);
	}
}