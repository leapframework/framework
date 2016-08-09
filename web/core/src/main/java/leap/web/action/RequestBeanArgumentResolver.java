/*
 * Copyright 2016 the original author or authors.
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
package leap.web.action;

import leap.lang.Beans;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.convert.Converts;
import leap.web.App;
import leap.web.body.RequestBodyReader;
import leap.web.format.RequestFormat;

import java.util.Map;

public class RequestBeanArgumentResolver implements ArgumentResolver {

    protected final Argument          argument;
    protected final boolean           requestBody;
    protected final BeanType          beanType;
    protected final BeanArgument[]    bindingArguments;
    protected final RequestBodyReader requestBodyReader;

    public RequestBeanArgumentResolver(App app, Argument argument, boolean requestBody, BeanType beanType, BeanArgument[] bindingArguments) {
        this.argument          = argument;
        this.requestBody       = requestBody;
        this.beanType          = beanType;
        this.bindingArguments  = bindingArguments;
        this.requestBodyReader = getRequestBodyReader(app, argument);
    }

    @Override
    public Object resolveValue(ActionContext context, Argument argument) throws Throwable {
        Object bean = beanType.newInstance();

        //binding the nested arguments(properties) in bean.
        for(BeanArgument ba : bindingArguments) {
            ArgumentResolver r = ba.resolver;
            if(null == r) {
                continue;
            }

            Object v = r.resolveValue(context, ba.argument);
            if(null != v) {
                ba.property.setValue(bean, v);
            }
        }

        //The bean itself is request body argument.
        if(requestBody) {
            Map body = readRequestBody(context);

            if(null != body) {
                for(BeanArgument ba : bindingArguments) {
                    Argument a = ba.argument;

                    if(ba.body) {

                        if(ba.isMap) {
                            ba.property.setValue(bean, body);
                        }else{
                            Object v = a.getBeanType().newInstance();

                            if(!body.isEmpty()) {
                                Beans.setProperties(a.getBeanType(), v, body);
                            }

                            ba.property.setValue(bean, v);
                        }

                        continue;
                    }

                    if(body.isEmpty()) {
                        continue;
                    }

                    if(!a.isLocationDeclared()) {
                        String name = a.getName();
                        if(body.containsKey(name)) {

                            BeanProperty bp = ba.property;

                            Object value = body.get(name);
                            if(null == value) {
                                bp.setValue(bean, null);
                            }else{
                                bp.setValue(bean, Converts.convert(value, bp.getType(), bp.getGenericType()));
                            }

                        }
                    }
                }
            }
        }

        return bean;
    }

    protected Map readRequestBody(ActionContext context) throws Throwable{
        RequestFormat format = context.getRequestFormat();

        if(null != format && format.supportsRequestBody()){
            return (Map)format.readRequestBody(context.getRequest());
        }else{
            return (Map)requestBodyReader.readRequestBody(context.getRequest(), Map.class, null);
        }
    }

    protected RequestBodyReader getRequestBodyReader(App app, Argument argument) {
        for(RequestBodyReader reader : app.factory().getBeans(RequestBodyReader.class)){
            if(reader.canReadRequestBody(argument.getType(), argument.getGenericType())){
                return reader;
            }
        }
        return null;
    }

    public static final class BeanArgument {
        public final BeanProperty     property;
        public final boolean          isMap;

        public Argument         argument;
        public boolean          body;
        public ArgumentResolver resolver;

        public BeanArgument(BeanProperty p) {
            this.property = p;
            this.isMap    = p.getTypeInfo().isMap();
        }
    }
}