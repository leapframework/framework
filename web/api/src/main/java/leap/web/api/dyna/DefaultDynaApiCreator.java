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

import leap.web.api.Apis;
import leap.web.api.config.ApiConfigurator;

public class DefaultDynaApiCreator implements DynaApiCreator {

    private final Apis            apis;
    private final ApiConfigurator configurator;

    public DefaultDynaApiCreator(Apis apis, ApiConfigurator configurator) {
        this.apis = apis;
        this.configurator = configurator;
    }

    @Override
    public ApiConfigurator configurator() {
        return configurator;
    }

    @Override
    public DynaApi create() {
        apis.create(configurator);

        return new DefaultDynaApi(configurator.config());
    }

}