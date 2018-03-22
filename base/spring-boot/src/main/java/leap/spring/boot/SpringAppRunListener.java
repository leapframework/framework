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

import leap.lang.Classes;
import leap.lang.Collections2;
import leap.lang.Props;
import leap.lang.Strings;
import leap.lang.extension.ExProperties;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.ClassUtils;

import java.util.*;

public class SpringAppRunListener implements SpringApplicationRunListener {

    private static final Log log = LogFactory.get(SpringAppRunListener.class);

    public SpringAppRunListener(SpringApplication application, String[] args) {
        log.debug("Init run listener from main : {}", application.getMainApplicationClass());

        Set<String> bps = new LinkedHashSet<>();

        if(application.getSources().size() == 1) {
            Object source = application.getSources().iterator().next();
            if(source instanceof Class) {
                Class<?> c = (Class)source;
                Global.bp = Classes.getPackageName(c);
                addBasePackages(bps, c);
            }
        }

        if(null != application.getMainApplicationClass()) {
            Class<?> mainClass = application.getMainApplicationClass();
            if(null == Global.bp) {
                Global.bp = Classes.getPackageName(mainClass);
            }
            addBasePackages(bps, mainClass);
        }

        addBasePackagesFromConfigurations(bps);

        Global.bps = bps.toArray(new String[0]);
        log.info("Base package : {}", Global.bp);
    }

    @Override
    public void starting() {}

    @Override
    public void environmentPrepared(ConfigurableEnvironment env) {
        log.debug("Env prepared");

        Global.env = env;

        String[] profiles = env.getActiveProfiles();
        if(profiles.length > 1) {
            throw new IllegalStateException("Leap does not supports multi active profiles: [" + Strings.join(profiles, ',') + "]");
        }

        if(profiles.length == 1) {
            Global.profile = profiles[0];
        }
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        Resources.setResourceLoader(new LeapResourceLoader(context));
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        Global.context = context;
    }

    @Override
    public void finished(ConfigurableApplicationContext context, Throwable exception) {}

    private static void addBasePackages(Set<String> bps, EntityScan a) {
        if(null == a) {
            return;
        }
        Collections2.addAll(bps, a.value());
        Collections2.addAll(bps, a.basePackages());
        for(Class<?> c : a.basePackageClasses()){
            bps.add(ClassUtils.getPackageName(c));
        }
    }

    private static void addBasePackages(Set<String> bps, ComponentScan a) {
        if(null == a) {
            return;
        }
        Collections2.addAll(bps, a.value());
        Collections2.addAll(bps, a.basePackages());
        for(Class<?> c : a.basePackageClasses()){
            bps.add(ClassUtils.getPackageName(c));
        }
    }

    private static void addBasePackages(Set<String> bps, Class<?> c) {
        addBasePackages(bps, c.getAnnotation(EntityScan.class));
        addBasePackages(bps, c.getAnnotation(ComponentScan.class));
    }

    private static void addBasePackagesFromConfigurations(Set<String> bps) {
        String frameworkPackage = ClassUtils.getPackageName(SpringAppRunListener.class);
        for(Resource r : Resources.scan("classpath*:META-INF/spring.factories")) {
            if(r.exists()) {
                ExProperties props = Props.load(r);

                String prop = props.get("org.springframework.boot.autoconfigure.EnableAutoConfiguration");
                if(!Strings.isEmpty(prop)) {
                    String[] classNames = Strings.split(prop, ',');
                    for(String className : classNames) {
                        if(className.startsWith(frameworkPackage) || className.startsWith(Global.SPRING_PACKAGE_PREFIX)) {
                            continue;
                        }
                        Class<?> c = Classes.tryForName(className);
                        if(null != c) {
                            bps.add(ClassUtils.getPackageName(c));
                            addBasePackages(bps, c);
                        }
                    }
                }
            }
        }
    }
}