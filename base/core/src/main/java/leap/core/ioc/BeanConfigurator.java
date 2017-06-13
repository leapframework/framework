/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.core.ioc;

import leap.core.AppConfig;
import leap.core.annotation.ConfigProperty;
import leap.core.annotation.Configurable;
import leap.core.config.dyna.*;
import leap.lang.Classes;
import leap.lang.Collections2;
import leap.lang.Strings;
import leap.lang.Types;
import leap.lang.beans.BeanCreationException;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.convert.Converts;
import leap.lang.json.JSON;
import leap.lang.reflect.ReflectField;
import leap.lang.reflect.ReflectMethod;
import leap.lang.reflect.ReflectParameter;
import leap.lang.reflect.ReflectValued;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;

public class BeanConfigurator {

    private final AppConfig config;

    public BeanConfigurator(AppConfig config) {
        this.config = config;
    }

    private String keyPrefix(String keyPrefix) {
        if(!Strings.isEmpty(keyPrefix)) {
            char lastChar = keyPrefix.charAt(keyPrefix.length() - 1);
            if(Character.isLetter(lastChar) || Character.isDigit(lastChar)) {
                keyPrefix = keyPrefix + ".";
            }
        }else{
            keyPrefix = "";
        }
        return keyPrefix;
    }

    public boolean configure(Object[] args, ReflectParameter p, String keyPrefix) {
        keyPrefix = keyPrefix(keyPrefix);

        if(Property.class.isAssignableFrom(p.getType())) {
            doBeanConfigure(args, p, keyPrefix, p.getAnnotation(ConfigProperty.class));
            return true;
        }

        ConfigProperty a = p.getAnnotation(ConfigProperty.class);
        if(null != a) {
            doBeanConfigure(args, p, keyPrefix, a);
            return true;
        }

        return false;
    }

    public void configure(Object bean, BeanType bt, String keyPrefix) {
        keyPrefix = keyPrefix(keyPrefix);

        Set<ReflectField> done = new HashSet<>();

        for(BeanProperty bp : bt.getProperties()){
            Configurable.Nested nested = bp.getAnnotation(Configurable.Nested.class);
            if(null != nested) {
                String nestedPrefix = nested.prefix();

                if(Strings.isEmpty(nestedPrefix)) {
                    nestedPrefix = Strings.lowerUnderscore(bp.getName());
                    if(nestedPrefix.endsWith("_config")) {
                        nestedPrefix = Strings.removeEnd(nestedPrefix, "_config");
                    }
                }

                String fullKeyPrefix = keyPrefix(keyPrefix + nestedPrefix);
                Object nestedBean    = bp.getValue(bean);
                BeanType nestedType;

                if(null == nestedBean) {
                    nestedType = BeanType.of(bp.getType());
                    nestedBean = nestedType.newInstance();
                    bp.setValue(bean, nestedBean);
                }else{
                    nestedType = BeanType.of(nestedBean.getClass());
                }

                configure(nestedBean, nestedType, fullKeyPrefix);
                continue;
            }

            ConfigProperty a = bp.getAnnotation(ConfigProperty.class);
            if(!Property.class.isAssignableFrom(bp.getType()) && null == a) {
                continue;
            }

            if(bp.isWritable()) {
                doBeanConfigure(bean, bp, keyPrefix, a);
            }else{
                ReflectField rf = bp.getReflectField();
                if(null == rf) {
                    throw new BeanCreationException("The property '" + bp.getName() + "' in class '" + bt.getReflectClass() + "' is not writable!");
                }
                doBeanConfigure(bean, rf, keyPrefix, a);
            }

            if(null != bp.getReflectField()) {
                done.add(bp.getReflectField());
            }
        }

        for(ReflectField field : bt.getReflectClass().getFields()) {
            if(done.contains(field)) {
                continue;
            }

            if(Property.class.isAssignableFrom(field.getType())) {
                doBeanConfigure(bean, field, keyPrefix, field.getAnnotation(ConfigProperty.class));
                continue;
            }

            ConfigProperty a = field.getAnnotation(ConfigProperty.class);
            if(null == a) {
                continue;
            }

            doBeanConfigure(bean, field, keyPrefix, a);
        }

        for(ReflectMethod m : bt.getReflectClass().getMethods()) {
            if(m.isSetterMethod() || m.isGetterMethod()) {
                continue;
            }

            if(m.getParameters().length == 1) {
                ConfigProperty a = m.getAnnotation(ConfigProperty.class);
                if(null != a) {
                    doBeanConfigure(bean, new MethodReflectValued(bean, m), keyPrefix, a);
                }
            }
        }

        done.clear();
    }

    protected void doBeanConfigure(Object bean, ReflectValued v, String keyPrefix, ConfigProperty a) {

        String defaultValue = null == a ? null : a.defaultValue();

        if(null != a) {
            String[] keys = a.key();
            if(keys.length == 0) {
                keys = a.value();
            }

            if(keys.length > 0) {
                for(String key : keys) {
                    if(doBeanConfigureByKey(bean, v, keyPrefix + key, defaultValue)) {
                        break;
                    }
                }
                return;
            }
        }

        if(doBeanConfigureByKey(bean, v, keyPrefix + v.getName(), defaultValue)) {
            return;
        }

        if(doBeanConfigureByKey(bean, v, keyPrefix + Strings.lowerHyphen(v.getName()), defaultValue)) {
            return;
        }

        if(doBeanConfigureByKey(bean, v, keyPrefix + Strings.lowerUnderscore(v.getName()), defaultValue)) {
            return;
        }

    }

    protected boolean doBeanConfigureByKey(Object bean, ReflectValued v, String key, String defaultValue) {
        if(Property.class.isAssignableFrom(v.getType())) {
            doBeanConfigureDynaProperty(bean, v, key, defaultValue);
            return true;
        }

        if(v.getType().isArray()) {
            String[] array = config.getArrayProperty(key);

            if((null == array || array.length == 0) && !Strings.isEmpty(defaultValue)) {
                array = Converts.convert(defaultValue, String[].class);
            }

            if(null != array) {
                v.setValue(bean, array);
                return true;
            }
        }

        if(List.class.equals(v.getType())) {
            String[] array = config.getArrayProperty(key);

            if((null == array || array.length == 0) && !Strings.isEmpty(defaultValue)) {
                array = Converts.convert(defaultValue, String[].class);
            }

            if(null != array) {
                List<String> list = new ArrayList<>();
                Collections2.addAll(list, array);
                v.setValue(bean, list);
                return true;
            }
        }

        if(Set.class.equals(v.getType())) {
            String[] array = config.getArrayProperty(key);

            if((null == array || array.length == 0) && !Strings.isEmpty(defaultValue)) {
                array = Converts.convert(defaultValue, String[].class);
            }

            if(null != array) {
                Set<String> set = new LinkedHashSet<>();
                Collections2.addAll(set, array);
                v.setValue(bean, set);
                return true;
            }
        }

        String prop = config.getProperty(key);

        if(Strings.isEmpty(prop) && !Strings.isEmpty(defaultValue)) {
            prop = defaultValue;
        }

        if(null != prop) {
            if(prop.length() > 0) {
                try {
                    Object value;
                    if(Classes.isSimpleValueType(v.getType())) {
                        value = Converts.convert(prop, v.getType(), v.getGenericType());
                    }else{
                        value = JSON.decode(prop, v.getType());
                    }
                    v.setValue(bean, value);
                } catch (Exception e) {
                    throw new BeanCreationException("Error configure property '" + bean.getClass().getName() + "#" + v.getName() +
                            "' using config key '" + key + "', " + e.getMessage(), e);
                }
            }
            return true;
        }

        return false;
    }

    protected void doBeanConfigureDynaProperty(Object bean, ReflectValued v, String key, String defaultValue) {

        Class<?> type  = v.getType();
        Property value = (Property)v.getValue(bean);

        if(null != value) {
            config.bindDynaProperty(key, type, value);
            return;
        }

        if(type.equals(StringProperty.class)) {

            value = config.getDynaProperty(key);

        }else if(type.equals(IntegerProperty.class)) {

            value = config.getDynaIntegerProperty(key);

        }else if(type.equals(LongProperty.class)) {

            value = config.getDynaLongProperty(key);

        }else if(type.equals(BooleanProperty.class)) {

            value = config.getDynaBooleanProperty(key);

        }else if(type.equals(DoubleProperty.class)) {

            value = config.getDynaDoubleProperty(key);

        }else if(type.equals(Property.class)){

            Class<?> valueType = Types.getActualTypeArgument(v.getGenericType());

            value = config.getDynaProperty(key, valueType);

        }else{
            throw new IllegalStateException("Not supported property type '" + type + "'");
        }

        if(null != value) {
            if(value.isNull()){
                value.convert(defaultValue);
            }
            
            v.setValue(bean, value);
        }else if(!Strings.isEmpty(defaultValue)) {
            v.setValue(bean, Converts.convert(defaultValue, v.getType(), v.getGenericType()));
        }
    }

    protected final class MethodReflectValued implements ReflectValued {

        private final Object           o;
        private final ReflectMethod    m;
        private final ReflectParameter p;

        public MethodReflectValued(Object o, ReflectMethod m) {
            this.o = o;
            this.m = m;
            this.p = m.getParameters()[0];
        }

        @Override
        public String getName() {
            return p.getName();
        }

        @Override
        public Class<?> getType() {
            return p.getType();
        }

        @Override
        public Type getGenericType() {
            return p.getGenericType();
        }

        @Override
        public Annotation[] getAnnotations() {
            return p.getAnnotations();
        }

        @Override
        public Object getValue(Object bean) {
            return null;
        }

        @Override
        public void setValue(Object bean, Object value) {
            m.invoke(o, value);
        }
    }
    
}
