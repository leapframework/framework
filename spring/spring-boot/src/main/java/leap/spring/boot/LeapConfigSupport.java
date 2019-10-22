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

import leap.core.AppConfigSupport;
import leap.lang.annotation.Name;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Name("spring-boot")
public class LeapConfigSupport implements AppConfigSupport {

    private static final Log log = LogFactory.get(LeapConfigSupport.class);

    static ThreadLocal<Boolean> skip = new ThreadLocal<>();

    @Override
    public Set<String> getPropertyNames() {
        if(Global.context == null) {
            return Collections.EMPTY_SET;
        }

        Environment env = Global.context.getEnvironment();
        if(null == env || !(env instanceof ConfigurableEnvironment)) {
            return Collections.EMPTY_SET;
        }

        Set<String> names = new HashSet<>();
        for (PropertySource<?> propertySource : ((ConfigurableEnvironment) env).getPropertySources()) {
            if (propertySource instanceof EnumerablePropertySource) {
                for (String key : ((EnumerablePropertySource) propertySource).getPropertyNames()) {
                    names.add(key);
                }
            }
        }
        return names;
    }

    @Override
    public String getProperty(String name) {
        if(Global.context == null) {
            return null;
        }

        Environment env = Global.context.getEnvironment();
        if(null == env) {
            return null;
        }

        if(null != skip.get()) {
            return null;
        }

        LeapConfigSupport.skip.set(Boolean.TRUE);
        try {
            return env.getProperty(name);
        }catch (IllegalArgumentException e) {
            if(e.getMessage().contains("Could not resolve placeholder")) {
                log.info("Unable get property '{}' : {}", name, e.getMessage());
            }else {
                log.warn("Error get property '{}' : {}", name, e.getMessage());
            }
            return null;
        }finally{
            LeapConfigSupport.skip.remove();
        }
    }

}
