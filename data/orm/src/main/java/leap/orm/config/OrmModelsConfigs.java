/*
 * Copyright 2015 the original author or authors.
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
package leap.orm.config;

import leap.orm.Orm;

import java.util.HashMap;
import java.util.Map;

public class OrmModelsConfigs {

    //private OrmModelsConfig 			global;
    private Map<String, OrmModelsConfig> modelsMap = new HashMap<String, OrmModelsConfig>();

    /*
    public OrmModelsConfig getGlobal() {
        return global;
    }

    public void setGlobal(OrmModelsConfig global) {
        this.global = global;
    }
    */
    public void addModels(String name, OrmModelsConfig models) {
        modelsMap.put(name, models);
    }

    public Map<String, OrmModelsConfig> getModelsConfigMap() {
        return modelsMap;
    }

    public OrmModelsConfig getModelsConfig(String name) {
        return getModelsConfig(name, false);
    }

    public OrmModelsConfig getModelsConfig(String name, boolean primary) {
        OrmModelsConfig config = modelsMap.get(name);
        if (null == config && primary) {
            config = modelsMap.get(Orm.DEFAULT_NAME);
        }
        return config;
    }

    public void removeBasePackage(String p) {
        for (OrmModelsConfig c : modelsMap.values()) {
            if (c.removeBasePackage(p) != null) {
                return;
            }
        }
    }

    public void removeClassName(String cn) {
        for (OrmModelsConfig c : modelsMap.values()) {
            if (c.removeClass(cn) != null) {
                return;
            }
        }
    }
}
