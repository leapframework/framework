/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.action;

import leap.lang.Args;
import leap.lang.ExtensibleBase;
import leap.web.action.*;
import leap.web.format.RequestFormat;
import leap.web.format.ResponseFormat;

import java.util.function.Function;

public class FuncAction extends ExtensibleBase implements Action {

    protected final String                         name;
    protected final Class<?>                       returnType;
    protected final Argument[]                     arguments;
    protected final RequestFormat[]                consumes;
    protected final ResponseFormat[]               produces;
    protected final Function<ActionParams, Object> function;

    public FuncAction(String name, Class<?> returnType, Argument[] arguments,
                      RequestFormat[] consumes, ResponseFormat[] produces, Function<ActionParams, Object> function) {
        Args.notNull(arguments);
        Args.notNull(function);
        this.name       = name == null ? function.toString() : name;
        this.returnType = returnType;
        this.arguments = arguments;
        this.consumes  = consumes;
        this.produces = produces;
        this.function = function;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getReturnType() {
        return returnType;
    }

    @Override
    public Argument[] getArguments() {
        return arguments;
    }

    @Override
    public RequestFormat[] getConsumes() {
        return consumes;
    }

    @Override
    public ResponseFormat[] getProduces() {
        return produces;
    }

    @Override
    public Object execute(ActionContext context, Object[] args) throws Throwable {
        SimpleActionParams params = new SimpleActionParams(arguments, args);

        return function.apply(params);
    }

    @Override
    public String toString() {
        String s = function.toString();
        int lambdaIndex = s.indexOf("$$Lambda");
        if(lambdaIndex > 0) {
            int lastDotIndex = s.lastIndexOf('.',lambdaIndex);
            if(lastDotIndex > 0) {
                s = s.substring(lastDotIndex + 1);
            }
        }
        return s;
    }
}