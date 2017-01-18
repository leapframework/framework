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

import leap.lang.Buildable;
import leap.lang.Strings;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.reflect.ReflectMethod;
import leap.lang.reflect.ReflectParameter;

import javax.management.*;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.openmbean.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MBeanBuilder implements Buildable<MBean> {

    private static final MBeanConstructorInfo[]  EMPTY_CONSTRUCTORS  = new ModelMBeanConstructorInfo[0];
    private static final MBeanNotificationInfo[] EMPTY_NOTIFICATIONS = new ModelMBeanNotificationInfo[0];

    protected final Object                      bean;
    protected final Map<String, MAttribute>     attributes     = new LinkedHashMap<>();
    protected final Map<MSignature, MOperation> operations     = new LinkedHashMap<>();
    protected final List<MBeanAttributeInfo>    attributeInfos = new ArrayList<>();
    protected final List<MBeanOperationInfo>    operationInfos = new ArrayList<>();

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
                String desc = Strings.firstNotEmpty(a.desc(), bp.getName()); // description can't be null or empty.

                boolean readable = false;
                boolean writable = false;

                if(bp.isField() && bp.getReflectField().isAnnotationPresent(Managed.class)) {
                    readable = bp.isReadable();
                    writable = bp.isWritable();
                }else {
                    if(bp.hasGetter() && bp.getGetter().isAnnotationPresent(Managed.class)) {
                        readable = true;
                    }

                    if(bp.hasSetter() && bp.getSetter().isAnnotationPresent(Managed.class)) {
                        writable = true;
                    }
                }

                OpenType type = MBeanTypes.of(bp.getTypeInfo());

                OpenMBeanAttributeInfoSupport ai =
                        new OpenMBeanAttributeInfoSupport(name, desc, type, readable, writable, false);

                attributeInfos.add(ai);
                attributes.put(name, new MAttribute(ai, bp));
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
                String desc = Strings.firstNotEmpty(a.desc(), rm.getName());

                OpenMBeanParameterInfo[] parameterInfos = params(rm);

                OpenType returnType = MBeanTypes.of(rm.getReturnType(), null);

                OpenMBeanOperationInfoSupport oi =
                        new OpenMBeanOperationInfoSupport(name, desc, parameterInfos, returnType, MBeanOperationInfo.UNKNOWN);

                operationInfos.add(oi);
                operations.put(new MSignature(name, rm.getReflectedMethod()), new MOperation(oi, rm));
            }
        }
    }

    private static OpenMBeanParameterInfo[] params(ReflectMethod rm) {
        final OpenMBeanParameterInfo[] params = new OpenMBeanParameterInfo[rm.getParameters().length];

        for (int i=0;i<params.length;i++) {
            ReflectParameter p = rm.getParameters()[i];

            params[i] = new OpenMBeanParameterInfoSupport(p.getName(), p.getName(),
                                                          MBeanTypes.of(p.getType(),p.getGenericType()));
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
