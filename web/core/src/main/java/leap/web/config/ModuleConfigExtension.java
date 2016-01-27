/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.web.config;

import leap.lang.exception.ObjectExistsException;

import java.util.HashMap;
import java.util.Map;

public class ModuleConfigExtension {

    protected Map<String, ModuleConfig> modules = new HashMap<>();

    public Map<String,ModuleConfig> getModules() {
        return modules;
    }

    public ModuleConfig getModule(String name) {
        return modules.get(name);
    }

    public void addModule(ModuleConfig module) {
        if(modules.containsKey(module.getName())) {
            throw new ObjectExistsException("The web module '" + module.getName() + "' already exists!");
        }
        modules.put(module.getName(), module);
    }

}