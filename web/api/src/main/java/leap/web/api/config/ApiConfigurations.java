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

package leap.web.api.config;

import leap.web.api.config.model.ModelConfig;
import leap.web.api.config.model.OAuthConfig;
import leap.web.api.meta.model.MApiResponseBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

public class ApiConfigurations {

    private final Map<String, ApiConfigurator>     configurators    = new LinkedHashMap<>();
    private final Map<String, MApiResponseBuilder> commonResponses  = new LinkedHashMap<>();
    private final Map<Class<?>, ModelConfig>       commonModelTypes = new LinkedHashMap<>();

    private OAuthConfig defaultOAuthConfig = new OAuthConfig(false, null, null);

    public Map<String, ApiConfigurator> getConfigurators(){
        return configurators;
    }

    public ApiConfigurator getConfigurator(String name){
        return configurators.get(name);
    }

    public void addConfigurator(ApiConfigurator configurator){
        String key = configurator.config().getName().toLowerCase();

        if(configurators.containsKey(key)){
            throw new ApiConfigException("Found duplicated api config with name : " + configurator.config().getName());
        }

        configurators.put(key,configurator);
    }

    public OAuthConfig getDefaultOAuthConfig() {
        return defaultOAuthConfig;
    }

    public void setDefaultOAuthConfig(OAuthConfig defaultOAuthConfig) {
        this.defaultOAuthConfig = defaultOAuthConfig;
    }

    public Map<String,MApiResponseBuilder> getCommonResponses(){
        return commonResponses;
    }

    public void addCommonResponse(String name, MApiResponseBuilder resp){

        if(commonResponses.containsKey(name)){
            throw new ApiConfigException("Found duplicated common response with name : " + name);
        }

        commonResponses.put(name, resp);
    }

    public Map<Class<?>, ModelConfig> getCommonModelTypes() {
        return commonModelTypes;
    }

    public void addCommonModelType(Class<?> type, ModelConfig c) {
        if(commonModelTypes.containsKey(type)) {
            throw new ApiConfigException("Found duplicated common model config of type : " + type);
        }

        commonModelTypes.put(type, c);
    }
}
