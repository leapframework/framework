/*
 *
 *  * Copyright 2016 the original author or authors.
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

package app.models.api;

import app.models.api.base.ModelWithDesc;
import leap.core.validation.annotations.Required;
import leap.lang.enums.Bool;
import leap.orm.annotation.Column;
import leap.orm.annotation.ManyToOne;
import leap.orm.annotation.NonColumn;
import leap.orm.annotation.Table;

import java.util.List;

@Table("eam_rest_path")
public class RestPath extends ModelWithDesc {

    @Column(nullable = Bool.FALSE, update = Bool.FALSE)
    @ManyToOne(RestApi.class)
    protected String apiId;

    @Column(nullable = Bool.TRUE, update = Bool.FALSE)
    @ManyToOne(RestPath.class)
    protected String parentId;

    @Column
    @Required
    protected String fullPath;

    @NonColumn
    protected List<RestOperation> operations;

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public List<RestOperation> getOperations() {
        return operations;
    }

    public void setOperations(List<RestOperation> operations) {
        this.operations = operations;
    }

}