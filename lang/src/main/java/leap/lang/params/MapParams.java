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
package leap.lang.params;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MapParams extends NamedParamsBase implements Params {
	
	private final Map<String,Object> raw;
	
	public MapParams() {
		this(new LinkedHashMap<String, Object>(1));
	}

	public MapParams(Map<String, Object> map) {
	    super(map);
	    if(this.map != map){
	    	this.raw = map;
	    }else{
	    	this.raw = null;
	    }
	}

	public void clear(){
		map.clear();
		if(null != raw){
			raw.clear();
		}
	}
	
	@Override
    protected void setRawValue(String name, Object value) {
		if(null != raw){
			raw.put(name, value);
		}
    }

	@Override
    public boolean isIndexed() {
		return null != raw && raw instanceof LinkedHashMap;
    }

	@Override
    public Object get(int i) throws IllegalStateException {
		if(!isIndexed()){
			throw new IllegalStateException("Not an indexed parameters because this map is null or not an instance of 'LinkedHashMap'");
		}
		int index=0;
		for(Entry<String, Object> entry : raw.entrySet()){
			if(index == i){
				return entry.getValue();
			}else{
				index++;	
			}
		}
		throw new IndexOutOfBoundsException("The given index " + i + " is out of bounds");
    }
	
	
}