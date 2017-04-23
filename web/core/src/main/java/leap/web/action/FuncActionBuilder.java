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

import leap.lang.Assert;
import leap.lang.Buildable;
import leap.lang.Builders;
import leap.lang.ExtensibleBase;
import leap.lang.exception.ObjectExistsException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FuncActionBuilder extends ExtensibleBase implements Buildable<FuncAction> {

    protected String                         name;
    protected Class<?>                       returnType;
    protected List<ArgumentBuilder>          arguments = new ArrayList<>();
    protected Function<ActionParams, Object> function;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

    public List<ArgumentBuilder> getArguments() {
        return arguments;
    }

    public FuncActionBuilder addArgument(ArgumentBuilder argument) throws ObjectExistsException{
        Assert.notNull(argument);

        for(ArgumentBuilder exists : arguments) {
            if(exists.getName().equalsIgnoreCase(argument.getName())) {
                throw new ObjectExistsException("The argument '" + argument.getName() + "' already exists!");
            }
        }

        arguments.add(argument);
        return this;
    }

    public Function<ActionParams, Object> getFunction() {
        return function;
    }

    public void setFunction(Function<ActionParams, Object> function) {
        this.function = function;
    }

    @Override
    public FuncAction build() {
        FuncAction action =
                new FuncAction(name, returnType, Builders.buildArray(arguments,new Argument[arguments.size()]), function);

        extensions.forEach(action::setExtension);

        return action;
    }

}