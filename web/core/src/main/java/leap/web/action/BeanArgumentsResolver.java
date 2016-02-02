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

public class BeanArgumentsResolver implements ArgumentResolver {

    protected final BeanType       beanType;
    protected final BeanArgument[] bindingArguments;

    public BeanArgumentsResolver(BeanType beanType, BeanArgument[] beanArguments) {
        this.beanType = beanType;
        this.bindingArguments = beanArguments;
    }

    @Override
    public Object resolveValue(ActionContext context, Argument argument) throws Throwable {
        Object bean = beanType.newInstance();

        for(BeanArgument ba : bindingArguments) {
            Object v = ba.resolver.resolveValue(context, ba.argument);
            if(null != v) {
                ba.property.setValue(bean, v);
            }
        }

        return bean;
    }

    public static final class BeanArgument {
        public BeanProperty     property;
        public Argument         argument;
        public ArgumentResolver resolver;
    }
}