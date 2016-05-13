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

import leap.core.BeanFactory;
import leap.core.ioc.FactoryBean;
import leap.core.ioc.PostCreateBean;
import leap.lang.Args;
import leap.lang.exception.ObjectExistsException;
import leap.lang.exception.ObjectNotFoundException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultCacheManager implements CacheManager, PostCreateBean, FactoryBean {
	
	private static final Log log = LogFactory.get(DefaultCacheManager.class);
	
	protected Map<String,Cache<?,?>> caches;
	
    @Override
    public <K, V> Cache<K, V> createSimpleLRUCache(int maxSize) {
        return new SimpleLRUCache<K, V>(maxSize);
    }

    @Override
    public <K, V> Cache<K, V> createSimpleLRUCache(String cacheName) throws ObjectExistsException {
    	Cache<K, V> cache = new SimpleLRUCache<>();
    	registerCache(cacheName, cache);
	    return cache;
    }

	@Override
    @SuppressWarnings("unchecked")
	public <K, V> Cache<K, V> getCache(String cacheName) {
		Cache<K,V> cache = (Cache<K,V>)caches.get(cacheName);
		if(null == cache){
			throw new ObjectNotFoundException("Cache '" + cacheName + "' not found");
		}
		return cache;
	}
    
	@Override
    public void registerCache(String name, Cache<?, ?> cache) throws ObjectExistsException {
		Args.notEmpty(name,"cache name");
		Args.notNull(cache,"cache");
		
		if(caches.containsKey(name)){
			throw new ObjectExistsException("The cache '" + name + "' already exists");
		}
		
		caches.put(name, cache);
    }

	@Override
    public void clearCache(String cacheName) throws ObjectNotFoundException {
		getCache(cacheName).clear();
    }

	@Override
	public void clearAll() {
		for(Entry<String, Cache<?,?>> entry : caches.entrySet()){
			try{
				entry.getValue().clear();
			}catch(Throwable e){
				log.error("Error clearing cache '{}' : {}",entry.getKey(),e.getMessage(),e);
			}
		}
	}

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void postCreate(BeanFactory factory) throws Exception {
		this.caches = new ConcurrentHashMap<>();
		this.caches.putAll((Map)factory.getNamedBeans(Cache.class));
		log.debug("Found {} managed Cache bean(s)",caches.size());
	}

    @Override
    @SuppressWarnings("unchecked")
    public Object getBean(BeanFactory beanFactory, Class type, String name) {
		if(!Cache.class.equals(type)){
			throw new IllegalStateException("Cannot get bean type '" + type.getName() + "'");
		}
	    return getCache(name);
    }
}
