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

package leap.web.api.restd;

import leap.orm.dao.Dao;
import leap.web.api.config.ApiConfig;
import leap.web.api.config.model.RestdConfig;
import leap.web.route.Routes;

import java.util.Set;

public class SimpleRestdContext implements RestdContext {

    protected ApiConfig       ac;
    protected RestdConfig     config;
    protected Dao             dao;
    protected Set<RestdModel> models;
    protected Routes          routes;

    public SimpleRestdContext(ApiConfig ac, RestdConfig config) {
        this.ac     = ac;
        this.config = config;
    }

    @Override
    public ApiConfig getApiConfig() {
        return ac;
    }

    @Override
    public RestdConfig getConfig() {
        return config;
    }

    @Override
    public Dao getDao() {
        return dao;
    }

    public void setDao(Dao dao) {
        this.dao = dao;
    }

    @Override
    public Set<RestdModel> getModels() {
        return models;
    }

    public void setModels(Set<RestdModel> models) {
        this.models = models;
    }

    @Override
    public Routes getRoutes() {
        return routes;
    }

    public void setRoutes(Routes routes) {
        this.routes = routes;
    }
}
