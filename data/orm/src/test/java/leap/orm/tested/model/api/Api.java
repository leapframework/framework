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

package leap.orm.tested.model.api;

import leap.core.validation.annotations.Required;
import leap.lang.enums.Bool;
import leap.lang.meta.annotation.UserFilterable;
import leap.lang.meta.annotation.UserSortable;
import leap.orm.annotation.AutoCreateTable;
import leap.orm.annotation.Column;
import leap.orm.annotation.NonColumn;
import leap.orm.annotation.Table;
import leap.orm.annotation.domain.Title;

import java.util.List;

@AutoCreateTable
public class Api extends ModelWithDesc {

    @Required
    @UserSortable
    @UserFilterable
    @Column(update = Bool.FALSE, unique = Bool.TRUE)
    protected String name;

    @Column
    protected String version;

    @Title
    @Required
    protected String title;

    @NonColumn
    protected List<ApiPath> paths;

    @NonColumn
    protected List<ApiModel> models;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Api withNameAndTitle(String name) {
        this.name = name;
        this.title = name;
        return this;
    }

    public List<ApiPath> getPaths() {
        return paths;
    }

    public void setPaths(List<ApiPath> paths) {
        this.paths = paths;
    }

    public List<ApiModel> getModels() {
        return models;
    }

    public void setModels(List<ApiModel> models) {
        this.models = models;
    }
}