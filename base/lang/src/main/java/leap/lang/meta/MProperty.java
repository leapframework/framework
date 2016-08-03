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
    protected final boolean  nullable;
    protected final String   defaultValue;
    protected final String[] enumValues;
    protected final boolean  fixedLength;
    protected final Integer  length;
    protected final Integer  precision;
    protected final Integer  scale;

    public MProperty(String name, String title, String summary, String description,
                     MType type, boolean nullable, String defaultValue, String[] enumValues,
                     boolean fixedLength,
                     Integer length, Integer precision, Integer scale) {
        super(name, title, summary, description);

        Args.notNull(type, "type");

        this.type = type;
        this.nullable = nullable;
        this.defaultValue = Strings.trimToNull(defaultValue);
        this.fixedLength = fixedLength;
        this.enumValues = enumValues;
        this.length = length;
        this.precision = precision;
        this.scale = scale;
    }

    public MType getType() {
        return type;
    }

    public boolean isNullable() {
        return nullable;
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

    @Override
    public String toString() {
        return "MProperty[name:" + name + ", kind:" + type.getTypeKind() + "]";
    }
}
