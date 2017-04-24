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

package leap.web.api.config.model;

import leap.lang.exception.ObjectExistsException;

import java.util.*;

public class RestdConfig {

    protected String      dataSourceName;
    protected boolean     anonymous;
    protected boolean     readonly;

    protected Set<String>                   includedModels = new LinkedHashSet<>();
    protected Set<String>                   excludedModels = new LinkedHashSet<>();
    protected Set<String>                   readonlyModels = new HashSet<>();
    protected Map<String, RestdModelConfig> models         = new HashMap<>();

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public Set<String> getIncludedModels() {
        return includedModels;
    }

    public void addIncludedModel(String name) {
        includedModels.add(name);
    }

    public Set<String> getExcludedModels() {
        return excludedModels;
    }

    public void addExcludedModel(String name) {
        excludedModels.add(name);
    }

    public Set<String> getReadonlyModels() {
        return readonlyModels;
    }

    public void addReadonlyModel(String name) {
        readonlyModels.add(name.toLowerCase());
    }

    public boolean isReadonlyModel(String name) {
        return readonlyModels.contains(name.toLowerCase());
    }

    public Map<String, RestdModelConfig> getModels() {
        return models;
    }

    public RestdModelConfig getModel(String name) {
        return models.get(name.toLowerCase());
    }

    public void addModel(RestdModelConfig model) {
        String key = model.getName().toLowerCase();
        if(models.containsKey(key)) {
            throw new ObjectExistsException("The configuration of model '" + model.getName() + "' already exists!");
        }
        models.put(key, model);
    }

    public boolean isModelAnonymous(String name) {
        RestdModelConfig model = getModel(name);
        if(null != model && null != model.getAnonymous()) {
            return model.getAnonymous();
        }

        return anonymous;
    }

    public boolean allowCreateModel(String name) {
        RestdModelConfig model = getModel(name);
        if(null != model && null != model.getCreateOperationEnabled()) {
            return model.getCreateOperationEnabled();
        }

        if(isReadonlyModel(name)) {
            return false;
        }

        return !readonly;
    }

    public boolean allowUpdateModel(String name) {
        RestdModelConfig model = getModel(name);
        if(null != model && null != model.getUpdateOperationEnabled()) {
            return model.getUpdateOperationEnabled();
        }

        if(isReadonlyModel(name)) {
            return false;
        }

        return !readonly;
    }

    public boolean allowDeleteModel(String name) {
        RestdModelConfig model = getModel(name);
        if(null != model && null != model.getDeleteOperationEnabled()) {
            return model.getDeleteOperationEnabled();
        }

        if(isReadonlyModel(name)) {
            return false;
        }

        return !readonly;
    }

    public boolean allowFindModel(String name) {
        RestdModelConfig model = getModel(name);
        if(null != model && null != model.getFindOperationEnabled()) {
            return model.getFindOperationEnabled();
        }

        return true;
    }

    public boolean allowQueryModel(String name) {
        RestdModelConfig model = getModel(name);
        if(null != model && null != model.getQueryOperationEnabled()) {
            return model.getQueryOperationEnabled();
        }

        return true;
    }
}