/*
 *
 *  * Copyright 2013 the original author or authors.
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
package leap.web.config;

import leap.core.annotation.Inject;
import leap.core.ioc.BeanList;
import leap.lang.Args;
import leap.web.RequestInterceptor;
import leap.web.action.ActionInterceptor;

public class DefaultWebInterceptors implements WebInterceptors {

    protected @Inject BeanList<RequestInterceptor> requestInterceptors;
    protected @Inject BeanList<ActionInterceptor>  actionInterceptors;

    @Override
    public BeanList<RequestInterceptor> getRequestInterceptors() {
        return requestInterceptors;
    }

    @Override
    public BeanList<ActionInterceptor> getActionInterceptors() {
        return actionInterceptors;
    }

    @Override
    public WebInterceptors add(RequestInterceptor interceptor) {
        Args.notNull(interceptor,"interceptor");
        requestInterceptors.add(interceptor);
        return this;
    }

    @Override
    public WebInterceptors add(ActionInterceptor interceptor) {
        Args.notNull(interceptor,"interceptor");
        actionInterceptors.add(interceptor);
        return this;
    }

    @Override
    public WebInterceptors addFirst(RequestInterceptor interceptor) {
        Args.notNull(interceptor,"interceptor");
        requestInterceptors.addFirst(interceptor);
        return this;
    }

    @Override
    public WebInterceptors addFirst(ActionInterceptor interceptor) {
        Args.notNull(interceptor,"interceptor");
        actionInterceptors.addFirst(interceptor);
        return this;
    }
}