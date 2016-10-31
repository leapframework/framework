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

import leap.web.api.meta.model.MApiResponseBuilder;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by kael on 2016/10/31.
 */
public class ApiConfigExtension {
    private final Map<String, ApiConfigurator> API_CONFIGURATORS = new ConcurrentHashMap<>();
    private final Map<String,MApiResponseBuilder> MRESP_BUILDER  = new ConcurrentHashMap<>();
    private OauthConfig defaultOauthConfig = new OauthConfig(false,null,null);


    public Map<String, ApiConfigurator> getApiConfigurators(){
        return Collections.unmodifiableMap(API_CONFIGURATORS);
    }

    public Map<String,MApiResponseBuilder> getCommonMResponseBuilders(){
        return Collections.unmodifiableMap(MRESP_BUILDER);
    }


    public ApiConfigurator getApiConfigurator(String name){
        return API_CONFIGURATORS.get(name);
    }

    public void addApiConfigurator(ApiConfigurator configurator){
        if(API_CONFIGURATORS.containsKey(configurator.config().getName())){
            throw new ApiConfigException("duplicate api config with name:"+configurator.config().getName());
        }
        API_CONFIGURATORS.put(configurator.config().getName(),configurator);
    }

    public OauthConfig getDefaultOauthConfig() {
        return defaultOauthConfig;
    }

    public void setDefaultOauthConfig(OauthConfig defaultOauthConfig) {
        this.defaultOauthConfig = defaultOauthConfig;
    }

    public void addCommonResponseBuilder(String name, MApiResponseBuilder builder){
        if(MRESP_BUILDER.containsKey(name)){
            throw new ApiConfigException("duplicate common response with name:"+name);
        }
        MRESP_BUILDER.put(name,builder);
    }
}
