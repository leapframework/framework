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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by kael on 2016/11/15.
 */
class CommonDescContainer {
    private static final Map<Class<?>, Parameter> parameters = new ConcurrentHashMap<>();

    public void addCommonParam(Parameter parameter){
        if(parameters.containsKey(parameter.getType())){
            throw new ApiConfigException("duplicate common parameter:"+parameter.getType().getName());
        }
        parameters.put(parameter.getType(),parameter);
    }

    public Parameter getCommonParam(Class<?> type){
        return parameters.get(type);
    }

    public static class Parameter{
        private final Class<?> type;
        private String title;
        private String desc;
        private final Map<String, Property> properties = new HashMap<>();


        public Parameter(Class<?> type) {
            this.type = type;
        }

        public Class<?> getType() {
            return type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public void addProperty(Property property){
            if(properties.containsKey(property.getName())){
                throw new ApiConfigException("duplicate property of common parameter:"+this.getType().getName());
            }
            properties.put(property.getName(),property);
        }

        public Property getProperty(String name){
            return properties.get(name);
        }
    }

    public static class Property{
        private final String name;
        private String title;
        private String desc;
        public Property(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }
}
