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
package leap.lang.json;

import leap.lang.Emptiable;
import leap.lang.collection.UnmodifiableIteratorBase;
import leap.lang.convert.Converts;
import leap.lang.reflect.ReflectClass;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings({"unchecked","rawtypes"})
public class JsonArray implements Iterable<JsonValue>,Emptiable,JsonValue {
	
	private final List<Object> list;
	
	public JsonArray(List<Object> list){
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
    public JsonArray asJsonArray() {
		return this;
	}

	/**
	 * Returns the length of items.
     */
	public int length(){
		return list.size();
	}
	
	/**
	 * Returns the raw value of item at the given index.
	 */
    public <T> T get(int i){
		return (T)list.get(i);
	}
	
	public <T> T get(int i,Class<T> type){
		return Converts.convert(get(i), type);
	}
	
    public JsonValue getValue(int i){
    	Object o = get(i);
    	return null == o ? null : JsonValue.of(o);
	}
	
    public JsonArray getArray(int i){
    	List list = get(i);
    	return null == list ? null : new JsonArray(list);
	}
	
    public JsonObject getObject(int i){
    	Map map = get(i);
		return null == map ? null : new JsonObject(map);
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

    /**
     * Parse all the items to the given type using {@link JsonParsable#parseJson(JsonValue)}.
     */
    public <T extends JsonParsable> List<T> parseList(Class<T> type) {
        List<T> newList = new ArrayList<>();

        ReflectClass rc = ReflectClass.of(type);

        for(Object item : this.list) {

            T obj = rc.newInstance();

            obj.parseJson(JsonValue.of(item));

            newList.add(obj);
        }

        return newList;
    }

    /**
     * Parse all the items to the given type using {@link JsonParsable#parseJson(JsonValue)}.
     */
    public <T extends JsonParsable> List<T> parseList(Supplier<T> factory) {
        List<T> newList = new ArrayList<>();

        for(Object item : this.list) {

            T obj = factory.get();

            obj.parseJson(JsonValue.of(item));

            newList.add(obj);
        }

        return newList;
    }

	@Override
    public Iterator<JsonValue> iterator() {
		final Iterator<Object> it = list.iterator();
		
		return new UnmodifiableIteratorBase<JsonValue>() {
			@Override
            protected JsonValue computeNext() {
				if(!it.hasNext()){
					return endOfData();
				}
	            return JsonValue.of(it.next());
            }
		};
    }
}
