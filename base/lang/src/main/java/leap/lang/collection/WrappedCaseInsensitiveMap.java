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
package leap.lang.collection;

import leap.lang.Args;
import leap.lang.Objects2;
import leap.lang.value.SimpleEntry;

import java.io.Serializable;
import java.util.*;

public class WrappedCaseInsensitiveMap<V> implements Map<String, V>,CaseInsensitiveMap<V>,Serializable {
	
	private static final long serialVersionUID = -4254530641694177354L;

	public static <V> WrappedCaseInsensitiveMap<V> create(){
		return new WrappedCaseInsensitiveMap<V>();
	}
	
	public static <V> WrappedCaseInsensitiveMap<V> create(Map<? extends String, ? extends V> m){
		WrappedCaseInsensitiveMap<V> map = new WrappedCaseInsensitiveMap<V>();
		map.putAll(m);
		return map;
	}
	
	public static <V> WrappedCaseInsensitiveMap<V> wrap(Map<CaseInsensitiveKey,V> map){
		return new WrappedCaseInsensitiveMap<V>(map);
	}
	
	public static <V> WrappedCaseInsensitiveMap<V> wrap(Map<CaseInsensitiveKey,V> map,Map<? extends String, ? extends V> m){
		WrappedCaseInsensitiveMap<V> wrapped = new WrappedCaseInsensitiveMap<V>(map);
		wrapped.putAll(m);
		return wrapped;
	}
	
	private final Map<CaseInsensitiveKey, V> map;
	
	/**
	 * Creates a new {@link WrappedCaseInsensitiveMap} instance wrapped {@link HashMap}.
	 */
	public WrappedCaseInsensitiveMap() {
	    this.map = new HashMap<CaseInsensitiveKey,V>();
    }
	
	/**
	 * @see HashMap#HashMap(int)
	 */
    public WrappedCaseInsensitiveMap(int initialCapacity) {
        this.map = new HashMap<CaseInsensitiveKey, V>(initialCapacity);
    }
    
	/**
	 * @see HashMap#HashMap(int, float)
	 */
    public WrappedCaseInsensitiveMap(int initialCapacity, float loadFactor) {
    	this.map = new HashMap<CaseInsensitiveKey, V>(initialCapacity,loadFactor);
    }
    
	public WrappedCaseInsensitiveMap(Map<CaseInsensitiveKey, V> map) {
		Args.notNull(map,"wrapped map");
	    this.map = map;
    }
	
	@Override
    public void clear() {
		map.clear();
    }

	@Override
    public boolean containsKey(Object key) {
		if(null == key){
			return false;
		}
	    return map.containsKey(new CaseInsensitiveKey(key.toString()));
    }

	@Override
    public boolean containsValue(Object value) {
		return map.containsValue(value);
    }

	@Override
    public V get(Object key) {
	    return map.get(null == key ? null : new CaseInsensitiveKey(key.toString()));
    }

	@Override
    public boolean isEmpty() {
	    return map.isEmpty();
    }

	@Override
    public V put(String key, V value) {
	    return map.put(null == key ? null : new CaseInsensitiveKey(key), value);
    }

	@Override
    public void putAll(Map<? extends String, ? extends V> m) {
		if(null == m){
			return;
		}
		for(Map.Entry<? extends String, ? extends V> entry : m.entrySet()){
			put(entry.getKey(),entry.getValue());
		}
	}

	@Override
    public V remove(Object key) {
	    return map.remove(null == key ? null : new CaseInsensitiveKey(key.toString()));
    }

	@Override
    public int size() {
	    return map.size();
    }

	@Override
    public Collection<V> values() {
	    return map.values();
    }
	
    @Override
    @SuppressWarnings("rawtypes")
    public boolean equals(Object obj) {
		if(null == obj){
			return false;
		}
		if(obj == this){
			return true;
		}
		
		if(obj.getClass() == WrappedCaseInsensitiveMap.class){
			return this.map.equals(((WrappedCaseInsensitiveMap)obj).map);
		}
		
		return map.equals(obj);
    }

	@Override
    public int hashCode() {
	    return Objects2.HASH_SEED * Objects2.HASH_OFFSET + map.hashCode();
    }
	
	@Override
    public String toString() {
	    return map.toString();
    }

	@Override
    public Set<Entry<String, V>> entrySet() {
	    return new EntrySet<V>(map);
    }
	
	@Override
    public Set<String> keySet() {
	    return new KeySet(map);
    }
	
	private static class EntrySet<V> extends AbstractSet<Entry<String,V>> {
		private final Map<CaseInsensitiveKey,V> map;
		private final EntrySetIterator<V>       iterator;
		
		private EntrySet(Map<CaseInsensitiveKey,V> map){
			this.map      = map;
			this.iterator = new EntrySetIterator<V>(map.entrySet().iterator());
		}
		
		@Override
        public Iterator<java.util.Map.Entry<String, V>> iterator() {
	        return iterator;
        }

		@Override
        public int size() {
	        return map.size();
        }
	}
	
	private static class EntrySetIterator<V> implements Iterator<Entry<String,V>> {
		
		private final Iterator<Entry<CaseInsensitiveKey, V>> iterator;
		
		private EntrySetIterator(Iterator<Entry<CaseInsensitiveKey,V>> iterator){
			this.iterator = iterator;
		}

		@Override
        public boolean hasNext() {
	        return iterator.hasNext();
        }

		@Override
        public java.util.Map.Entry<String, V> next() {
			Entry<CaseInsensitiveKey,V> entry = iterator.next();
	        return new SimpleEntry<String,V>(entry.getKey().key, entry.getValue());
        }

		@Override
        public void remove() {
			iterator.remove();
        }
	}
	
	private static class KeySet extends AbstractSet<String> {
		private final Map<CaseInsensitiveKey,?> map;
		private final KeySetIterator            iterator;
		
		private KeySet(Map<CaseInsensitiveKey,?> map){
			this.map      = map;
			this.iterator = new KeySetIterator(map.keySet().iterator());
		}
		
		@Override
        public Iterator<String> iterator() {
	        return iterator;
        }

		@Override
        public int size() {
	        return map.size();
        }
	}
	
	private static class KeySetIterator implements Iterator<String> {
		
		private final Iterator<CaseInsensitiveKey> iterator;
		
		private KeySetIterator(Iterator<CaseInsensitiveKey> iterator){
			this.iterator = iterator;
		}

		@Override
        public boolean hasNext() {
	        return iterator.hasNext();
        }

		@Override
        public String next() {
			return iterator.next().key;
        }

		@Override
        public void remove() {
			iterator.remove();
        }
	}

	public static final class CaseInsensitiveKey {
		
        private final String key;
        
        private CaseInsensitiveKey(String key) {
            this.key = key;
        }

        @Override
        public int hashCode() {
            return Objects2.HASH_SEED * Objects2.HASH_OFFSET + key.toLowerCase().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            if (obj.getClass() != CaseInsensitiveKey.class) {
                return false;
            }
            return this.key.equalsIgnoreCase(((CaseInsensitiveKey) obj).key);
        }

        @Override
        public String toString() {
            return key;
        }
    }
}
