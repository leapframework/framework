/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.core.ioc;

public interface BeanDefinitions {

    /**
     * Returns an exists or creates a new definition of a primary bean for the given type.
     */
    default BeanDefinitionConfigurator getOrAdd(Class<?> beanClass) {
        return getOrAdd(beanClass, beanClass);
    }

    /**
     * Returns an exists or creates a new definition of a primary bean for the given type.
     */
    BeanDefinitionConfigurator getOrAdd(Class<?> type, Class<?> beanClass);

    /**
     * Return an exists or creates a new definition of a named bean for the given type.
     */
    default BeanDefinitionConfigurator getOrAdd(Class<?> beanClass, String name) {
        return getOrAdd(beanClass, beanClass, name);
    }

    /**
     * Return an exists or creates a new definition of a named bean for the given type.
     */
    BeanDefinitionConfigurator getOrAdd(Class<?> type, Class<?> beanClass, String name);

}