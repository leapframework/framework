/*
 *
 *  * Copyright 2016 the original author or authors.
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

package leap.lang.jmx;

import com.sun.jmx.mbeanserver.Introspector;
import leap.lang.Buildable;
import leap.lang.Strings;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.reflect.ReflectMethod;
import leap.lang.reflect.ReflectValued;

import javax.management.*;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MBeanBuilder implements Buildable<MBean> {

    private static final MBeanConstructorInfo[]  EMPTY_CONSTRUCTORS  = new ModelMBeanConstructorInfo[0];
    private static final MBeanNotificationInfo[] EMPTY_NOTIFICATIONS = new ModelMBeanNotificationInfo[0];

    protected final Object                         bean;
    protected final Map<String, ReflectValued>     attributes = new LinkedHashMap<>();
    protected final Map<MSignature, ReflectMethod> operations = new LinkedHashMap<>();
    protected final List<MBeanAttributeInfo> attributeInfos   = new ArrayList<>();
    protected final List<MBeanOperationInfo> operationInfos   = new ArrayList<>();

    protected String className;
    protected String description;

    public MBeanBuilder(Object bean) {
        this.bean = bean;
        this.initFromBean();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    protected void initFromBean() {
        //class name
        this.className = bean.getClass().getName();

        //description.
        Managed a = bean.getClass().getAnnotation(Managed.class);
        if(null != a) {
            this.description = a.desc();
        }

        BeanType bt = BeanType.of(bean.getClass());

        //attributes.
        for(BeanProperty bp : bt.getProperties()) {
            a = bp.getAnnotation(Managed.class);
            if(null != a) {
                String name = Strings.firstNotEmpty(a.name(), bp.getName());

                MBeanAttributeInfo ai =
                        new ModelMBeanAttributeInfo(name, bp.getType().getName(), a.desc(), bp.isReadable(), bp.isWritable(), false);

                attributeInfos.add(ai);
                attributes.put(name, bp);
            }
        }

        //operations.
        for(ReflectMethod rm : bt.getReflectClass().getMethods()) {
            if(rm.isGetterMethod() || rm.isSetterMethod() || rm.isSynthetic()) {
                continue;
            }

            if(!rm.isPublic()) {
                continue;
            }

            a = rm.getAnnotation(Managed.class);
            if(null != a) {
                String name = Strings.firstNotEmpty(a.name(), rm.getName());

                MBeanParameterInfo[] parameterInfos =
                        methodSignature(rm.getReflectedMethod());

                MBeanOperationInfo oi = new ModelMBeanOperationInfo(name, a.desc(),
                                                parameterInfos,
                                                rm.getReturnType().getName(),
                                                MBeanOperationInfo.UNKNOWN);

                operationInfos.add(oi);
                operations.put(new MSignature(name, rm.getReflectedMethod()), rm);
            }
        }
    }

    //from jdk.
    private static MBeanParameterInfo[] methodSignature(Method method) {
        final Class<?>[] classes = method.getParameterTypes();
        final Annotation[][] annots = method.getParameterAnnotations();
        return parameters(classes, annots);
    }

    static MBeanParameterInfo[] parameters(Class<?>[] classes,
                                           Annotation[][] annots) {
        final MBeanParameterInfo[] params =
                new MBeanParameterInfo[classes.length];
        assert(classes.length == annots.length);

        for (int i = 0; i < classes.length; i++) {
            Descriptor d = Introspector.descriptorForAnnotations(annots[i]);
            final String pn = "p" + (i + 1);
            params[i] =
                    new MBeanParameterInfo(pn, classes[i].getName(), "", d);
        }

        return params;
    }

    @Override
    public MBean build() {
        MBeanAttributeInfo[] attributeInfoArray = attributeInfos.toArray(new MBeanAttributeInfo[0]);
        MBeanOperationInfo[] operationInfoArray = operationInfos.toArray(new MBeanOperationInfo[0]);

        MBeanInfo mbeanInfo = new MBeanInfo(className,
                                            description,
                                            attributeInfoArray,
                                            EMPTY_CONSTRUCTORS,
                                            operationInfoArray,
                                            EMPTY_NOTIFICATIONS);

        return new MBean(bean, mbeanInfo, attributes, operations);
    }

}
