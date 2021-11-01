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
import leap.lang.Arrays2;
import leap.lang.Strings;
import leap.lang.beans.BeanProperty;
import leap.lang.meta.MNamedWithDescBuilder;
import leap.lang.reflect.ReflectMethod;
import leap.lang.reflect.ReflectParameter;
import leap.web.action.Argument;
import leap.core.doc.annotation.Doc;
import leap.web.api.meta.ApiMetadataBuilder;
import leap.web.api.meta.ApiMetadataContext;
import leap.web.api.meta.ApiMetadataProcessor;
import leap.web.api.meta.model.MApiModelBuilder;
import leap.web.api.meta.model.MApiOperationBuilder;
import leap.web.api.meta.model.MApiParameterBuilder;
import leap.web.api.meta.model.MApiPropertyBuilder;
import java.lang.reflect.Method;

/**
 * Reads doc from annotations.
 */
public class AnnotationDocProcessor implements ApiMetadataProcessor {

    protected @Inject DocResolver docResolver;

    @Override
    public void preProcess(ApiMetadataContext context, ApiMetadataBuilder m) {
        //operations
        m.getPaths().forEach((k, p) -> {
            p.getOperations().forEach(o -> {
                processOperation(context, m, o);
            });
        });

        //models
        m.getModels().forEach((k, model) -> {
            processModel(context, model);
        });
    }

    protected void processOperation(ApiMetadataContext context, ApiMetadataBuilder m, MApiOperationBuilder o) {
        ReflectMethod method = o.getRoute().getAction().getMethod();

        //operation
        if(null != method) {
            Doc doc = method.getAnnotation(Doc.class);
            if(null == doc) {
                doc = searchUp(method);
            }
            if(null != doc) {
                if (!Arrays2.isEmpty(doc.tags())) {
                    for (String tag : doc.tags()) {
                        o.addTag(tag);
                        m.tryAddTag(tag);
                    }
                }
                resolveDoc(context, o, doc);
            }
        }

        //parameters
        o.getParameters().forEach((param) -> processParameter(context, param, method));
    }

    protected void processParameter(ApiMetadataContext context, MApiParameterBuilder param, ReflectMethod method) {
        Argument a = param.getArgument();

        if (null != a) {
            Doc doc = param.getArgument().getAnnotation(Doc.class);
            if (null == doc && null == param.getWrapperArgument() && null != method && null != a.getParameter()) {
                //search from super class or interface.
                doc = searchUp(method, a.getParameter());
            }
            if (null != doc) {
                resolveDoc(context, param, doc);
            }
        }
    }

    protected void processModel(ApiMetadataContext context, MApiModelBuilder model) {
        for(Class<?> c : model.getJavaTypes()) {
            Doc doc = c.getAnnotation(Doc.class);
            if (null != doc) {
                resolveDoc(context, model, doc);
            }
        }

        model.getProperties().forEach((k,p) -> {
            processProperty(context, p);
        });
    }

    protected void processProperty(ApiMetadataContext context, MApiPropertyBuilder p) {
        BeanProperty bp = p.getBeanProperty();
        if(null != bp) {
            Doc doc = bp.getAnnotation(Doc.class);
            if(null != doc) {
                resolveDoc(context, p, doc);
            }
        }
    }

    protected Doc searchUp(ReflectMethod m) {
        Class<?> c = m.getReflectedMethod().getDeclaringClass();

        for(Class<?> ic : c.getInterfaces()) {
            try {
                Method im = ic.getMethod(m.getName(), m.getReflectedMethod().getParameterTypes());

                return im.getAnnotation(Doc.class);
            } catch (NoSuchMethodException e) {
                //do nothing.
            }
        }

        return null;
    }

    protected Doc searchUp(ReflectMethod m, ReflectParameter p) {
        Class<?> c = m.getReflectedMethod().getDeclaringClass();

        for(Class<?> ic : c.getInterfaces()) {
            try {
                Method im = ic.getMethod(m.getName(), m.getReflectedMethod().getParameterTypes());

                return im.getParameters()[p.getIndex()-1].getAnnotation(Doc.class);
            } catch (NoSuchMethodException e) {
                //do nothing.
            }
        }

        return null;
    }

    protected void resolveDoc(ApiMetadataContext context, MNamedWithDescBuilder o, Doc a) {
        String summary = Strings.firstNotEmpty(a.summary(), a.value());
        if(Strings.isEmpty(o.getSummary()) && !Strings.isEmpty(summary)) {
            o.setSummary(docResolver.resolveDesc(summary));
        }

        String desc = a.desc();
        if(Strings.isEmpty(o.getDescription()) && !Strings.isEmpty(desc)) {
            o.setDescription(docResolver.resolveDesc(desc));
        }
    }
}