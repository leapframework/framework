/*
 *   Copyright 2019 the original author or authors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package leap.spring.boot.web;

import leap.core.AppConfig;
import leap.core.AppContext;
import leap.core.BeanFactory;
import leap.spring.boot.Global;
import leap.web.AppBootstrap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

public class BootstrapRunListener implements SpringApplicationRunListener, Ordered {

    public BootstrapRunListener(SpringApplication application, String[] args) {

    }

    public int getOrder() {
        return HIGHEST_PRECEDENCE + 101;
    }

    public void starting() {

    }

    public void environmentPrepared(ConfigurableEnvironment environment) {

    }

    public void contextPrepared(ConfigurableApplicationContext context) {

    }

    public void contextLoaded(ConfigurableApplicationContext context) {

    }

    //spring-boot 1.5
    public void finished(ConfigurableApplicationContext context, Throwable exception) {
        started(context);
    }

    //spring-boot 2.0
    public void started(ConfigurableApplicationContext context) {
        if(context instanceof WebApplicationContext) {
            ServletContext sc = ((WebApplicationContext) context).getServletContext();
            if(AppBootstrap.isInitialized(sc)) {
                return;
            }

            final AppBootstrap bootstrap = new AppBootstrap();
            bootstrap.initialize(sc);

            Global.leap = new Global.LeapContext() {
                @Override
                public AppConfig config() {
                    return bootstrap.getAppConfig();
                }

                @Override
                public BeanFactory factory() {
                    return bootstrap.getBeanFactory();
                }

                @Override
                public AppContext context() {
                    return bootstrap.getAppContext();
                }
            };
        }
    }
    public void running(ConfigurableApplicationContext context) { }
    public void failed(ConfigurableApplicationContext context, Throwable exception) {}

}
