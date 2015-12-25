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

import leap.lang.exception.ObjectExistsException;
import leap.lang.exception.ObjectNotFoundException;


public interface CacheManager {
    
    /**
     * Creates a simple LRU cache.
     */
    <K,V> Cache<K,V> createSimpleLRUCache(int maxSize);
	
	/**
	 * Creates a simple lru cache and register it to this manager.
	 */
	<K,V> Cache<K,V> createSimpleLRUCache(String cacheName) throws ObjectExistsException;

	/**
	 * Looks up a managed {@link Cache} given it's name.
	 * 
	 * <p>
	 * Returns a {@link Cache} if it is present for the given name.
	 * 
	 * 
	 * @throws ObjectNotFoundException if the cache entry mapping to the given name does not exists.
	 */
	<K,V> Cache<K, V> getCache(String cacheName) throws ObjectNotFoundException;
	
	/**
	 * Register a cache in this manager.
	 * 
	 * @throws ObjectExistsException if the given cache name aleady exists.
	 */
	void registerCache(String name,Cache<?, ?> cache) throws ObjectExistsException;
	
	/**
	 * Clears the cache defined by the given cache name.
	 * 
	 * @throws ObjectNotFoundException if the cache name not exists.
	 */
	void clearCache(String cacheName) throws ObjectNotFoundException;

	/**
	 * Clears all the managed {@link Cache}.
	 */
	void clearAll();
	
}