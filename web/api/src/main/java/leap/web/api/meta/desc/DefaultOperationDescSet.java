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

import leap.web.action.Action;
import leap.web.action.Argument;
import leap.web.api.config.ApiConfigException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by kael on 2016/11/8.
 */
public class DefaultOperationDescSet implements OperationDescSet {

    private List<OperationDesc> descs = new LinkedList<>();

    @Override
    public OperationDesc getOperationDesc(Action action) {
        if(action == null){
            return null;
        }
        for(OperationDesc desc : descs){
            if(desc.getAction() == action){
                return desc;
            }
        }
        return null;
    }

    public void addOperationDesc(Action action, OperationDesc desc){
        if(null != getOperationDesc(action)){
            throw new ApiConfigException("duplicate description for operation :"+action.getName());
        }
        descs.add(desc);
    }

    public static class DefaultOperationDesc implements OperationDescSet.OperationDesc {

        private String summary;
        private String description;
        private Action action;

        private Map<String, ParameterDesc> params = new ConcurrentHashMap<>();

        @Override
        public String getSummary() {
            return summary;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public Action getAction() {
            return action;
        }

        @Override
        public OperationDescSet.ParameterDesc getParameter(Argument argument) {
            return params.get(argument.getName());
        }

        public void addParameter(OperationDescSet.ParameterDesc param){
            params.put(param.getArgument().getName(),param);
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setAction(Action action) {
            this.action = action;
        }

    }

    public static class DefaultParameterDesc implements OperationDescSet.ParameterDesc {

        private String description;
        private Argument argument;

        private final Map<String, PropertyDesc> propertyDescMap = new HashMap<>();

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public Argument getArgument() {
            return argument;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setArgument(Argument argument) {
            this.argument = argument;
        }

        public void addProperty(PropertyDesc propertyDesc){
            if(propertyDescMap.containsKey(propertyDesc.getName())){
                throw new ApiConfigException("duplicate property "+propertyDesc.getName()+" for parameter :"+argument.getName());
            }
            propertyDescMap.put(propertyDesc.getName(),propertyDesc);
        }

        @Override
        public PropertyDesc getProperty(String name) {
            return propertyDescMap.get(name);
        }
    }

    public static class DefaultProperty implements PropertyDesc {

        private final String name;
        private String desc;

        public DefaultProperty(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc){
            this.desc = desc;
        }
    }
}
