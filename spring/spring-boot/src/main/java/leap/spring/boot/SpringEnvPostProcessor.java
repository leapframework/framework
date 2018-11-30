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

import leap.lang.Arrays2;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Order(0)
public class SpringEnvPostProcessor implements EnvironmentPostProcessor {

    private static final Log log = LogFactory.get(SpringEnvPostProcessor.class);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication app) {
        addMetaPropertySource(env);

        log.debug("Add leap property source");
        env.getPropertySources().addLast(new SpringPropertySource("leap"));
    }

    private void addMetaPropertySource(ConfigurableEnvironment env) {
        List<Resource> resources = Resources.scan("classpath*:META-INF/application.*").toList();

        final PropertiesPropertySourceLoader propLoader = new PropertiesPropertySourceLoader();
        final YamlPropertySourceLoader       yamlLoader = new YamlPropertySourceLoader();
        try {
            for (Resource resource : resources) {
                if (resource.getFilename().endsWith(".properties")) {
                    addMetaPropertySource(env, resource, propLoader);
                    log.info("Load META-INF properties '{}'", resource.getDescription());
                } else if (resource.getFilename().endsWith("yml") || resource.getFilename().endsWith("yaml")) {
                    addMetaPropertySource(env, resource, yamlLoader);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e.getMessage(), e);
        }
    }

    private void addMetaPropertySource(ConfigurableEnvironment env, Resource resource, PropertySourceLoader loader) throws IOException{
        if(null == env.getActiveProfiles() || env.getActiveProfiles().length == 0)  {
            addMetaPropertySource(env, resource, loader, null);
        }else {
            for(String profile : env.getActiveProfiles()) {
                addMetaPropertySource(env, resource, loader, profile);
            }
        }
    }

    private void addMetaPropertySource(ConfigurableEnvironment env, Resource resource, PropertySourceLoader loader, String profile) throws IOException{
        PropertySource propertySource =
                loader.load(resource.getClasspath(), new SpringResource(resource), profile);

        if(propertySource instanceof EnumerablePropertySource &&
                Arrays2.isEmpty(((EnumerablePropertySource) propertySource).getPropertyNames())) {
            return;
        }

        log.info("Add META-INF property source '{}' with profile '{}'", resource.getDescription(), null == profile ? "" : profile);
        env.getPropertySources().addLast(propertySource);
    }
}
