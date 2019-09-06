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
import leap.lang.resource.Resources;
import leap.spring.boot.Global;
import leap.spring.boot.LeapResourceLoader;
import leap.web.AppBootstrap;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class BootstrapRunListener implements SpringApplicationRunListener, Ordered, WebApplicationInitializer {

    private static ServletContext sc;

    public BootstrapRunListener() {

    }

    public BootstrapRunListener(SpringApplication application, String[] args) {

    }

    public int getOrder() {
        return HIGHEST_PRECEDENCE + 101;
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        WebConfiguration.startedServletContext = servletContext;
    }

    public void starting() {

    }

    public void environmentPrepared(ConfigurableEnvironment environment) {

    }

    public void contextPrepared(ConfigurableApplicationContext context) {
        if(isRealContext(context)) {
            Resources.setResourceLoader(new LeapResourceLoader(context));
        }
    }

    public void contextLoaded(ConfigurableApplicationContext context) {
        if(isRealContext(context)) {
            Global.context = context;
        }
    }

    //spring-boot 1.5
    public void finished(ConfigurableApplicationContext context, Throwable exception) {
        started(context);
    }

    //spring-boot 2.0
    public void started(ConfigurableApplicationContext context) { }
    public void running(ConfigurableApplicationContext context) { }
    public void failed(ConfigurableApplicationContext context, Throwable exception) {}

    protected boolean isRealContext(ApplicationContext context) {
        boolean web = context instanceof WebApplicationContext;
        if(!web) {
            return false;
        }
        if(null != context.getParent() && context.getParent() instanceof WebApplicationContext) {
            return false;
        }
        return true;
    }

    protected void boot(ServletContext sc) {
        if(AppBootstrap.isInitialized(sc)) {
            return;
        }

        final AppBootstrap bootstrap = new AppBootstrap();
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
        bootstrap.initialize(sc);
    }
}
