/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package leap.web.api.orm;

import leap.lang.accessor.MapAttributeAccessor;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.web.action.ActionParams;
import leap.web.api.config.ApiConfig;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.remote.RestResourceFactory;

public class DefaultModelExecutionContext extends MapAttributeAccessor implements ModelExecutionContext {

    private final ModelExecutorContext context;

    DefaultModelExecutionContext(ModelExecutorContext context) {
        this.context = context;
        this.attributes.putAll(context.getAttributes());
    }

    @Override
    public ApiConfig getApiConfig() {
        return context.getApiConfig();
    }

    @Override
    public ApiMetadata getApiMetadata() {
        return context.getApiMetadata();
    }

    @Override
    public MApiModel getApiModel() {
        return context.getApiModel();
    }

    @Override
    public Dao getDao() {
        return context.getDao();
    }

    @Override
    public EntityMapping getEntityMapping() {
        return context.getEntityMapping();
    }

    @Override
    public ActionParams getActionParams() {
        return context.getActionParams();
    }

    @Override
    public RestResourceFactory getRestResourceFactory() {
        return context.getRestResourceFactory();
    }

    @Override
    public void setRestResourceFactory(RestResourceFactory restResourceFactory) {
        context.setRestResourceFactory(restResourceFactory);
    }
}
