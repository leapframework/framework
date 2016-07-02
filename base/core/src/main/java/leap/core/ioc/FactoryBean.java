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
package leap.core.ioc;

import leap.core.BeanFactory;

/**
 * A bean used as a factory for providing object(s).
 */
public interface FactoryBean<T> {

    /**
     * Returns the primary bean of the given type.
     *
     * <p/>
     * Returns null if no primary bean exists.
     */
	default T getBean(BeanFactory beanFactory, Class<T> type) {
        return null;
    }

    /**
     * Returns the named bean of the given type.
     *
     * <p/>
     * Returns null if no named bean exists.
     */
	default T getBean(BeanFactory beanFactory, Class<T> type,String name) {
        return null;
    }

}