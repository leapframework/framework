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

package leap.orm.mapping;

import leap.lang.Buildable;
import leap.lang.beans.BeanProperty;

public class RelationPropertyBuilder implements Buildable<RelationProperty> {

    protected String       name;
    protected boolean      many;
    protected String       relationName;
    protected String       targetEntityName;
    protected String       joinEntityName;
    protected boolean      optional;
    protected BeanProperty beanProperty;

    public RelationPropertyBuilder() {

    }

    public RelationPropertyBuilder(BeanProperty beanProperty) {
        this.beanProperty = beanProperty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isMany() {
        return many;
    }

    public void setMany(boolean many) {
        this.many = many;
    }

    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public String getTargetEntityName() {
        return targetEntityName;
    }

    public void setTargetEntityName(String targetEntityName) {
        this.targetEntityName = targetEntityName;
    }

    public String getJoinEntityName() {
        return joinEntityName;
    }

    public void setJoinEntityName(String joinEntityName) {
        this.joinEntityName = joinEntityName;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public BeanProperty getBeanProperty() {
        return beanProperty;
    }

    public void setBeanProperty(BeanProperty beanProperty) {
        this.beanProperty = beanProperty;
    }

    @Override
    public RelationProperty build() {
        return new RelationProperty(name, many, relationName, targetEntityName, joinEntityName, optional, beanProperty);
    }
}