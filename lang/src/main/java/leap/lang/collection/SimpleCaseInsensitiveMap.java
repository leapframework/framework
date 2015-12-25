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

import java.util.HashMap;
import java.util.Map;

import leap.lang.enums.CaseType;

public class SimpleCaseInsensitiveMap<V> extends HashMap<String,V> implements CaseSensitiveMap<V> {

	private static final long serialVersionUID = 7737207181956011290L;
	
	private final CaseType caseType;
	
	public SimpleCaseInsensitiveMap() {
	    super();
	    this.caseType = CaseType.UPPER;
    }
	
	public SimpleCaseInsensitiveMap(CaseType caseType) {
	    super();
	    this.caseType = caseType;
    }
	
	public SimpleCaseInsensitiveMap(int initialCapacity) {
	    super(initialCapacity);
	    this.caseType = CaseType.UPPER;
    }
	
	public SimpleCaseInsensitiveMap(int initialCapacity,CaseType caseType) {
	    super(initialCapacity);
	    this.caseType = caseType;
    }

	public SimpleCaseInsensitiveMap(int initialCapacity, float loadFactor) {
	    super(initialCapacity, loadFactor);
	    this.caseType = CaseType.UPPER;
    }
	
	public SimpleCaseInsensitiveMap(int initialCapacity, float loadFactor,CaseType caseType) {
	    super(initialCapacity, loadFactor);
	    this.caseType = caseType;
    }

	public SimpleCaseInsensitiveMap(Map<? extends String, ? extends V> m) {
		super();
		this.caseType = CaseType.UPPER;
	    this.putAll(m);
    }
	
	public SimpleCaseInsensitiveMap(Map<? extends String, ? extends V> m,CaseType caseType) {
		super();
		this.caseType = caseType;
	    this.putAll(m);
    }

	@Override
    public boolean containsKey(Object key) {
	    return super.containsKey(caseObjectKey(key));
    }
	
	@Override
    public V get(Object key) {
	    return super.get(caseObjectKey(key));
    }

	@Override
    public V put(String key, V value) {
	    return super.put(caseStringKey(key), value);
    }

	@Override
    public void putAll(Map<? extends String, ? extends V> map) {
		if(null == map){
			return;
		}
		
		for(Map.Entry<? extends String,? extends V> entry : map.entrySet()){
			super.put(caseStringKey(entry.getKey()), entry.getValue());
		}
	}

	@Override
    public V remove(Object key) {
	    return super.remove(caseObjectKey(key));
    }
	
    protected final String caseStringKey(String key) {
		return null == key ? null : key.toUpperCase();
    }

    protected final String caseObjectKey(Object key) {
		return null == key ? null : key.toString().toUpperCase();
    }
    
    protected String caseString(String string){
    	return caseType.get(string);
    }
}