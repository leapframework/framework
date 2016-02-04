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

import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.web.App;
import leap.web.annotation.RequestBody;
import leap.web.body.RequestBodyReader;
import leap.web.format.RequestFormat;

import java.util.Map;

public class BeanArgumentsResolver implements ArgumentResolver {

    protected final Argument          argument;
    protected final RequestBody       requestBody;
    protected final BeanType          beanType;
    protected final BeanArgument[]    bindingArguments;
    protected final RequestBodyReader requestBodyReader;

    public BeanArgumentsResolver(App app, Argument argument, RequestBody requestBody, BeanType beanType, BeanArgument[] bindingArguments) {
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
            Object v = ba.resolver.resolveValue(context, ba.argument);
            if(null != v) {
                ba.property.setValue(bean, v);
            }
        }

        //The bean itself is request body argument.
        if(null != requestBody) {
            Map body = readRequestBody(context);

            if(null != body && !body.isEmpty()) {
                for(BeanArgument ba : bindingArguments) {
                    if(!ba.argument.isLocationDeclared()) {
                        String name = ba.argument.getName();
                        if(body.containsKey(name)) {
                            Object value = body.get(name);
                            ba.property.setValue(bean, value);
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
            return (Map)format.readRequestBody(context.getRequest(), Map.class, null);
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
        public BeanProperty     property;
        public Argument         argument;
        public ArgumentResolver resolver;
    }
}