/*
 *
 *  * Copyright 2019 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package leap.core.bean;

import leap.core.AppConfig;
import leap.core.AppResource;
import leap.core.AppResources;
import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.config.ConfigUtils;
import leap.core.validation.BeanValidator;
import leap.core.validation.Valid;
import leap.core.validation.ValidationException;
import leap.lang.Classes;
import leap.lang.Strings;
import leap.lang.annotation.Init;
import leap.lang.convert.Converts;
import leap.lang.json.JSON;
import leap.lang.reflect.Reflection;

import java.lang.reflect.Array;
import java.util.*;

public class DefaultBeanCreator implements BeanCreator {

    protected static final Map<String, Map<String, TypeDef>> typeMap = new HashMap<>();

    @Inject
    protected AppConfig config;

    @Inject
    protected BeanFactory factory;

    @Inject
    protected BeanAutowirer autowirer;

    @Inject
    protected BeanValidator beanValidator;

    protected Object standardValidator;

    @Init
    protected void init() {
        if (Classes.isPresent("javax.validation.Validator")) {
            standardValidator = factory.tryGetBean(javax.validation.Validator.class);
        }
        AppResources resources = AppResources.tryGet(config);
        if (null != resources) {
            loadTypes(resources.search("bean_types"));
        } else {
            loadTypes(AppResources.scanMetaAndApp("bean_types"));
        }
    }

    protected void loadTypes(AppResource[] resources) {
        for (AppResource ar : resources) {
            if (ConfigUtils.isJsonOrYaml(ar.getResource())) {
                final Map<String, Object> map = ConfigUtils.decodeMap(ar.getResource());
                map.forEach((k, v) -> {
                    Map<String, TypeDef> types = typeMap.get(k);
                    if (null == types) {
                        types = new HashMap<>();
                        typeMap.put(k, types);
                    }
                    if (!(v instanceof Map)) {
                        throw new IllegalStateException("Invalid type def '" + k + "' at '" + ar.getPath() + "'");
                    }
                    final Map<?, ?> kvs = (Map) v;
                    for (Map.Entry kv : kvs.entrySet()) {
                        types.put(kv.getKey().toString(), new TypeDef(kv.getValue().toString()));
                    }
                });
            }
        }
    }

    @Override
    public <T> T tryCreateBean(Class<T> type, String configurationPrefix) {
        Map<String, Object> props = ConfigUtils.extractMap(config, configurationPrefix);
        if (props.isEmpty()) {
            return null;
        }

        String beanType = (String) props.remove("type");
        if (Strings.isEmpty(beanType)) {
            return null;
        }

        Boolean enabled = (Boolean) props.remove("enabled");

        BeanDef def = new BeanDef();
        def.setType(beanType);
        def.setEnabled(enabled);
        def.setConfig(props);

        return tryCreateBean(type, def);
    }

    @Override
    public <T> T tryCreateBean(Class<T> type, BeanDef def) {
        if (!def.isEnabled()) {
            return null;
        }

        T bean;
        if (!Strings.isEmpty(def.getClassName())) {
            Class<?> c = Classes.tryForName(def.getClassName());
            if (null == c) {
                throw new IllegalStateException("Class '" + def.getClassName() + "' not found");
            }
            bean = createBean(type, c);
        } else if (!Strings.isEmpty(def.getType())) {
            final TypeDef typeDef = getTypeDef(type, def.getType());
            if (null != typeDef) {
                bean = createBean(type, typeDef.getClazz());
            } else {
                bean = factory.tryCreateBean(type, def.getType());
                if (null == bean) {
                    bean = factory.tryGetBean(type, def.getType());
                }
                if (null == bean) {
                    throw new IllegalStateException("Bean '" + def.getType() + "' is not exists for '" + type.getName() + "'");
                }
                if (null != autowirer && !autowirer.isBeanFactoryWrapper()) {
                    autowirer.autowire(bean);
                }
            }
        } else {
            throw new IllegalStateException("Type 'type' or 'className' must be exists at bean definition");
        }

        if (bean instanceof ConfigurableBean) {
            ConfigurableBean cb = (ConfigurableBean) bean;

            Map<String, Object> configMap = def.getConfig();
            if (null == configMap) {
                configMap = Collections.emptyMap();
            }

            Class<?> cc = cb.getConfigurationClass();
            if (null != cc && !Object.class.equals(cc)) {
                if (Map.class.equals(cc)) {
                    cb.initConfiguration(configMap);
                } else if (Map.class.isAssignableFrom(cc)) {
                    Map map = (Map) Reflection.newInstance(cc);
                    map.putAll(configMap);
                    cb.initConfiguration(map);
                } else {
                    Set<String> missingProperties = JSON.resolveMissingProperties(cc, def.getConfig());
                    if (!missingProperties.isEmpty()) {
                        for (String p : missingProperties) {
                            if (p.equals("$") || p.endsWith(".$")) {
                                continue;
                            }
                            throw new IllegalStateException("Invalid property '" + p +
                                    "' at the config of '" + type.getSimpleName() + " : " + def.getType() + "'");
                        }
                    }
                    Object config = Converts.convert(configMap, cc);
                    if (config.getClass().isAnnotationPresent(Valid.class)) {
                        beanValidator.validate(type.getSimpleName() + "(" + def.getType() + ")", config);
                    }
                    if (null != standardValidator) {
                        validateByStandardValidator(type.getSimpleName() + "(" + def.getType() + ")", config);
                    }
                    cb.initConfiguration(config);
                }
            }
        }

        return bean;
    }

    @Override
    public <T> T[] createBeans(Class<T> type, BeanDef[] defs) {
        List<T> list = new ArrayList<>();
        if (null != defs) {
            for (BeanDef def : defs) {
                T bean = tryCreateBean(type, def);
                if (null != bean) {
                    list.add(bean);
                }
            }
        }
        return list.toArray((T[]) Array.newInstance(type, list.size()));
    }

    protected <T> T createBean(Class<T> typeClass, Class<?> beanClass) {
        if (!typeClass.isAssignableFrom(beanClass)) {
            throw new IllegalStateException("Class '" + beanClass.getName() + "' must be sub-class of '" + typeClass.getName() + "'");
        }
        T bean = (T) Reflection.newInstance(beanClass);
        if (null != autowirer) {
            autowirer.autowire(bean);
        } else {
            factory.inject(bean);
        }
        return bean;
    }

    protected void validateByStandardValidator(String name, Object v) {
        final String error = ValidatorUtils.validate(((javax.validation.Validator) standardValidator), name, v);
        if (null != error) {
            throw new ValidationException(error);
        }
    }

    protected TypeDef getTypeDef(Class<?> typeClass, String typeName) {
        final Map<String, TypeDef> types = typeMap.get(typeClass.getName());
        if (null == types || types.isEmpty()) {
            return null;
        } else {
            return types.get(typeName);
        }
    }

    protected static class TypeDef {
        private final String   className;
        private       Class<?> clazz;

        public TypeDef(String className) {
            this.className = className;
        }

        public Class<?> getClazz() {
            if (null == clazz) {
                clazz = Classes.forName(className);
            }
            return clazz;
        }
    }
}