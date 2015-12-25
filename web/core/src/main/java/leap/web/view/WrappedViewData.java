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
package leap.web.view;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class WrappedViewData implements ViewData {
	
	public static final WrappedViewData EMPTY = new WrappedViewData(Collections.emptyMap());
	
	protected final Map<String, Object> map;
	protected Object returnValue;

	public WrappedViewData() {
		this.map = new LinkedHashMap<String, Object>();
	}
	
	public WrappedViewData(Map<String,Object> m) {
		this.map = null == m ? new LinkedHashMap<String, Object>() : m;
	}

	@Override
    public Object getReturnValue() {
	    return returnValue;
    }

	@Override
    public void setReturnValue(Object returnValue) {
	    this.returnValue = returnValue;
	}

	@Override
    public int size() {
	    return map.size();
    }

	@Override
    public boolean isEmpty() {
	    return map.isEmpty();
    }

	@Override
    public boolean containsKey(Object key) {
	    return map.containsKey(key);
    }

	@Override
    public boolean containsValue(Object value) {
	    return map.containsValue(value);
    }

	@Override
    public Object get(Object key) {
	    return map.get(key);
    }

	@Override
    public Object put(String key, Object value) {
	    return map.put(key, value);
    }

	@Override
    public Object remove(Object key) {
	    return map.remove(key);
    }

	@Override
    public void putAll(Map<? extends String, ? extends Object> m) {
		map.putAll(m);
    }

	@Override
    public void clear() {
		map.clear();
    }

	@Override
    public Set<String> keySet() {
	    return map.keySet();
    }

	@Override
    public Collection<Object> values() {
	    return map.values();
    }

	@Override
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
	    return map.entrySet();
    }
}
