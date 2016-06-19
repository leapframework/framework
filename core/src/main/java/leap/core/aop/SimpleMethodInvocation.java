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

package leap.core.aop;

import java.lang.reflect.Method;

public final class SimpleMethodInvocation implements MethodInvocation {
    private final MethodInterception  interception;
    private final MethodInterceptor[] interceptors;

    private int index = 0;

    public SimpleMethodInvocation(MethodInterception interception) {
        this.interception = interception;
        this.interceptors = interception.getInterceptors();
    }

    @Override
    public Method getMethod() {
        return null;
    }

    @Override
    public Object getObject() {
        return interception.getObject();
    }

    @Override
    public Object[] getArguments() {
        return interception.getArguments();
    }

    @Override
    public Object execute() throws Throwable {
        if(index < interceptors.length) {
            MethodInterceptor interceptor = interceptors[index];
            index++;
            return interceptor.invoke(this);
        }else{
            return interception.execute();
        }
    }
}
