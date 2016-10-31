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

package leap.orm.tested.model.petclinic;

import leap.lang.enums.Bool;
import leap.orm.annotation.Column;
import leap.orm.annotation.ColumnType;
import leap.orm.annotation.Id;
import leap.orm.annotation.NonColumn;
import leap.orm.model.Model;
import leap.orm.query.PageResult;

/**
 * Created by kael on 2016/6/28.
 */
public class DataTypeModel extends Model {
    @Id
    private String id;

    private Long longType;
    @NonColumn
    private Boolean booleanType;
    private Boolean nullBooleanType;
    @Column
    private EnumType enumType;

    public Boolean getBooleanType() {
        return booleanType;
    }

    public void setBooleanType(Boolean booleanType) {
        this.booleanType = booleanType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getLongType() {
        return longType;
    }

    public void setLongType(Long longType) {
        this.longType = longType;
    }

    public Boolean getNullBooleanType() {
        return nullBooleanType;
    }

    public void setNullBooleanType(Boolean nullBooleanType) {
        this.nullBooleanType = nullBooleanType;
    }

    public EnumType getEnumType() {
        return enumType;
    }

    public void setEnumType(EnumType enumType) {
        this.enumType = enumType;
    }
}
