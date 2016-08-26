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

public class RelationMappingBuilder implements Buildable<RelationMapping> {
	
	protected BeanProperty				    beanProperty;
	protected String					    name;
	protected RelationType				    type;
	protected Boolean					    optional;
	protected Class<?>                      targetEntityType;
	protected String                        targetEntityName;
	protected Class<?>						joinEntityType;
	protected String						joinEntityName;
	protected String						joinTableName;
	protected List<JoinFieldMappingBuilder> joinFields;
	
	public BeanProperty getBeanProperty() {
		return beanProperty;
	}

	public RelationMappingBuilder setBeanProperty(BeanProperty beanProperty) {
		this.beanProperty = beanProperty;
		return this;
	}
	
	public String getName() {
		return name;
	}

	public RelationMappingBuilder setName(String name) {
		this.name = name;
		return this;
	}
	
	public RelationType getType() {
		return type;
	}

	public RelationMappingBuilder setType(RelationType type) {
		this.type = type;
		return this;
	}
	
	public boolean isOptional() {
		return null == optional ? true : optional;
	}

	public Boolean getOptional() {
		return optional;
	}

	public RelationMappingBuilder setOptional(Boolean optional) {
		this.optional = optional;
		return this;
	}
	
	public Class<?> getTargetEntityType() {
		return targetEntityType;
	}

	public RelationMappingBuilder setTargetEntityType(Class<?> targetEntityType) {
		this.targetEntityType = targetEntityType;
		return this;
	}
	
	public String getTargetEntityName() {
		return targetEntityName;
	}

	public RelationMappingBuilder setTargetEntityName(String targetEntityName) {
		this.targetEntityName = targetEntityName;
		return this;
	}
	
	public Class<?> getJoinEntityType() {
		return joinEntityType;
	}

	public RelationMappingBuilder setJoinEntityType(Class<?> joinEntityType) {
		this.joinEntityType = joinEntityType;
		return this;
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

	public List<JoinFieldMappingBuilder> getJoinFields() {
		if(null == joinFields){
			joinFields = new ArrayList<>();
		}
		return joinFields;
	}

	public void setJoinFields(List<JoinFieldMappingBuilder> joinFields) {
		this.joinFields = joinFields;
	}
	
	public RelationMappingBuilder addJoinField(JoinFieldMappingBuilder joinField) {
		getJoinFields().add(joinField);
		return this;
	}

	@Override
    public RelationMapping build() {
		List<JoinFieldMapping> joinFields = Builders.buildList(this.joinFields);
		
	    return new RelationMapping(name, type, targetEntityName, joinEntityName, isOptional(), joinFields);
    }
}