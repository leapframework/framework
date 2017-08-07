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

package leap.web.api.dyna;

import leap.core.annotation.Inject;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.App;
import leap.web.api.Api;
import leap.web.api.Apis;
import leap.web.api.config.ApiConfig;
import leap.web.route.RoutesPrinter;

public class DefaultDynaApiFactory implements DynaApiFactory {

    private static final Log log = LogFactory.get(DefaultDynaApiFactory.class);

    protected @Inject Apis          apis;
    protected @Inject App           app;
    protected @Inject RoutesPrinter routesPrinter;

    @Override
    public DynaApi newDynaApi(String name, String basePath, boolean register) {
        Api api = apis.newDynamic(name, basePath);

        if(register) {
            apis.add(api);
        }

        return new DefaultDynaApi(api);
    }

    @Override
    public void destroyDynaApi(DynaApi api) {
        ApiConfig config = api.getConfig();

        if(log.isDebugEnabled()) {
            log.debug("Routes before destroying api '{}': \n\n{}\n", api.getName(), routesPrinter.print(app.routes()));
        }

        config.getApiRoutes().forEach(ar -> {
            if(ar.isDynamic()) {
                app.routes().remove(ar.getRoute());
            }
        });

        if(log.isDebugEnabled()) {
            log.debug("Routes after destroying api '{}': \n\n{}\n", api.getName(), routesPrinter.print(app.routes()));
        }

        apis.remove(api);
    }

}