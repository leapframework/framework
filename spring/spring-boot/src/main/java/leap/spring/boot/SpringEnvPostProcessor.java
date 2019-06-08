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
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.lang.reflect.Reflection;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Order(0)
public class SpringEnvPostProcessor implements EnvironmentPostProcessor {

    private static final Log log = LogFactory.get(SpringEnvPostProcessor.class);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication app) {
        addMetaPropertySources(env);

        log.debug("Add leap property source");
        env.getPropertySources().addLast(new SpringPropertySource("leap"));
    }

    private void addMetaPropertySources(ConfigurableEnvironment env) {
        List<Resource> resources = scanConfigurations(null, "classpath*:META-INF/");
        if (resources.isEmpty()) {
            return;
        }
        addPropertySources(env, resources);
    }

    private List<Resource> scanConfigurations(Resource root, String prefix) {
        List<Resource> resources = new ArrayList<>();
        resources.addAll(scanResources(root, prefix + "application.*"));
        resources.addAll(scanResources(root, prefix + "application-*.*"));
        return resources;
    }

    private List<Resource> scanResources(Resource root, String location) {
        return null == root ? Resources.scan(location).toList() : Resources.scan(root, location).toList();
    }

    private void addPropertySources(ConfigurableEnvironment env, List<Resource> resources) {
        final PropertiesPropertySourceLoader propLoader = new PropertiesPropertySourceLoader();
        final YamlPropertySourceLoader       yamlLoader = new YamlPropertySourceLoader();
        try {
            for (Resource resource : resources) {
                if (resource.getFilename().endsWith(".properties")) {
                    addPropertySource(env, resource, propLoader, false);
                    log.info("Load properties '{}'", resource.getDescription());
                } else if (resource.getFilename().endsWith("yml") || resource.getFilename().endsWith("yaml")) {
                    addPropertySource(env, resource, yamlLoader, false);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e.getMessage(), e);
        }
    }

    private void addPropertySource(ConfigurableEnvironment env, Resource resource, PropertySourceLoader loader, boolean frist) throws IOException {
        if (null == env.getActiveProfiles() || env.getActiveProfiles().length == 0) {
            addPropertySource(env, resource, loader, null, frist);
        } else {
            for (String profile : env.getActiveProfiles()) {
                addPropertySource(env, resource, loader, profile, frist);
            }
        }
    }

    private void addPropertySource(ConfigurableEnvironment env, Resource resource, PropertySourceLoader loader, String profile, boolean first) throws IOException {
        if (null != profile) {
            String filenameOnly = Paths.getFileNameWithoutExtension(resource.getFilename());
            int    index        = filenameOnly.lastIndexOf('-');
            if (index > 0) {
                String profileOfFile = filenameOnly.substring(index + 1);
                if (!Strings.equals(profileOfFile, profile)) {
                    return;
                }
            }
        }

        PropertySource propertySource;
        Method         load = Reflection.getMethod(loader.getClass(), "load");
        try {
            if (SpringBootUtils.is1x()) {
                propertySource = (PropertySource) load.invoke(loader, resource.getDescription(), new SpringResource(resource), null);
            } else {
                List<PropertySource> list = (List<PropertySource>) load.invoke(loader, resource.getDescription(), new SpringResource(resource));
                if(null != list && list.size() > 1) {
                    log.error("Load multi {} property sources, must be zero or one", list.size());
                }
                propertySource = null == list || list.isEmpty() ? null : list.get(0);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        if (null == propertySource) {
            return;
        }

        if (propertySource instanceof EnumerablePropertySource &&
                Arrays2.isEmpty(((EnumerablePropertySource) propertySource).getPropertyNames())) {
            return;
        }

        log.info("Add property source '{}' with profile '{}'", resource.getDescription(), null == profile ? "" : profile);
        if (first) {
            env.getPropertySources().addFirst(propertySource);
        } else {
            env.getPropertySources().addLast(propertySource);
        }
    }
}
