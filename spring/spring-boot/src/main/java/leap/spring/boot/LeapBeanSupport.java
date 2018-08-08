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
import leap.lang.Strings;
import leap.lang.beans.BeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.LinkedHashMap;
import java.util.Map;

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
        if(Global.context == null) {
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
        if(Global.context == null) {
            return null;
        }

        if(type == ApplicationContext.class) {
            return (T)Global.context;
        }

        if(type == BeanFactory.class || type == DefaultListableBeanFactory.class) {
            return (T)((AbstractApplicationContext)Global.context).getBeanFactory();
        }

        try {
            return shouldReturn(Global.context.getBean(type));
        }catch (NoSuchBeanDefinitionException e) {
            return null;
        }
    }

    @Override
    public <T> T tryGetBean(Class<? super T> type, String name) throws BeanException {
        if(Global.context == null) {
            return null;
        }

        Map<String, T> beans = getNamedBeans(type);
        if(null == beans || beans.isEmpty()) {
            return null;
        }

        T bean = beans.get(name);
        if(null == bean) {
            return null;
        }

        return (T) shouldReturn(bean);
    }

    @Override
    public <T> Map<String, T> getNamedBeans(Class<? super T> type) {
        if(Global.context == null) {
            return null;
        }
        Map<String, T> namedBeans = (Map<String,T>)Global.context.getBeansOfType(type);
        if(namedBeans == null || namedBeans.isEmpty()) {
            return namedBeans;
        }

        final String suffix = type.getSimpleName();
        final Map<String, T> beans = new LinkedHashMap<>();
        namedBeans.forEach((name, bean) -> {
            name = Strings.removeEnd(name, suffix);
            beans.put(name, bean);
        });
        return beans;
    }

    protected <T> T shouldReturn(T bean) {
        if(bean.getClass().isAnnotationPresent(Ignore.class)) {
            return null;
        }
        return bean;
    }
}
