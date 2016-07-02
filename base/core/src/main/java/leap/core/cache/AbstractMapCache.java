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
package leap.core.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import leap.lang.Args;

public abstract class AbstractMapCache<K,V> implements Cache<K, V> {
	
	private final Map<K, V> map;

	protected AbstractMapCache(Map<K, V> map) {
		Args.notNull(map,"map");
		this.map = Collections.synchronizedMap(map);
	}

	@Override
    public V get(K key) {
	    return map.get(key);
    }
	
	@Override
    public Map<K, V> getAll() {
	    return new HashMap<K,V>(map);
    }

	@Override
    public void put(K key, V value) {
		map.put(key, value);
    }

	@Override
    public boolean containsKey(K key) {
	    return map.containsKey(key);
    }

	@Override
    public boolean remove(K key) {
	    return map.remove(key) != null;
    }

	@Override
    public V getAndRemove(K key) {
	    return map.remove(key);
    }

	@Override
    public void clear() {
		map.clear();
    }
}
