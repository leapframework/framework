/*
 * Copyright 2018 the original author or authors.
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

package leap.lang.meta;

import leap.lang.accessor.MapAttributeAccessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleMTypeContext extends MapAttributeAccessor implements MTypeContext {

    private final Map<Class<?>, String>       creatingComplexTypes = new ConcurrentHashMap<>();
    private final Map<Class<?>, MComplexType> createdComplexTypes  = new ConcurrentHashMap<>();

    @Override
    public void onComplexTypeCreating(Class<?> type, String name) {
        creatingComplexTypes.put(type, name);
    }

    @Override
    public void onComplexTypeCreated(Class<?> type, MComplexType ct) {
        createdComplexTypes.put(type, ct);
        creatingComplexTypes.remove(type);
    }

    @Override
    public String getCreatingComplexType(Class type) {
        return creatingComplexTypes.get(type);
    }

    @Override
    public MComplexType getCreatedComplexType(Class type) {
        return createdComplexTypes.get(type);
    }

    @Override
    public boolean isComplexTypeCreatingOrCreated(Class type) {
        return null != getCreatingComplexType(type) || null != getCreatingComplexType(type);
    }

}
