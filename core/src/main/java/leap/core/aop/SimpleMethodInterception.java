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

import leap.lang.Arrays2;

import java.lang.reflect.Method;
import java.util.function.Supplier;

public class SimpleMethodInterception implements MethodInterception {

    private final Object              object;
    private final Method              method;
    private final Object[]            arguments;
    private final MethodInterceptor[] interceptors;
    private final Runnable            runnable;
    private final Supplier            supplier;

    private int index  = 0;

    public SimpleMethodInterception(Object object,Method method,
                                    MethodInterceptor[] interceptors,
                                    Runnable runnable) {

        this.object = object;
        this.method = method;
        this.arguments = Arrays2.EMPTY_OBJECT_ARRAY;
        this.interceptors = interceptors;
        this.runnable = runnable;
        this.supplier = null;
    }

    public SimpleMethodInterception(Object object, Method method, Object[] arguments,
                                    MethodInterceptor[] interceptors,
                                    Runnable runnable) {

        this.object = object;
        this.method = method;
        this.arguments = arguments;
        this.interceptors = interceptors;
        this.runnable = runnable;
        this.supplier = null;
    }

    public SimpleMethodInterception(Object object,Method method,
                                    MethodInterceptor[] interceptors,
                                    Supplier supplier) {

        this.object = object;
        this.method = method;
        this.arguments = Arrays2.EMPTY_OBJECT_ARRAY;
        this.interceptors = interceptors;
        this.runnable = null;
        this.supplier = supplier;
    }

    public SimpleMethodInterception(Object object, Method method, Object[] arguments,
                                    MethodInterceptor[] interceptors,
                                    Supplier supplier) {

        this.object = object;
        this.method = method;
        this.arguments = arguments;
        this.interceptors = interceptors;
        this.runnable = null;
        this.supplier = supplier;
    }

    public Object getObject() {
        return object;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public MethodInterceptor[] getInterceptors() {
        return interceptors;
    }

    @Override
    public Object execute() throws Throwable {
        if(index < interceptors.length) {
            MethodInterceptor interceptor = interceptors[index];
            index++;
            return interceptor.invoke(this);
        }else{
            return executeMethod();
        }
    }

    @Override
    public Object executeMethod() {
        if(null != supplier) {
            return supplier.get();
        }else{
            runnable.run();
            return null;
        }
    }
}