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

import leap.lang.enums.Bool;
import leap.lang.meta.annotation.Filterable;
import leap.orm.annotation.*;
import leap.orm.model.Model;

@Entity
@DataSource("db1")
public class Entity1 extends Model {

    @Id
    protected String id;

    @Column
    @Filterable
    private String name;

    @Column
    @Filterable
    private String title;

    @Column
    protected String field1;

    @ManyToOne(target = Entity2.class,optional=Bool.TRUE)
    protected String entity2Id;

    @ManyToOne(target = RemoteEntity.class,optional=Bool.TRUE)
	private String remoteEntity1;

    @Relational
    @NonColumn
    private RemoteEntity remoteEntity;

    @Relational
    @NonColumn
    private Entity2 entity2;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getField1() {
        return field1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public String getEntity2Id() {
        return entity2Id;
    }

    public void setEntity2Id(String entity2Id) {
        this.entity2Id = entity2Id;
    }

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

	public String getRemoteEntity1() {
		return remoteEntity1;
	}

	public void setRemoteEntity1(String remoteEntity1) {
		this.remoteEntity1 = remoteEntity1;
	}

	public Entity2 getEntity2() {
		return entity2;
	}

	public void setEntity2(Entity2 entity2) {
		this.entity2 = entity2;
	}

	public RemoteEntity getRemoteEntity() {
		return remoteEntity;
	}

	public void setRemoteEntity(RemoteEntity remoteEntity) {
		this.remoteEntity = remoteEntity;
	}
}
