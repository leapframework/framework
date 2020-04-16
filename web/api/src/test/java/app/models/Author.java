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

package app.models;

import leap.lang.meta.annotation.Filterable;
import leap.orm.annotation.AutoCreateTable;
import leap.orm.annotation.Column;
import leap.orm.model.Model;

@AutoCreateTable
public class Author extends Model {

    protected String id;
    protected String name;
    @Column
    protected AuthorInfo info;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Filterable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Filterable
    public AuthorInfo getInfo() {
        return info;
    }

    public void setInfo(AuthorInfo info) {
        this.info = info;
    }
}