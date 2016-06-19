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

import leap.core.aop.MethodInterception;
import leap.core.aop.MethodInterceptor;

import java.util.function.Supplier;

public class SimpleMethodInterception implements MethodInterception {

    private final String              className;
    private final String              methodName;
    private final String              methodDesc;
    private final Object              object;
    private final Object[]            arguments;
    private final MethodInterceptor[] interceptors;
    private final Runnable            runnable;
    private final Supplier            supplier;

    public SimpleMethodInterception(String className, String methodName, String methodDesc,
                                    Object object,
                                    MethodInterceptor[] interceptors,
                                    Runnable runnable) {

        this.className = className;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        this.object = object;
        this.arguments = null;
        this.interceptors = interceptors;
        this.runnable = runnable;
        this.supplier = null;
    }

    public SimpleMethodInterception(String className, String methodName, String methodDesc,
                                    Object object, Object[] arguments,
                                    MethodInterceptor[] interceptors,
                                    Runnable runnable) {

        this.className = className;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        this.object = object;
        this.arguments = arguments;
        this.interceptors = interceptors;
        this.runnable = runnable;
        this.supplier = null;
    }

    public SimpleMethodInterception(String className, String methodName, String methodDesc,
                                    Object object,
                                    MethodInterceptor[] interceptors,
                                    Supplier supplier) {

        this.className = className;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        this.object = object;
        this.arguments = null;
        this.interceptors = interceptors;
        this.runnable = null;
        this.supplier = supplier;
    }

    public SimpleMethodInterception(String className, String methodName, String methodDesc,
                                    Object object, Object[] arguments,
                                    MethodInterceptor[] interceptors,
                                    Supplier supplier) {

        this.className = className;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        this.object = object;
        this.arguments = arguments;
        this.interceptors = interceptors;
        this.runnable = null;
        this.supplier = supplier;
    }

    @Override
    public boolean hasReturnValue() {
        return null != supplier;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public Object getObject() {
        return object;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public MethodInterceptor[] getInterceptors() {
        return interceptors;
    }

    @Override
    public Object execute() {
        if(null != supplier) {
            return supplier.get();
        }else{
            runnable.run();
            return null;
        }
    }
}