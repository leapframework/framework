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

package leap.web.api.log.model;

import leap.lang.meta.annotation.Filterable;
import leap.lang.meta.annotation.Sortable;
import leap.orm.annotation.Column;
import leap.orm.model.Model;

import java.sql.Timestamp;

/**
 * Created by kael on 2016/10/10.
 */
public abstract class OpLogModel extends Model {
    @Filterable
    @Sortable
    @Column
    protected String id;
    @Filterable
    @Sortable
    @Column
    protected String title;
    @Filterable
    @Sortable
    @Column
    protected String description;
    @Filterable
    @Sortable
    @Column
    protected Timestamp createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
