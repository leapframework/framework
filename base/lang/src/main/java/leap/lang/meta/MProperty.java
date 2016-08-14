/*
 * Copyright 2015 the original author or authors.
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
package leap.lang.meta;

import leap.lang.Args;
import leap.lang.Strings;

public class MProperty extends ImmutableMNamedWithDesc {

    protected final MType    type;
    protected final Boolean  required;
    protected final String   defaultValue;
    protected final String[] enumValues;
    protected final boolean  fixedLength;
    protected final Integer  length;
    protected final Integer  precision;
    protected final Integer  scale;
    protected final Boolean  userCreatable;
    protected final Boolean  userUpdatable;
    protected final Boolean  userSortable;
    protected final Boolean  userFilterable;

    public MProperty(String name, String title, String summary, String description,
                     MType type, Boolean required, String defaultValue, String[] enumValues,
                     boolean fixedLength,
                     Integer length, Integer precision, Integer scale,
                     Boolean userCreatable, Boolean userUpdatable, Boolean userSortable, Boolean userFilterable) {
        super(name, title, summary, description);

        Args.notNull(type, "type");

        this.type = type;
        this.required = required;
        this.defaultValue = Strings.trimToNull(defaultValue);
        this.fixedLength = fixedLength;
        this.enumValues = enumValues;
        this.length = length;
        this.precision = precision;
        this.scale = scale;
        this.userCreatable = userCreatable;
        this.userUpdatable = userUpdatable;
        this.userSortable = userSortable;
        this.userFilterable = userFilterable;
    }

    public MType getType() {
        return type;
    }

    public Boolean getRequired() {
        return required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String[] getEnumValues() {
        return enumValues;
    }

    public boolean isFixedLength() {
        return fixedLength;
    }

    public Integer getLength() {
        return length;
    }

    public Integer getPrecision() {
        return precision;
    }

    public Integer getScale() {
        return scale;
    }

    public Boolean getUserCreatable() {
        return userCreatable;
    }

    public Boolean getUserUpdatable() {
        return userUpdatable;
    }

    public Boolean getUserSortable() {
        return userSortable;
    }

    public Boolean getUserFilterable() {
        return userFilterable;
    }

    @Override
    public String toString() {
        return "MProperty[name:" + name + ", kind:" + type.getTypeKind() + "]";
    }
}
