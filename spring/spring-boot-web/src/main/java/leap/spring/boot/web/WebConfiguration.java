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
import leap.core.AppContext;
import leap.core.BeanFactory;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.spring.boot.Global;
import leap.web.AppFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class WebConfiguration {

    private static final Log log = LogFactory.get(WebConfiguration.class);

    static AppFilter filter = new AppFilter();

    static {
        Global.leap = new Global.LeapContext() {
            @Override
            public AppConfig config() {
                return filter.config();
            }

            @Override
            public BeanFactory factory() {
                return filter.factory();
            }

            @Override
            public AppContext context() {
                return filter.context();
            }
        };
    }

    @Bean
    public FilterRegistrationBean appFilter() {
        FilterRegistrationBean r = new FilterRegistrationBean();
        r.setFilter(filter);
        r.addUrlPatterns("/*");
        r.setName("app-filter");
        r.setOrder(Ordered.LOWEST_PRECEDENCE);

        if(!Strings.isEmpty(leap.spring.boot.Global.bp)) {
            r.addInitParameter(AppConfig.INIT_PROPERTY_BASE_PACKAGE, leap.spring.boot.Global.bp);
        }

        if(!Strings.isEmpty(leap.spring.boot.Global.profile)) {
            r.addInitParameter(AppConfig.INIT_PROPERTY_PROFILE, leap.spring.boot.Global.profile);
        }

        log.debug("Register app filter, base-package : {}, profile : {}", Global.bp, Global.profile);

        return r;
    }

}