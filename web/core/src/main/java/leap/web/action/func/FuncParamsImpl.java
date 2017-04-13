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

package leap.web.action.func;

import leap.lang.collection.SimpleCaseInsensitiveMap;
import leap.lang.exception.ObjectNotFoundException;
import leap.web.action.Argument;

import java.util.Map;

public class FuncParamsImpl implements FuncParams {

    protected final Argument[] arguments;
    protected final Object[]   values;

    private final Map<String, Integer> nameIndexMap;

    public FuncParamsImpl(Argument[] arguments, Object[] values) {
        this.arguments = arguments;
        this.values = values;

        nameIndexMap = new SimpleCaseInsensitiveMap<>(values.length);

        for(int i=0;i<arguments.length;i++) {
            nameIndexMap.put(arguments[i].getName(), i);
        }
    }

    @Override
    public <T> T get(String name) throws ObjectNotFoundException {
        Integer index = nameIndexMap.get(name);
        if(null == index) {
            throw new ObjectNotFoundException("The parameter '" + name + "' not exists!");
        }

        return get(index);
    }

    @Override
    public <T> T get(int index) throws IndexOutOfBoundsException {
        return (T)values[index];
    }

}