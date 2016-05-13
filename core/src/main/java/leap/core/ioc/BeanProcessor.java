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

import leap.core.AppContext;
import leap.core.BeanFactory;

public interface BeanProcessor {

    /**
     * Called when all {@link BeanProcessor} was created.
     */
    default void postCreateProcessors(AppContext context, BeanFactory factory) throws Throwable {

    }

    /**
     * Called after loading all bean definitions and all {@link BeanProcessor} was created.
     */
	default void postInitBean(AppContext context, BeanFactory factory, BeanDefinitionConfigurator c) throws Throwable {
        
    }

    /**
     * Called when create a bean's instance.
     */
	default void postCreateBean(AppContext context, BeanFactory factory, BeanDefinition def, Object bean) throws Throwable {

    }

}