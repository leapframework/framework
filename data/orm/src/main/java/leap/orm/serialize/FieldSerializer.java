/*
 * Copyright 2016 the original author or authors.
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
package leap.orm.serialize;

import leap.lang.convert.Converts;
import leap.orm.mapping.FieldMapping;
import java.lang.reflect.Type;
import java.util.Objects;

public interface FieldSerializer {

    /**
     * Serialize the value to encoded value.
     *
     * <p/>
     * If the value already serialized, just return it.
     */
    Object trySerialize(FieldMapping fm, Object value);

    /**
     * Decodes the encoded value to raw value.
     */
    Object deserialize(FieldMapping fm, Object encoded);

    /**
     * Deserialize the encoded value to the value of given type..
     */
    default Object deserialize(FieldMapping fm, Object encoded, Class<?> type, Type genericType) {
        return Converts.convert(deserialize(fm, encoded), type, genericType);
    }

    default boolean matches(FieldMapping fm, Object value, Object encoded) {
        Object encodedValue = trySerialize(fm, value);
        return Objects.equals(encoded, encodedValue);
    }
}