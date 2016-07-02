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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import leap.lang.Emptiable;
import leap.lang.collection.UnmodifiableIteratorBase;
import leap.lang.convert.Converts;

@SuppressWarnings({"unchecked","rawtypes"})
public class YamlCollection implements Iterable<YamlValue>,Emptiable,YamlValue {
	
	private final List<Object> list;
	
	public YamlCollection(List<Object> list){
		this.list = list;
	}
	
	@Override
    public Object raw() {
	    return list;
    }
	
	@Override
    public List<Object> asList() {
		return list;
	}
	
	@Override
    public YamlCollection asYamlCollection() {
		return this;
	}

	public int size(){
		return list.size();
	}
	
	/**
	 * Returns the raw value.
	 */
    public <T> T get(int i){
		return (T)list.get(i);
	}
	
	public <T> T get(int i,Class<T> type){
		return Converts.convert(get(i), type);
	}
	
    public YamlValue getValue(int i){
    	Object o = get(i);
    	return null == o ? null : YamlValue.of(o);
	}
	
    public YamlCollection getArray(int i){
    	List list = get(i);
    	return null == list ? null : new YamlCollection(list);
	}
	
    public YamlObject getObject(int i){
    	Map map = get(i);
		return null == map ? null : new YamlObject(map);
	}
    
    public String getString(int i){
    	return get(i);
    }
    
    public Integer getInteger(int i){
    	return get(i);
    }
    
    public Long getLong(int i){
    	return get(i);
    }
    
    public Boolean getBoolean(int i){
    	return get(i);
    }
    
    public Float getFloat(int i){
    	return get(i);
    }
    
    public Double getDouble(int i){
    	return get(i);
    }
    
	@Override
    public boolean isEmpty() {
	    return list.isEmpty();
    }

	@Override
	public String toString() {
		return list.toString();
	}
	
	@Override
    public Iterator<YamlValue> iterator() {
		final Iterator<Object> it = list.iterator();
		
		return new UnmodifiableIteratorBase<YamlValue>() {
			@Override
            protected YamlValue computeNext() {
				if(!it.hasNext()){
					return endOfData();
				}
	            return YamlValue.of(it.next());
            }
		};
    }
}
