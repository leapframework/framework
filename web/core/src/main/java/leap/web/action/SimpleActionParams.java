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

import leap.lang.collection.SimpleCaseInsensitiveMap;

import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleActionParams implements ActionParams {

    protected final ActionContext context;
    protected final Argument[]    arguments;
    protected final Object[]      values;

    private final Map<String, Integer> nameIndexMap;

    private Map<String, Object> map;

    public SimpleActionParams(ActionContext context, Argument[] arguments, Object[] values) {
        this.context = context;
        this.arguments = arguments;
        this.values = values;

        nameIndexMap = new SimpleCaseInsensitiveMap<>(values.length);

        for (int i = 0; i < arguments.length; i++) {
            nameIndexMap.put(arguments[i].getName(), i);
        }
    }

    @Override
    public ActionContext getContext() {
        return context;
    }

    @Override
    public Argument[] getArguments() {
        return arguments;
    }

    @Override
    public Map<String, Object> toMap() {
        if (null == map) {
            map = new LinkedHashMap<>(values.length);
            for (int i = 0; i < arguments.length; i++) {
                map.put(arguments[i].getName(), values[i]);
            }
        }
        return map;
    }

    @Override
    public boolean contains(String name) {
        return nameIndexMap.containsKey(name);
    }

    @Override
    public Object get(String name) {
        Integer index = nameIndexMap.get(name);
        return get(index);
    }

    @Override
    public <T> T get(int index) throws IndexOutOfBoundsException {
        return (T) values[index];
    }

}