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
package leap.lang.json;

import java.util.List;
import java.util.Map;

public interface JsonValue {
	
	JsonValue NULL = () -> null;

    /**
     * Returns the implementation of {@link JsonValue} according to the type of given object.
     */
    static JsonValue of(Object raw) {
		if(null == raw) {
			return NULL;
		}
		if(raw instanceof JsonValue){
			return (JsonValue)raw;
		}
		if(raw instanceof Map) {
			return new JsonObject((Map)raw);
		}
		if(raw instanceof List) {
			return new JsonArray((List)raw);
		}
		return new JsonScalar(raw);
	}
	
	/**
	 * Returns the raw value.
	 */
	Object raw();

	/**
	 * Returns <code>true</code> if the raw value is <code>null</code>
	 */
	default boolean isNull() {
		return null == raw();
	}

    /**
     * Returns true if the raw value is a simple value.
     */
	default boolean isScalar() {
		return false;
	}

    /**
     * Returns true if the raw value is an json array.
     */
	default boolean isArray() {
		return raw() instanceof List;
	}

    /**
     * Returns true if the raw value is an json array.
     *
     * <p/>
     * Same as {@link #isArray()}.
     */
    default boolean isList() {
        return raw() instanceof List;
    }

    /**
     * Returns true if the raw value is an json object.
     *
     * <p/>
     * Same as {@link #isMap()}
     */
    default boolean isObject() {
        return raw() instanceof Map;
    }

    /**
     * Returns true if the raw value is an json object.
     */
	default boolean isMap() {
		return raw() instanceof Map;
	}

    /**
     * Returns the raw value as an object array.
     */
	default Object[] asArray() {
		return asList().toArray();
	}

    /**
     * Returns the raw value as a list.
     *
     * @throws IllegalStateException if the raw value is not a json array.
     */
	default List<Object> asList() {
		throw new IllegalStateException("Not a list");
	}

    /**
     * Returns the raw value as a map.
     *
     * @throws IllegalStateException if the raw value is not a json object.
     */
	default Map<String, Object> asMap() {
		throw new IllegalStateException("Not a map");
	}

    /**
     * Wraps the raw value as {@link JsonArray}.
     *
     * @throws IllegalStateException it the raw value is not a json array.
     */
	default JsonArray asJsonArray() {
		throw new IllegalStateException("Not a json array");
	}

    /**
     * Wraps the raw value as {@link JsonObject}.
     *
     * @throws IllegalStateException if the raw value is not a json object.
     */
	default JsonObject asJsonObject() {
		throw new IllegalStateException("Not an json object");
	}

    /**
     * Wraps the raw value as {@link JsonScalar}.
     *
     * @throws IllegalStateException if the raw value if not a json scalar (simple) value.
     */
	default JsonScalar asJsonScalar() {
		throw new IllegalStateException("Not a scalar value");
	}

    /**
     * Returns the raw value as {@link String}.
     */
	default String asString() {
		return (String)raw();
	}

    /**
     * Returns the raw value as {@link Long}.
     */
	default Long asLong() {
		return (Long)raw();
	}

    /**
     * Returns the raw value as {@link Integer}.
     */
    default Integer asInteger() {
        return (Integer)raw();
    }

    /**
     * Returns the raw value as {@link Float}.
     */
    default Float asFloat() {
        return (Float) raw();
    }

    /**
     * Returns the raw value as {@link Double}.
     */
    default Double asDouble() {
        return (Double) raw();
    }
}