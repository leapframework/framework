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
import leap.web.action.*;

import java.util.function.Function;

public class FuncAction implements Action {

    protected final Class<?>                       returnType;
    protected final Argument[]                     arguments;
    protected final Function<ActionParams, Object> function;

    public FuncAction(Class<?> returnType, Argument[] arguments, Function<ActionParams, Object> function) {
        Args.notNull(arguments);
        Args.notNull(function);
        this.returnType = returnType;
        this.arguments = arguments;
        this.function = function;
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
    public Object execute(ActionContext context, Object[] args) throws Throwable {
        SimpleActionParams params = new SimpleActionParams(arguments, args);

        return function.apply(params);
    }

}