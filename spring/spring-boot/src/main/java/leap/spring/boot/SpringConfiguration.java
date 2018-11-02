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

import leap.core.AppContextInitializer;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import leap.spring.boot.spel.SpringExpressionFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfiguration {
    private static final Log log = LogFactory.get(SpringConfiguration.class);

    static {
        boolean enabled = false;
        Resource resource = Resources.getResource("classpath:instrument");
        if(null != resource && resource.exists()) {
            if("enabled".equals(resource.getContent())) {
                log.info("Instrument enabled by '{}'", resource.getURLString());
                enabled = true;
            }
        }

        //Disable instrumentation.
        if(!enabled) {
            AppContextInitializer.setInstrumentDisabled(true);
        }
    }

    @Bean
    public static BeanFactoryPostProcessor beanFactoryPostProcessor() {
        return springBeanFactory -> {
            if(springBeanFactory instanceof DefaultListableBeanFactory) {
                DefaultListableBeanFactory dbf = ((DefaultListableBeanFactory) springBeanFactory);
                dbf.setAutowireCandidateResolver(new SpringAutowireResolver(dbf.getAutowireCandidateResolver()));
            }else{
                log.error("Found unsupported spring bean factory '{}', can't autowire leap's beans", springBeanFactory);
            }
        };
    }

    @Bean
    public InstantiationAwareBeanPostProcessor beanPostProcessor() {
        return new SpringBeanPostProcessor();
    }

    @Bean
    public EnvironmentPostProcessor environmentPostProcessor() {
        return new SpringEnvPostProcessor();
    }
}