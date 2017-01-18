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

import leap.lang.exception.ObjectNotFoundException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.reflect.ReflectException;

import javax.management.*;
import java.util.Map;

public class MBean implements DynamicMBean {

    private static final Log log = LogFactory.get(MBean.class);

    protected final Object                      bean;
    protected final MBeanInfo                   mbeanInfo;
    protected final Map<String, MAttribute>     attributes;
    protected final Map<MSignature, MOperation> operations;

    public MBean(Object bean, MBeanInfo mbeanInfo, Map<String, MAttribute> attributes, Map<MSignature, MOperation> operations) {
        this.bean       = bean;
        this.mbeanInfo  = mbeanInfo;
        this.attributes = attributes;
        this.operations = operations;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return mbeanInfo;
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        AttributeList list = new AttributeList();

        if(null != attributes) {
            for(String name : attributes) {
                try {
                    list.add(new Attribute(name, getAttribute(name)));
                } catch (Exception e) {
                    log.error("Error get attribute '" + name + "' : " + e.getMessage(), e);
                }
            }
        }

        return list;
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        AttributeList list = new AttributeList();

        if(null != attributes) {

            for(Attribute a : attributes.asList()) {
                try {
                    setAttribute(a);

                    list.add(a);
                } catch (Exception e) {
                    log.error("Error set attribute '" + a.getName() + "' : " + e.getMessage(), e);
                }
            }

        }

        return list;
    }

    @Override
    public Object getAttribute(String name) throws AttributeNotFoundException, MBeanException, ReflectionException {
        MAttribute a = this.attributes.get(name);
        if(null == a) {
            throw new AttributeNotFoundException("Attribute '" + name + "' not found in '" + bean.getClass() + "'");
        }

        try{
            Object value = a.valued().getValue(bean);

            if(a.isOpen()) {
                value = MConverts.convert(value, a.getOpenType());
            }

            return value;
        }catch(ReflectException e) {
            throw new ReflectionException(e);
        }catch(Exception e) {
            throw new MBeanException(e);
        }
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        String name = attribute.getName();

        MAttribute a = this.attributes.get(name);
        if(null == a) {
            throw new AttributeNotFoundException("Attribute '" + name + "' not found in '" + bean.getClass() + "'");
        }

        //todo : convert the value.

        try{
            a.valued().setValue(bean, attribute.getValue());
        }catch(ReflectException e) {
            throw new ReflectionException(e);
        }catch(Exception e) {
            throw new MBeanException(e);
        }
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        MOperation o = operations.get(new MSignature(actionName, signature));
        if(null == o) {
            throw new MBeanException(new ObjectNotFoundException("Operation '" + actionName + "' not found in '" + bean.getClass() + "'"));
        }

        try{
            Object value = o.getMethod().invoke(bean, params);

            if(o.isOpen()) {
                value = MConverts.convert(value, o.getReturnOpenType());
            }

            return value;
        }catch(ReflectException e) {
            throw new ReflectionException(e);
        }catch(Exception e) {
            throw new MBeanException(e);
        }

    }

}