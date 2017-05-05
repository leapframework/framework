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

import leap.lang.reflect.ReflectMethod;
import leap.lang.reflect.ReflectParameter;
import leap.web.action.Argument;
import leap.web.api.annotation.Desc;
import leap.web.api.meta.ApiMetadataBuilder;
import leap.web.api.meta.ApiMetadataContext;
import leap.web.api.meta.ApiMetadataProcessor;
import leap.web.api.meta.model.MApiOperationBuilder;
import leap.web.api.meta.model.MApiParameterBuilder;

import java.lang.reflect.Method;

/**
 * Reads desc from annotations.
 */
public class AnnotationDescProcessor implements ApiMetadataProcessor {

    @Override
    public void preProcess(ApiMetadataContext context, ApiMetadataBuilder m) {
        //parameters
        m.getPaths().forEach((k, p) -> {
            p.getOperations().forEach(o -> {
                processParameters(context, o);
            });
        });

        //models
//        m.getModels().forEach((k, model) -> {
//            processModel(context, model);
//        });
    }

    protected void processParameters(ApiMetadataContext context, MApiOperationBuilder o) {
        ReflectMethod method = o.getRoute().getAction().getMethod();

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
                param.setDescription(resolveDescription(desc));
            }
        }
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

    protected String resolveDescription(Desc a) {
        String s = a.value();

        //todo : message key

        //todo : external file

        return s;
    }
}