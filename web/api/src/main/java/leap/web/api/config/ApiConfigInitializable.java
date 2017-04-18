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

import leap.core.AppResources;
import leap.core.config.AppConfigContext;
import leap.core.config.AppConfigInitializable;
import leap.lang.Strings;

/**
 * Created by kael on 2016/10/31.
 */
public class ApiConfigInitializable implements AppConfigInitializable {

    @Override
    public void preLoadConfig(AppConfigContext context, AppResources appResources) {
        context.addExternalConfig("apis");
    }

    @Override
    public void postLoadConfig(AppConfigContext context, AppResources appResources) {
        ApiConfigurations extension = context.getExtension(ApiConfigurations.class);
        if(extension != null){
            extension.getConfigurators().forEach((k, v)->{
                String basepackage = v.config().getBasePackage();
                if(Strings.isNotEmpty(basepackage)){
                    context.getAdditionalPackages().add(basepackage);
                }
            });
        }
    }
}
