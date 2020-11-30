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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app;

import app.beans.*;
import leap.core.variable.Variable;
import leap.spring.boot.condition.ConditionalOnMaxBootVersion;
import leap.spring.boot.condition.ConditionalOnMinBootVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AutoConfiguration {

    @Bean("hello")
    public HelloBean helloBean1() {
        return new HelloBean();
    }

    @Bean
    public Variable helloVariable() {
        return new HelloVariable();
    }

    @Bean("helloBean")
    public HelloBean helloBean2() {
        return new HelloBean();
    }

    @Bean
    public SpringBean springBean() {
        return new SpringBean();
    }

    @Bean
    @Primary
    @ConditionalOnMaxBootVersion("1.6")
    public TBean bean1() {
        return new TBeanImpl1();
    }

    @Bean
    @Primary
    @ConditionalOnMinBootVersion("1.6")
    public TBean bean2() {
        return new TBeanImpl1();
    }

    @Bean
    @ConditionalOnMinBootVersion("1.5")
    public TBeanImpl2 beanImpl2() {
        return new TBeanImpl2();
    }
}