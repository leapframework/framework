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

package leap.web.api.config.model;

import leap.web.api.config.ApiConfigException;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ParamConfigImpl extends ModelConfigImpl.PropertyImpl implements ParamConfig {

    protected String  className;
    protected boolean override;

    protected Map<String, ParamConfig> wrappedParams = new LinkedHashMap<>();

    public ParamConfigImpl(String name) {
        super(name);
    }

    @Override
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isOverride() {
        return override;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }

    @Override
    public Set<ParamConfig> getWrappedParams() {
        return new LinkedHashSet<>(wrappedParams.values());
    }

    @Override
    public ParamConfig getWrappedParam(String name) {
        return wrappedParams.get(name.toLowerCase());
    }

    public void addWrappedParam(ParamConfig p) {
        if(wrappedParams.containsKey(p.getName().toLowerCase())) {
            throw new ApiConfigException("Found duplicated wrapped param '" + p.getName() + "' of type " + className);
        }
        wrappedParams.put(p.getName().toLowerCase(), p);
    }
}
