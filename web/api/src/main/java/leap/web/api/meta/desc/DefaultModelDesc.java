/*
 *
 *  * Copyright 2013 the original author or authors.
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

package leap.web.api.meta.desc;

import leap.web.api.config.ApiConfigException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by kael on 2016/11/14.
 */
public class DefaultModelDesc implements ModelDesc {

    private static final Map<String, PropertyDesc> fields = new ConcurrentHashMap<>();

    @Override
    public PropertyDesc getPropertyDesc(String fieldName) {
        return fields.get(fieldName);
    }

    public void addPropertyDesc(String name, PropertyDesc desc){
        if(fields.containsKey(name)){
            throw new ApiConfigException("duplicate field description of "+name);
        }
        fields.put(name,desc);
    }

    public static class DefaultPropertyDesc implements PropertyDesc {

        private String desc;

        @Override
        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

}
