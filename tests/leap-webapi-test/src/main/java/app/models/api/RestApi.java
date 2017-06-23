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
import leap.lang.meta.annotation.Filterable;
import leap.lang.meta.annotation.Property;
import leap.orm.annotation.*;

import java.util.List;

@Table("eam_rest_api")
public class RestApi extends ModelWithDesc {

    @Column(update = Bool.FALSE, unique = Bool.TRUE, nullable = Bool.FALSE)
    @Property(sortable = Bool.TRUE, filterable = Bool.TRUE)
    protected String name;

    @Column
    protected String version;

    @Column
    @Required
    protected String title;

    @Column
    @Filterable
    @ManyToOne(target = Kind.class, optional = Bool.TRUE)
    protected String kindId;

    @Filterable
    @Column(nullable = Bool.FALSE, defaultValue = "false")
    protected Boolean published;

    @Filterable
    @Column(nullable = Bool.FALSE, defaultValue = "false")
    protected Boolean deployed;

    @Column
    protected String summary;

    @NonColumn
    protected List<RestPath> paths;

    @NonColumn
    protected List<RestModel> models;

    @Relational
    protected List<Category> categories;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getKindId() {
        return kindId;
    }

    public void setKindId(String kindId) {
        this.kindId = kindId;
    }

    public RestApi withNameAndTitle(String name) {
        this.name = name;
        this.title = name;
        return this;
    }

    public List<RestPath> getPaths() {
        return paths;
    }

    public void setPaths(List<RestPath> paths) {
        this.paths = paths;
    }

    public List<RestModel> getModels() {
        return models;
    }

    public void setModels(List<RestModel> models) {
        this.models = models;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public Boolean getDeployed() {
        return deployed;
    }

    public void setDeployed(Boolean deployed) {
        this.deployed = deployed;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }
}