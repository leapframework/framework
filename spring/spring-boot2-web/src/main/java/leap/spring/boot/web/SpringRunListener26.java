/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.spring.boot.web;

import leap.spring.boot.SpringRunListener;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Spring run listener for spring-boot 2.6.x.
 */
public class SpringRunListener26 extends SpringRunListener {

    public SpringRunListener26(SpringApplication application, String[] args) {
        super(application, args);
    }

    /**
     * The {@link ConfigurableEnvironment} parameter is added after the spring-boot 2.6.x
     * and no longer compatible with {@code environmentPrepared(ConfigurableEnvironment)}.
     */
    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
        prepareGlobalEnvironment(environment);
    }

}
