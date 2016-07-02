/*
 * Copyright 2013 the original author or authors.
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
package leap.lang.value;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Objects;

public class SimpleEntry<K,V> implements Entry<K, V>,Serializable {
	
	private static final long serialVersionUID = 4084244210482187547L;
	
	public static <K,V> SimpleEntry<K,V> of(K key,V value){
		return new SimpleEntry<K, V>(key, value);
	}
	
	private final K key;
	private V value;
	
	public SimpleEntry(K key,V value){
		this.key   = key;
		this.value = value;
	}

    public K getKey() {
	    return key;
    }

    public V getValue() {
	    return value;
    }

	public V setValue(V value) {
	    this.value = value;
	    return value;
    }
	
	@Override
	public boolean equals(Object obj) {
		// see Map.Entry API specification
		if (obj == this) {
			return true;
		}
		if (obj instanceof Entry<?, ?>) {
			Entry<?, ?> other = (Entry<?, ?>) obj;
			return Objects.equals(getKey(), other.getKey()) && Objects.equals(getValue(), other.getValue());
		}
		return false;
	}

	@Override
	public int hashCode() {
		// see Map.Entry API specification
		return (getKey() == null ? 0 : getKey().hashCode()) ^ (getValue() == null ? 0 : getValue().hashCode());
	}

	@Override
	public String toString() {
		return new StringBuilder().append('(').append(getKey()).append('=').append(getValue()).append(')').toString();
	}
}