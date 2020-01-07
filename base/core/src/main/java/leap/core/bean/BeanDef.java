/*
 *
 *  * Copyright 2019 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package leap.core.bean;

import leap.lang.Classes;
import leap.lang.convert.StringParsable;

import java.util.LinkedHashMap;
import java.util.Map;

public class BeanDef implements StringParsable {

    protected String              type;
    protected String              className;
    protected Boolean             enabled;
    protected Map<String, Object> config;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isEnabled() {
        return null == enabled || enabled;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }

    public void setConfigProperty(String name, Object value) {
        if(null == config) {
            config = new LinkedHashMap<>();
        }
        config.put(name, value);
    }

    @Override
    public void parseString(String s) {
        if(null != Classes.tryForName(s)) {
            this.className = s;
        }else {
            this.type = s;
        }
    }

}
