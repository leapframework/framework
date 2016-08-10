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
import leap.web.exception.BadRequestException;
import leap.web.format.RequestFormat;

import java.util.Map;

public class WrapperArgumentResolver implements ArgumentResolver {

    protected final Argument          argument;
    protected final boolean           requestBody;
    protected final BeanType          beanType;
    protected final WrappedArgument[] wrappedArguments;
    protected final RequestBodyReader requestBodyReader;

    public WrapperArgumentResolver(App app, Argument argument, boolean requestBody, BeanType beanType, WrappedArgument[] wrappedArguments) {
        this.argument          = argument;
        this.requestBody       = requestBody;
        this.beanType          = beanType;
        this.wrappedArguments  = wrappedArguments;
        this.requestBodyReader = getRequestBodyReader(app, argument);
    }

    @Override
    public Object resolveValue(ActionContext context, Argument argument) throws Throwable {
        Object bean = beanType.newInstance();

        //binding the nested arguments(properties) in bean.
        for(WrappedArgument ba : wrappedArguments) {
            ArgumentResolver r = ba.resolver;
            if(null == r) {
                continue;
            }

            Object v = r.resolveValue(context, ba.argument);
            if(null != v) {
                ba.property.setValue(bean, v);
            }
        }

        //The bean itself is a request body argument.
        if(requestBody) {
            Object body = readRequestBody(context);
            if(null != body) {

                if(! (body instanceof Map)) {
                    throw new BadRequestException("The request body must be an object");
                }

                Map map = (Map)body;
                if(!map.isEmpty()) {
                    Beans.setProperties(argument.getBeanType(), bean, map);
                }

            }
        }

        return bean;
    }

    protected Object readRequestBody(ActionContext context) throws Throwable {
        RequestFormat format = context.getRequestFormat();

        if(null != format && format.supportsRequestBody()){
            return format.readRequestBody(context.getRequest());
        }else{
            return requestBodyReader.readRequestBody(context.getRequest(), Object.class, null);
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

    public static final class WrappedArgument {
        public final Argument     argument;
        public final BeanProperty property;
        public final boolean      isMap;

        public ArgumentResolver resolver;

        public WrappedArgument(Argument a) {
            this.argument = a;
            this.property = a.getBeanProperty();
            this.isMap    = a.getBeanProperty().getTypeInfo().isMap();
        }
    }
}