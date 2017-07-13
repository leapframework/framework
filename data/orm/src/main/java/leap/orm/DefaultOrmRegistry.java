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

package leap.orm;

import leap.lang.exception.ObjectExistsException;
import leap.lang.exception.ObjectNotFoundException;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultOrmRegistry implements OrmRegistry {

    protected final Map<String, OrmContext> contexts = new ConcurrentHashMap<>();
    protected OrmContext defaultContext;

    @Override
    public boolean isEmpty() {
        return contexts.isEmpty();
    }

    @Override
    public Collection<OrmContext> contexts() {
        return contexts.values();
    }

    @Override
    public void registerContext(OrmContext context, boolean _default) throws ObjectExistsException {
        String key = context.getName().toLowerCase();
        if(contexts.containsKey(key)) {
            throw new ObjectExistsException("Orm context '" + context.getName() + "' already exists!");
        }
        contexts.put(key, context);

        if(_default) {
            if(null != defaultContext) {
                throw new ObjectExistsException("Default context '" + defaultContext.getName() + "' already exists!");
            }
            this.defaultContext = context;
        }
    }

    @Override
    public OrmContext removeContext(String name) {
        String key = name.toLowerCase();

        return contexts.remove(key);
    }

    @Override
    public OrmContext getDefaultContext() {
        return defaultContext;
    }

    @Override
    public OrmContext findContext(String name) {
        String key = name.toLowerCase();

        return contexts.get(key);
    }

    @Override
    public OrmContext getContext(String name) throws ObjectNotFoundException {
        OrmContext context = findContext(name);
        if(null == context) {
            throw new ObjectNotFoundException("Orm context '" + name + "' not found");
        }
        return context;
    }
}
