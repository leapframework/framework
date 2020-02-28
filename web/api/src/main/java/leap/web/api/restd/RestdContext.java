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

import leap.core.web.path.PathTemplateFactory;
import leap.orm.dao.Dao;
import leap.web.api.Api;
import leap.web.api.config.ApiConfig;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.config.model.RestdConfig;
import leap.web.api.orm.ModelExecutorFactory;
import leap.web.route.Route;
import leap.web.route.Routes;

import java.util.Set;

public interface RestdContext {

    /**
     * Returns the api.
     */
    Api getApi();

    /**
     * Returns the config of restd api.
     */
    RestdConfig getConfig();

    /**
     * Returns the config of api.
     */
    ApiConfig getApiConfig();

    /**
     * Returns the {@link ApiConfigurator}.
     */
    default ApiConfigurator getApiConfigurator() {
        return getApi().getConfigurator();
    }

    /**
     * Shortcut of {@link #getApiConfigurator()#addDynamicRoute(Route)}.
     */
    default void addDynamicRoute(Route route) {
        getApiConfigurator().addDynamicRoute(route);
    }

    /**
     * Returns the dao object.
     */
    Dao getDao();

    /**
     * Returns the included models of restd api.
     */
    Set<RestdModel> getModels();

    /**
     * Returns the {@link Routes} for storing dynamic created routes.
     */
    Routes getRoutes();

    /**
     * Returns the {@link PathTemplateFactory}.
     */
    PathTemplateFactory getPathTemplateFactory();

    /**
     * Returns the {@link ModelExecutorFactory}
     */
    ModelExecutorFactory getModelExecutorFactory();
}