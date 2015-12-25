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
package leap.web.action;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import leap.lang.Classes;
import leap.lang.Named;

public interface Action extends Named {
	
	Argument[]			EMPTY_ARGUMENTS	   = new Argument[]{};
	ActionInterceptor[] EMPTY_INTERCEPTORS = new ActionInterceptor[]{};
	
	@Override
	default String getName() {
		return toString();
	}
	
	default boolean hasReturnValue() {
		return false;
	}
	
	default boolean hasArguments() {
		return false;
	}
	
	default Class<?> getReturnType() {
		return null;
	}
	
	default Type getGenericReturnType() {
		return null;
	}
	
	default Argument[] getArguments() {
		return EMPTY_ARGUMENTS;
	}
	
	/**
	 * Returns the annotations defined in action level.
	 */
	default Annotation[] getAnnotations() {
		return Classes.EMPTY_ANNOTATION_ARRAY;
	}
	
	/**
	 * Returns the annotations defined in controller level.
	 */
	default Annotation[] getControllerAnnotations() {
		return Classes.EMPTY_ANNOTATION_ARRAY;
	}
	
	/**
	 * Returns the a merged annotations of controller and action.
	 * 
	 * <p>
	 * If an annotation ared defined both in controller and action, the action's will override the annotation of controller.  
	 */
	default Annotation[] getMergedAnnotations() {
		return Classes.EMPTY_ANNOTATION_ARRAY;
	}
	
	/**
	 * Returns the interceptors of this action.
	 */
	default ActionInterceptor[] getInterceptors() {
		return EMPTY_INTERCEPTORS;
	}
	
	default <T extends Annotation> T getAnnotation(Class<T> annotationType) {
		return Classes.getAnnotation(getAnnotations(), annotationType);
	}
	
	default <T extends Annotation> T getControllerAnnotation(Class<T> annotationType) {
		return Classes.getAnnotation(getControllerAnnotations(), annotationType);
	}
	
	default <T extends Annotation> T searchAnnotation(Class<T> annotationType) {
		return null;
	}
	
	Object execute(ActionContext context,Object[] args) throws Throwable;

}