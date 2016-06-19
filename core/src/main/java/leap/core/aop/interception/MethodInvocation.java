/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.core.aop.interception;

import java.lang.reflect.Method;

public interface MethodInvocation {

    /**
     * Returns the object instance of the intercepted method.
     *
     * <p/>
     * Returns null if the intercepted method is static.
     */
    Object getObject();

    /**
     * Returns the intercepted {@link Method}.
     */
    Method getMethod();

    /**
     * Returns the arguments of the intercepted method.
     *
     * <p/>
     * Returns an empty object array is no arguments.
     */
    Object[] getArguments();

    /**
     * Executes the next interceptor.
     *
     * <p/>
     * Or
     *
     * <p/>
     * Executes the intercepted method if no next interceptor(s).
     */
    Object execute();

}