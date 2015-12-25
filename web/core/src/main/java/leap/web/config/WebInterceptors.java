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

import leap.core.ioc.BeanList;
import leap.web.RequestInterceptor;
import leap.web.action.ActionInterceptor;

/**
 * Global interceptor configurator.
 */
public interface WebInterceptors {

    /**
     * Returns the list contains all the {@link RequestInterceptor}.
     */
    BeanList<RequestInterceptor> getRequestInterceptors();

    /**
     * Returns the list contains all the {@link ActionInterceptor}.
     */
    BeanList<ActionInterceptor> getActionInterceptors();

    /**
     * Adds a {@link RequestInterceptor} at the last in request interceptor list.
     */
    WebInterceptors add(RequestInterceptor interceptor);

    /**
     * Adds a {@link ActionInterceptor} at the last in action interceptors list.
     */
    WebInterceptors add(ActionInterceptor interceptor);

    /**
     * Adds a {@link RequestInterceptor} at the first in request interceptor list.
     */
    WebInterceptors addFirst(RequestInterceptor interceptor);

    /**
     * Adds a {@link ActionInterceptor} at the first in action interceptor list.
     */
    WebInterceptors addFirst(ActionInterceptor interceptor);
}
