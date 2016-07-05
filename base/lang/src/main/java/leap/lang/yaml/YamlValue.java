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
package leap.lang.yaml;

import java.util.List;
import java.util.Map;

public interface YamlValue {
	
	public static final YamlValue NULL = new YamlValue() {
		@Override
		public Object raw() {
			return null;
		}
	};

	@SuppressWarnings({ "rawtypes", "unchecked" })
    public static YamlValue of(Object raw) {
		if(null == raw) {
			return NULL;
		}
		if(raw instanceof YamlValue){
			return (YamlValue)raw;
		}
		if(raw instanceof Map) {
			return new YamlObject((Map)raw);
		}
		if(raw instanceof List) {
			return new YamlCollection((List)raw);
		}
		return new YamlScalar(raw);
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
	
	default boolean isScalar() {
		return false;
	}

	default boolean isArray() {
		return raw() instanceof List;
	}

	default boolean isMap() {
		return raw() instanceof Map;
	}

	default Object[] asArray() {
		return asList().toArray();
	}

	default List<Object> asList() {
		throw new IllegalStateException("Not a list");
	}

	default Map<String, Object> asMap() {
		throw new IllegalStateException("Not a map");
	}

	default YamlCollection asYamlCollection() {
		throw new IllegalStateException("Not a yaml collection");
	}

	default YamlObject asYamlObject() {
		throw new IllegalStateException("Not a yaml object");
	}
	
	default YamlScalar asYamlScalar() {
		throw new IllegalStateException("Not a scalar value");
	}
}