/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.api.doc;

import leap.core.annotation.Inject;
import leap.core.doc.DocResolver;
import leap.lang.beans.BeanProperty;
import leap.lang.reflect.ReflectMethod;
import leap.lang.reflect.ReflectParameter;
import leap.web.action.Argument;
import leap.core.doc.annotation.Desc;
import leap.web.api.meta.ApiMetadataBuilder;
import leap.web.api.meta.ApiMetadataContext;
import leap.web.api.meta.ApiMetadataProcessor;
import leap.web.api.meta.model.MApiModelBuilder;
import leap.web.api.meta.model.MApiOperationBuilder;
import leap.web.api.meta.model.MApiParameterBuilder;
import leap.web.api.meta.model.MApiPropertyBuilder;

import java.lang.reflect.Method;

/**
 * Reads desc from annotations.
 */
public class AnnotationDescProcessor implements ApiMetadataProcessor {

    protected @Inject DocResolver docResolver;

    @Override
    public void preProcess(ApiMetadataContext context, ApiMetadataBuilder m) {
        //operations
        m.getPaths().forEach((k, p) -> {
            p.getOperations().forEach(o -> {
                processOperation(context, o);
            });
        });

        //models
        m.getModels().forEach((k, model) -> {
            processModel(context, model);
        });
    }

    protected void processOperation(ApiMetadataContext context, MApiOperationBuilder o) {
        ReflectMethod method = o.getRoute().getAction().getMethod();

        //operation
        if(null != method) {
            Desc desc = method.getAnnotation(Desc.class);
            if(null == desc) {
                desc = searchUp(method);
            }
            if(null != desc) {
                o.setDescription(resolveDescription(context, desc));
            }
        }

        //parameters
        o.getParameters().forEach((param) -> processParameter(context, param, method));
    }

    protected void processParameter(ApiMetadataContext context, MApiParameterBuilder param, ReflectMethod method) {
        Argument a = param.getArgument();

        if (null != a) {
            Desc desc = param.getArgument().getAnnotation(Desc.class);
            if (null == desc && null == param.getWrapperArgument() && null != method && null != a.getParameter()) {
                //search from super class or interface.
                desc = searchUp(method, a.getParameter());
            }
            if (null != desc) {
                param.setDescription(resolveDescription(context, desc));
            }
        }
    }

    protected void processModel(ApiMetadataContext context, MApiModelBuilder model) {
        Class<?> c = model.getJavaType();
        if(null != c) {
            Desc desc = c.getAnnotation(Desc.class);
            if(null != desc) {
                model.setDescription(resolveDescription(context, desc));
            }
        }

        model.getProperties().forEach((k,p) -> {
            processProperty(context, p);
        });
    }

    protected void processProperty(ApiMetadataContext context, MApiPropertyBuilder p) {
        BeanProperty bp = p.getBeanProperty();
        if(null != bp) {
            Desc desc = bp.getAnnotation(Desc.class);
            if(null != desc) {
                p.setDescription(resolveDescription(context, desc));
            }
        }
    }

    protected Desc searchUp(ReflectMethod m) {
        Class<?> c = m.getReflectedMethod().getDeclaringClass();

        for(Class<?> ic : c.getInterfaces()) {
            try {
                Method im = ic.getMethod(m.getName(), m.getReflectedMethod().getParameterTypes());

                return im.getAnnotation(Desc.class);
            } catch (NoSuchMethodException e) {
                //do nothing.
            }
        }

        return null;
    }

    protected Desc searchUp(ReflectMethod m, ReflectParameter p) {
        Class<?> c = m.getReflectedMethod().getDeclaringClass();

        for(Class<?> ic : c.getInterfaces()) {
            try {
                Method im = ic.getMethod(m.getName(), m.getReflectedMethod().getParameterTypes());

                return im.getParameters()[p.getIndex()-1].getAnnotation(Desc.class);
            } catch (NoSuchMethodException e) {
                //do nothing.
            }
        }

        return null;
    }

    protected String resolveDescription(ApiMetadataContext context, Desc a) {
        return docResolver.resolveDesc(a.value());
    }
}