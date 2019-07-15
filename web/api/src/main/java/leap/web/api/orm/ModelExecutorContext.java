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

package leap.web.api.orm;

import leap.lang.accessor.AttributeAccessor;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.web.action.ActionContext;
import leap.web.action.ActionParams;
import leap.web.api.config.ApiConfig;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.remote.RestResourceFactory;

import java.util.Map;

public interface ModelExecutorContext extends AttributeAccessor {

    /**
     * Returns all the attributes.
     */
    Map<String, Object> getAttributes();

    /**
     * Required.
     */
    ApiConfig getApiConfig();

    /**
     * Required.
     */
    ApiMetadata getApiMetadata();

    /**
     * Required.
     */
    MApiModel getApiModel();

    /**
     * Required.
     */
    Dao getDao();

    /**
     * Required.
     */
    EntityMapping getEntityMapping();

    /**
     * Optional.
     */
    ActionParams getActionParams();

    /**
     * Optional
     */
    default ActionContext getActionContext() {
        return null == getActionParams() ? null : getActionParams().getContext();
    }

    /**
     * Returns the {@link RestResourceFactory}
     */
    RestResourceFactory getRestResourceFactory();

    /**
     * Set the {@link RestResourceFactory}.
     */
    void setRestResourceFactory(RestResourceFactory restResourceFactory);
}
