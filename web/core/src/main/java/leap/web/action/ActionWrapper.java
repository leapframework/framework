/*
 * Copyright 2018 the original author or authors.
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

package leap.web.action;

import leap.lang.reflect.ReflectMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

public class ActionWrapper implements Action {

    protected final Action action;

    public ActionWrapper(Action action) {
        this.action = action;
    }

    @Override
    public Object execute(ActionContext context, Object[] args) throws Throwable {
        return action.execute(context, args);
    }

    @Override
    public <T> void setExtension(Class<T> type, Object extension) {
        action.setExtension(type, extension);
    }

    @Override
    public <T> T removeExtension(Class<?> type) {
        return action.removeExtension(type);
    }

    @Override
    public Map<Class<?>, Object> getExtensions() {
        return action.getExtensions();
    }

    @Override
    public <T> T getExtension(Class<?> type) {
        return action.getExtension(type);
    }

    @Override
    public String getName() {
        return action.getName();
    }

    @Override
    public boolean hasController() {
        return action.hasController();
    }

    @Override
    public Object getController() {
        return action.getController();
    }

    @Override
    public Class<?> getControllerClass() {
        return action.getControllerClass();
    }

    @Override
    public ReflectMethod getMethod() {
        return action.getMethod();
    }

    @Override
    public boolean hasReturnValue() {
        return action.hasReturnValue();
    }

    @Override
    public boolean hasArguments() {
        return action.hasArguments();
    }

    @Override
    public Class<?> getReturnType() {
        return action.getReturnType();
    }

    @Override
    public Type getGenericReturnType() {
        return action.getGenericReturnType();
    }

    @Override
    public Argument[] getArguments() {
        return action.getArguments();
    }

    @Override
    public Annotation[] getAnnotations() {
        return action.getAnnotations();
    }

    @Override
    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return action.getAnnotationsByType(annotationClass);
    }

    @Override
    public Annotation[] getControllerAnnotations() {
        return action.getControllerAnnotations();
    }

    @Override
    public Annotation[] getMergedAnnotations() {
        return action.getMergedAnnotations();
    }

    @Override
    public ActionInterceptor[] getInterceptors() {
        return action.getInterceptors();
    }

    @Override
    public <T extends Annotation> T getControllerAnnotation(Class<T> annotationType) {
        return action.getControllerAnnotation(annotationType);
    }

    @Override
    public <T extends Annotation> T searchAnnotation(Class<T> annotationType) {
        return action.searchAnnotation(annotationType);
    }

    @Override
    public <T> void setExtension(T extension) {
        action.setExtension(extension);
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return action.getAnnotation(annotationType);
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return action.isAnnotationPresent(annotationType);
    }

    @Override
    public String toString() {
        return action.toString();
    }
}
