/*
 * Copyright 2016 the original author or authors.
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

/**
 * The configurator of {@link BeanDefinition}.
 */
public interface BeanDefinitionConfigurator {

    /**
     * Returns the definition.
     */
    BeanDefinition definition();

    /**
     * Sets lazy init of the bean.
     */
    void setLazyInit(boolean b);

    /**
     * Sets configurable of bean.
     */
    void setConfigurable(boolean b);

    /**
     * Sets the key prefix of configuration properties.
     */
    void setConfigurationPrefix(String prefix);

    /**
     * Sets bean sort order
     */
    void setSortOrder(int order);

}