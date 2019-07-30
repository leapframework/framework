/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.lang.convert;

import leap.lang.Objects2;
import leap.lang.Out;
import leap.lang.annotation.Name;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.beans.DynaProps;
import leap.lang.reflect.*;
import leap.lang.serialize.Serialize;
import leap.lang.serialize.Serializer;
import leap.lang.serialize.Serializes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings({"rawtypes"})
public class BeanConverter extends AbstractConverter<Object> {

    @Override
    public boolean convertFrom(Object value, Class<?> targetType, Type genericType, Out<Object> out, ConvertContext context) throws Throwable {
        //        if(Modifier.isAbstract(targetType.getModifiers()) || Modifier.isInterface(targetType.getModifiers())){
        //            return false;
        //        }

        if (value instanceof Map) {
            out.set(convertFromMap(targetType, genericType, (Map) value, context));
            return true;
        }

        if (ValueParsable.class.isAssignableFrom(targetType)) {
            Object bean = Reflection.newInstance(targetType);
            ((ValueParsable) bean).parseValue(value);
            out.set(bean);
            return true;
        }

        if (value instanceof Boolean && BooleanParsable.class.isAssignableFrom(targetType)) {
            Object bean = Reflection.newInstance(targetType);
            ((BooleanParsable) bean).parseBoolean((Boolean) value);
            out.set(bean);
            return true;
        }

        if (value instanceof CharSequence && StringParsable.class.isAssignableFrom(targetType)) {
            Object bean = Reflection.newInstance(targetType);
            ((StringParsable) bean).parseString(value.toString());
            out.set(bean);
            return true;
        }

        return false;
    }

    @Override
    public boolean convertTo(Object value, Class<?> targetType, Type genericType, Out<Object> out, ConvertContext context) throws Throwable {
        if (Map.class.isAssignableFrom(targetType)) {
            out.set(convertToMap(value));
            return true;
        }
        return false;
    }

    public void convert(Map map, Object bean, ConvertContext context) {
        doConvert(map, bean, BeanType.of(bean.getClass()), context);
    }

    protected Object convertFromMap(Class<?> targetType, Type genericType, Map map, ConvertContext context) {
        BeanType bt = BeanType.of(targetType);

        @SuppressWarnings("unchecked")
        Object bean = newInstance(context, bt, map);

        if (bean instanceof PreConvertibleFromMap) {
            ((PreConvertibleFromMap) bean).preConvertFromMap(map);
        }

        if (bean instanceof ConvertibleFromMap) {
            ((ConvertibleFromMap) bean).fromMap(map);
            return bean;
        }

        //Check is the concrete type.
        if (bean.getClass() != targetType) {
            bt = BeanType.of(bean.getClass());
        }

        doConvert(map, bean, bt, context);

        if (bean instanceof PostConvertible) {
            ((PostConvertible) bean).postConvert();
        }

        return bean;
    }

    protected void doConvert(Map map, Object bean, BeanType bt, ConvertContext context) {
        boolean             dyna    = bean instanceof DynaProps;
        Map<String, Object> dynaMap = dyna ? new LinkedHashMap(map) : null;

        for (BeanProperty prop : bt.getProperties()) {
            String name = prop.getName();

            boolean multiNames = false;

            for (Annotation a : prop.getAnnotations()) {
                Name nameAnnotation = a.annotationType().getAnnotation(Name.class);

                if (null != nameAnnotation) {
                    String v = (String) ReflectClass.of(a.getClass()).getMethod(nameAnnotation.value()).invoke(a);
                    if (!v.isEmpty()) {
                        name = v;
                        multiNames = true;
                    }
                    break;
                }
            }

            Serializer serializer = Serializes.getSerializer(prop.getAnnotation(Serialize.class));

            for (Object entryObject : map.entrySet()) {
                Entry entry = (Entry) entryObject;

                String key = Objects2.toStringOrEmpty(entry.getKey());

                if (name.equalsIgnoreCase(key) || (multiNames && prop.getName().equalsIgnoreCase(key))) {
                    Object param = entry.getValue();

                    if (null != serializer && null != param && param instanceof String) {
                        param = serializer.tryDeserialize((String) param);
                    }

                    if (dyna) {
                        dynaMap.remove(entry.getKey());
                    }

                    if (prop.isWritable()) {
                        setProperty(context, prop, bean, param);
                        break;
                    }
                }
            }
        }

        //supports special '$' value.
        Object v = map.get("$");
        if (null != v) {
            if (bean instanceof StringParsable) {
                ((StringParsable) bean).parseString(v.toString());
            } else {
                ReflectMethod m = bt.getReflectClass().getMethod("$");
                if (null != m && m.getParameters().length == 1) {
                    m.invoke(bean, Converts.convert(v, m.getParameters()[0].getType(), m.getParameters()[0].getGenericType(), context));
                }
            }
        }

        if (dyna) {
            DynaProps           dynaBean   = (DynaProps) bean;
            Map<String, Object> properties = dynaBean.getDynaProperties();
            if (null != properties) {
                properties.putAll(dynaMap);
            } else {
                dynaMap.forEach(dynaBean::setDynaProperty);
            }
        }
    }

    protected void setProperty(ConvertContext context, BeanProperty bp, Object bean, Object value) {
        if (null != value) {
            Class<?> ptype = bp.getType();
            Class<?> vtype = value.getClass();

            if (ptype.isAssignableFrom(vtype)) {
                value = Converts.convert(value, ptype, bp.getGenericType(), context);
                bp.setValue(bean, value);
                return;
            }

            Map<Class<?>, ReflectMethod> extraSetters = bp.getExtraSetters();
            if (null != extraSetters) {
                ReflectMethod extraSetter = extraSetters.get(vtype);
                if (null != extraSetter) {
                    ReflectParameter p = extraSetter.getParameters()[0];
                    value = Converts.convert(value, p.getType(), p.getGenericType(), context);
                    extraSetter.invoke(bean, value);
                    return;
                }
            }

            try {
                value = Converts.convert(value, ptype, bp.getGenericType(), context);
                bp.setValue(bean, value);
            } catch (ConvertUnsupportedException e) {
                if (null != extraSetters) {
                    for (ReflectMethod extraSetter : extraSetters.values()) {
                        try {
                            ReflectParameter p = extraSetter.getParameters()[0];
                            value = Converts.convert(value, p.getType(), p.getGenericType(), context);
                            extraSetter.invoke(bean, value);
                            return;
                        } catch (ConvertUnsupportedException e1) {

                        }
                    }
                }
                throw e;
            }
        } else {
            bp.setValue(bean, null);
        }
    }

    protected Object newInstance(ConvertContext context, BeanType bt, Map<String, Object> map) {
        ReflectClass cls = bt.getReflectClass();

        ConvertContext.ConcreteTypes types = null == context ? null : context.getConcreteTypes();
        if (null != types) {
            Object instance = types.newInstance(context, bt.getBeanClass(), null, map);
            if (null != instance) {
                return instance;
            }
        }

        if (cls.isAbstract() || cls.isInterface()) {
            throw new ConvertException("Cannot new instance for abstract class or interface '" + bt.getBeanClass().getName() + "'");
        } else if (cls.hasDefaultConstructor()) {
            return bt.newInstance();
        } else {
            ReflectConstructor c  = cls.getConstructors()[0];
            ReflectParameter[] ps = c.getParameters();

            Object[] args = new Object[c.getParameters().length];

            for (int i = 0; i < args.length; i++) {
                ReflectParameter p = ps[i];
                args[i] = Converts.convert(map.get(p.getName()), p.getType(), p.getGenericType(), context);
            }

            return c.newInstance(args);
        }
    }

    protected Map<String, Object> convertToMap(Object bean) {
        BeanType beanType = BeanType.of(bean.getClass());

        Map<String, Object> map = new LinkedHashMap<String, Object>();

        for (BeanProperty prop : beanType.getProperties()) {
            if (prop.isReadable()) {
                map.put(prop.getName(), prop.getValue(bean));
            }
        }

        return map;
    }
}