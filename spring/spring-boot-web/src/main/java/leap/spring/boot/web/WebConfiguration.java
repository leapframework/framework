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

package leap.spring.boot.web;

import leap.core.AppConfig;
import leap.core.AppConfigException;
import leap.core.AppContext;
import leap.core.BeanFactory;
import leap.lang.Classes;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.reflect.ReflectClass;
import leap.spring.boot.Global;
import leap.web.AppBootstrap;
import leap.web.AppFilter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.weaving.LoadTimeWeaverAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.instrument.classloading.LoadTimeWeaver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.PostConstruct;
import javax.servlet.*;
import java.util.List;
import java.util.Map;

@Configuration
public class WebConfiguration {

    private static final Log log = LogFactory.get(WebConfiguration.class);

    static AppFilter      filter;
    static ServletContext startedServletContext;

    static {
        if (null == Global.leap) {
            Global.leap = new Global.LeapContext() {
                @Override
                public AppConfig config() {
                    return null == filter ? null : filter.config();
                }

                @Override
                public BeanFactory factory() {
                    return null == filter ? null : filter.factory();
                }

                @Override
                public AppContext context() {
                    return null == filter ? null : filter.context();
                }
            };
        }
    }

    @Bean
    @Lazy(false)
    public AppFilter filterBean(Environment env) {
        String   clsName = env.getProperty("leap.filter.className", AppFilter.class.getName());
        Class<?> cls     = Classes.forName(clsName);
        if (!AppFilter.class.isAssignableFrom(cls)) {
            throw new AppConfigException(clsName + " is not sub class of " + AppFilter.class.getName());
        }
        filter = ReflectClass.of(cls).newInstance();
        return filter;
    }

    @Bean
    @ConfigurationProperties(prefix = "leap.filter")
    public FilterRegistrationBean appFilter(AppFilter filter, Environment env) {
        FilterRegistrationBean r = new FilterRegistrationBean();
        r.setFilter(filter);
        r.addUrlPatterns("/*");
        r.setName("app-filter");
        r.setOrder(Ordered.LOWEST_PRECEDENCE);

        Map<String, String> initProps = Global.extraInitPropertiesFromEnv();
        if (!initProps.isEmpty()) {
            initProps.forEach((n, v) -> r.addInitParameter(n, v));
        }

        if (!Strings.isEmpty(leap.spring.boot.Global.bp)) {
            r.addInitParameter(AppConfig.INIT_PROPERTY_BASE_PACKAGE, leap.spring.boot.Global.bp);
        }

        if (!Strings.isEmpty(leap.spring.boot.Global.profile)) {
            r.addInitParameter(AppConfig.INIT_PROPERTY_PROFILE, leap.spring.boot.Global.profile);
        }
        log.debug("Register app filter, base-package : {}, profile : {}", Global.bp, Global.profile);
        return r;
    }

    @Bean
    public ServletRegistrationBean bootServlet() {
        ServletRegistrationBean r = new ServletRegistrationBean(new BootServlet(), false);
        r.setName("boot-servlet");
        r.setOrder(Ordered.LOWEST_PRECEDENCE);
        r.setLoadOnStartup(1);
        return r;
    }

    @Bean
    public WebMvcConfigurerAdapter webMvcConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
                converters.add(0, new JsonMessageConverter());
                super.extendMessageConverters(converters);
            }

            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                //todo: registry.addInterceptor(new SpringRequestInterceptor());
                super.addInterceptors(registry);
            }
        };
    }

    private static boolean booted;

    @Bean
    public BeanPostProcessor leapBootingBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                return bean;
            }

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                /*
                    Earlier bean will cause cyclic reference exception, Booting only the ApplicationListener has been created.
                */
                if(bean instanceof ApplicationListener) {
                    if (null != startedServletContext && !booted) {
                        booted = true;
                        boot(startedServletContext);
                    }
                }
                return bean;
            }
        };
    }

    protected static void boot(ServletContext sc) {
        if (AppBootstrap.isInitialized(sc)) {
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
        bootstrap.initialize(sc, Global.extraInitPropertiesFromEnv());
        AppContext.setStandalone(bootstrap.getAppContext());
    }

    public static class BootServlet extends GenericServlet {
        @Override
        public void init(ServletConfig config) throws ServletException {
            if (booted) {
                return;
            }
            if (null != AppContext.getStandalone()) {
                throw new IllegalStateException("Found duplicated standalone context");
            }
            AppContext.setStandalone(Global.context());
        }

        @Override
        public void service(ServletRequest req, ServletResponse res) {
            throw new UnsupportedOperationException();
        }
    }
}