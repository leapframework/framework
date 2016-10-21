/*
 *
 *  * Copyright 2016 the original author or authors.
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

package tested.aop;

import leap.core.aop.MethodInterceptor;
import leap.core.aop.MethodInvocation;

public class TInterceptor implements MethodInterceptor {

    private int interceptedCount = 0;
    private MethodInvocation lastInvocation;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        interceptedCount++;

        lastInvocation = invocation;

        return invocation.execute();
    }

    public int getInterceptedCount() {
        return interceptedCount;
    }

    public void resetInterceptedCount() {
        interceptedCount = 0;
    }

    public MethodInvocation getLastInvocation() {
        return lastInvocation;
    }

    public void resetLastInvocation() {
        this.lastInvocation = null;
    }
}