/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.orm.mapping;

import java.util.ArrayList;
import java.util.List;

import leap.lang.Buildable;
import leap.lang.Builders;
import leap.lang.beans.BeanProperty;
import leap.orm.annotation.ManyToOne;

public class RelationMappingBuilder implements Buildable<RelationMapping> {
	
	protected BeanProperty				    beanProperty;
	protected String					    name;
	protected RelationType				    type;
	protected Boolean					    optional;
    protected boolean                       virtual;
	protected Class<?>                      targetEntityType;
	protected String                        targetEntityName;
	protected Class<?>						joinEntityType;
	protected String						joinEntityName;
	protected String						joinTableName;
	protected List<JoinFieldMappingBuilder> joinFields = new ArrayList<>();

    public RelationMappingBuilder() {

    }

    public RelationMappingBuilder(BeanProperty beanProperty) {
        this.beanProperty = beanProperty;
    }

    public BeanProperty getBeanProperty() {
		return beanProperty;
	}

	public void setBeanProperty(BeanProperty beanProperty) {
		this.beanProperty = beanProperty;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public RelationType getType() {
		return type;
	}

	public void setType(RelationType type) {
		this.type = type;
	}

	public boolean isOptional() {
		return null == optional ? true : optional;
	}

	public Boolean getOptional() {
		return optional;
	}

    public boolean isVirtual() {
        return virtual;
    }

    public void setVirtual(boolean virtual) {
        this.virtual = virtual;
    }

    public void setOptional(Boolean optional) {
		this.optional = optional;
	}
	
	public Class<?> getTargetEntityType() {
		return targetEntityType;
	}

	public void setTargetEntityType(Class<?> targetEntityType) {
		this.targetEntityType = targetEntityType;
	}
	
	public String getTargetEntityName() {
		return targetEntityName;
	}

	public void setTargetEntityName(String targetEntityName) {
		this.targetEntityName = targetEntityName;
	}

    public void setTargetEntity(EntityMappingBuilder emb) {
        this.targetEntityName = emb.getEntityName();
        this.targetEntityType = emb.getEntityClass();
    }
	
	public Class<?> getJoinEntityType() {
		return joinEntityType;
	}

	public void setJoinEntityType(Class<?> joinEntityType) {
		this.joinEntityType = joinEntityType;
	}

	public String getJoinEntityName() {
		return joinEntityName;
	}

	public void setJoinEntityName(String joinEntityName) {
		this.joinEntityName = joinEntityName;
	}

	public String getJoinTableName() {
		return joinTableName;
	}

	public void setJoinTableName(String joinTableName) {
		this.joinTableName = joinTableName;
	}

    public void setJoinEntity(EntityMappingBuilder emb) {
        this.joinEntityName = emb.getEntityName();
        this.joinEntityType = emb.getEntityClass();
        this.joinTableName  = emb.getTableName();
    }

	public List<JoinFieldMappingBuilder> getJoinFields() {
		return joinFields;
	}

	public void addJoinField(JoinFieldMappingBuilder joinField) {
		joinFields.add(joinField);
	}

	@Override
    public RelationMapping build() {
		List<JoinFieldMapping> joinFields = Builders.buildList(this.joinFields);
		
	    return new RelationMapping(name, type, targetEntityName, joinEntityName, isOptional(), virtual, joinFields);
    }
}