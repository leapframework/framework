/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.spring.boot;

import leap.core.BeanFactorySupport;
import leap.lang.beans.BeanException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class LeapBeanSupport implements BeanFactorySupport {

    private static final ThreadLocal<Boolean> disabled = ThreadLocal.withInitial(() -> false);

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Ignore {}

    static void disable() {
        disabled.set(true);
    }

    static void enable() {
        disabled.set(false);
    }

    @Override
    public <T> T tryGetBean(String id) throws BeanException {
        if(Global.context == null || disabled.get()) {
            return null;
        }

        try {
            return (T) shouldReturn(Global.context.getBean(id));
        }catch (NoSuchBeanDefinitionException e) {
            return null;
        }
    }

    @Override
    public <T> T tryGetBean(Class<T> type) throws BeanException {
        if(Global.context == null || disabled.get()) {
            return null;
        }

        if(type.getName().startsWith(Global.LEAP_PACKAGE_PREFIX)) {
            return null;
        }

        try {
            return shouldReturn(Global.context.getBean(type));
        }catch (NoSuchBeanDefinitionException e) {
            return null;
        }
    }

    @Override
    public <T> T tryGetBean(Class<? super T> type, String name) throws BeanException {
        if(Global.context == null || disabled.get()) {
            return null;
        }

        if(type.getName().startsWith(Global.LEAP_PACKAGE_PREFIX)) {
            return null;
        }

        try {
            return (T) shouldReturn(Global.context.getBean(type, name));
        }catch (NoSuchBeanDefinitionException e) {
            return null;
        }
    }

    protected <T> T shouldReturn(T bean) {
        if(bean.getClass().isAnnotationPresent(Ignore.class)) {
            return null;
        }
        return bean;
    }
}
