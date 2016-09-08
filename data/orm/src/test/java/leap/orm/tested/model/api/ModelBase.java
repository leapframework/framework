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

import leap.lang.meta.annotation.Creatable;
import leap.lang.meta.annotation.Updatable;
import leap.orm.annotation.Column;
import leap.orm.annotation.Id;
import leap.orm.annotation.domain.CreatedAt;
import leap.orm.annotation.domain.UpdatedAt;
import leap.orm.model.Model;

import java.util.Date;

public abstract class ModelBase extends Model {

    @Id(generator = "shortid")
    protected String id;

    @Column
    @CreatedAt
    @Creatable(false)
    protected Date createdAt;

    @Column
    @UpdatedAt
    @Creatable(false)
    @Updatable(false)
    protected Date updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

}