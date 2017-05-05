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

import leap.lang.Collections2;
import leap.lang.Keyed;
import leap.lang.Strings;
import leap.web.api.config.model.ModelConfig;
import leap.web.api.config.model.OAuthConfig;
import leap.web.api.config.model.ParamConfig;
import leap.web.api.meta.model.MApiResponseBuilder;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Global configurations of all api(s).
 */
public class ApiConfigs {

    private final Map<String, ApiConfigurator>     apis            = new LinkedHashMap<>();
    private final Set<ModelConfig>                 commonModels    = new LinkedHashSet<>();
    private final Set<ParamConfig>                 commonParams    = new LinkedHashSet<>();
    private final Map<String, MApiResponseBuilder> commonResponses = new LinkedHashMap<>();

    private OAuthConfig oauthConfig;

    public Map<String, ApiConfigurator> getApis() {
        return apis;
    }

    public ApiConfigurator getApi(String name) {
        return apis.get(name);
    }

    public void addApi(ApiConfigurator api) {
        String key = api.config().getName().toLowerCase();

        if (apis.containsKey(key)) {
            throw new ApiConfigException("Found duplicated api config with name : " + api.config().getName());
        }

        apis.put(key, api);
    }

    public OAuthConfig getOAuthConfig() {
        return oauthConfig;
    }

    public void setOAuthConfig(OAuthConfig oauthConfig) {
        this.oauthConfig = oauthConfig;
    }

    public Set<ModelConfig> getCommonModels() {
        return commonModels;
    }

    public void addCommonModel(ModelConfig model) {
        addModel(commonModels, model);
    }

    public Set<ParamConfig> getCommonParams() {
        return commonParams;
    }

    public void addCommonParam(ParamConfig param) {
        addParam(commonParams, param);
    }

    public boolean removeCommonParam(Keyed param) {
        return removeParam(commonParams, param);
    }

    public Map<String, MApiResponseBuilder> getCommonResponses() {
        return commonResponses;
    }

    public void addCommonResponse(String name, MApiResponseBuilder resp) {
        if (commonResponses.containsKey(name)) {
            throw new ApiConfigException("Found duplicated common response with name : " + name);
        }

        commonResponses.put(name, resp);
    }

    static void addModel(Set<ModelConfig> models, ModelConfig model) {
        models.forEach(exists -> {
            if (null != exists.getClassName() && exists.getClassName().equals(model.getClassName())) {
                throw new ApiConfigException("Found duplicated model type '" + model.getClassName() + "'");
            }

            if (!Strings.isEmpty(exists.getName()) && exists.getName().equalsIgnoreCase(model.getName())) {
                throw new ApiConfigException("Found duplicated model name '" + model.getName() + "'");
            }
        });

        models.add(model);
    }

    static void addParam(Set<ParamConfig> params, ParamConfig param) {
        params.forEach(exists -> {
            if(exists.getKey().equals(param.getKey())) {
                throw new ApiConfigException("Found duplicated param '" + param.getKey() + "'");
            }
        });

        params.add(param);
    }

    static boolean removeParam(Set<ParamConfig> params, Keyed param) {
        return null != Collections2.remove(params, (p) -> p.getKey().equals(param.getKey()));
    }
}
