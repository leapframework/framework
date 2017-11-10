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

package leap.lang.reflect;

import leap.lang.Classes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class SimpleReflectValued implements ReflectValued {

    protected Annotation[] annotations = Classes.EMPTY_ANNOTATION_ARRAY;
    protected String       name;
    protected Class<?>     type;
    protected Type         genericType;
    protected Object       value;

    public SimpleReflectValued() {
    }

    public SimpleReflectValued(Class<?> type) {
        this.type = type;
    }

    public SimpleReflectValued(Class<?> type, String name) {
        this.type = type;
        this.name = name;
    }

    public SimpleReflectValued(Class<?> type, Type genericType) {
        this.type = type;
        this.genericType = genericType;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    @Override
    public Type getGenericType() {
        return genericType;
    }

    public void setGenericType(Type genericType) {
        this.genericType = genericType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public Annotation[] getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Annotation[] annotations) {
        this.annotations = annotations;
    }

    @Override
    public Object getValue(Object bean) {
        return value;
    }

    @Override
    public void setValue(Object bean, Object value) {
        this.value = value;
    }
}
