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

public class RestdModelConfig {

    protected final String name;

    protected Boolean anonymous;
    protected Boolean createOperationEnabled;
    protected Boolean updateOperationEnabled;
    protected Boolean deleteOperationEnabled;
    protected Boolean findOperationEnabled;
    protected Boolean queryOperationEnabled;

    public RestdModelConfig(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Boolean getAnonymous() {
        return anonymous;
    }

    public void setAnonymous(Boolean anonymous) {
        this.anonymous = anonymous;
    }

    public Boolean getCreateOperationEnabled() {
        return createOperationEnabled;
    }

    public void setCreateOperationEnabled(Boolean createOperationEnabled) {
        this.createOperationEnabled = createOperationEnabled;
    }

    public Boolean getUpdateOperationEnabled() {
        return updateOperationEnabled;
    }

    public void setUpdateOperationEnabled(Boolean updateOperationEnabled) {
        this.updateOperationEnabled = updateOperationEnabled;
    }

    public Boolean getDeleteOperationEnabled() {
        return deleteOperationEnabled;
    }

    public void setDeleteOperationEnabled(Boolean deleteOperationEnabled) {
        this.deleteOperationEnabled = deleteOperationEnabled;
    }

    public Boolean getFindOperationEnabled() {
        return findOperationEnabled;
    }

    public void setFindOperationEnabled(Boolean findOperationEnabled) {
        this.findOperationEnabled = findOperationEnabled;
    }

    public Boolean getQueryOperationEnabled() {
        return queryOperationEnabled;
    }

    public void setQueryOperationEnabled(Boolean queryOperationEnabled) {
        this.queryOperationEnabled = queryOperationEnabled;
    }
}
