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
import leap.lang.Factory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public class Global {

    public static final String SPRING_PACKAGE_PREFIX = "org.springframework.";
    public static final String LEAP_PACKAGE_PREFIX   = "leap.";

    public static String    bp; //base package.
    public static String[]  bps;
    public static String    profile;
    public static ConfigurableEnvironment env;
    public static ApplicationContext      context;
    public static LeapContext             leap;

    public interface LeapContext {
        AppConfig   config();
        BeanFactory factory();
        AppContext  context();
    }

    private static Boolean springReady = null;

    public static boolean isSpringReady() {
        if(null != springReady) {
            return springReady;
        }

        if(null == context) {
            return false;
        }

        if(context instanceof ConfigurableApplicationContext) {
            if(((ConfigurableApplicationContext) context).isActive()) {
                springReady = true;
                return true;
            }
        }

        return false;
    }

    public static AppContext context() {
        return null == leap ? null : leap.context();
    }

    public static AppConfig config() {
        return null == leap ? null : leap.config();
    }

    public static BeanFactory factory() {
        return null == leap ? null : leap.factory();
    }

    static void start() {
        Starter starter = Factory.newInstance(Starter.class, StandaloneStarter.class);
        starter.start();
    }
}
