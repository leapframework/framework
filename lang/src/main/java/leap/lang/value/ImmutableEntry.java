/*
 * Copyright 2012 the original author or authors.
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

import leap.lang.exception.ReadonlyException;

public class ImmutableEntry<K,V> extends SimpleEntry<K, V> {
	
	private static final long serialVersionUID = 6333097634226450971L;
	
	public static <K,V> ImmutableEntry<K,V> of(K key,V value){
		return new ImmutableEntry<K, V>(key, value);
	}
	
	public ImmutableEntry(K key,V value){
		super(key,value);
	}

	public V setValue(V value) {
	    throw new ReadonlyException("this entry is immutable");
    }
}