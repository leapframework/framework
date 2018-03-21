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

import leap.core.AppConfig;
import leap.core.AppContext;
import leap.core.BeanFactory;
import org.springframework.boot.ApplicationArguments;

public class StandaloneStarter implements Starter {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        AppContext.initStandalone();

        final AppConfig   config  = AppContext.config();
        final BeanFactory factory = AppContext.factory();
        Global.leap = new Global.LeapContext() {
            @Override
            public AppConfig config() {
                return config;
            }

            @Override
            public BeanFactory factory() {
                return factory;
            }
        };
    }

}