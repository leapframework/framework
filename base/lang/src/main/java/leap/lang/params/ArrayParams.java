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


public class ArrayParams extends NamedParamsBase {
	
	public static final String PREFIX = "$";
	
	protected final Object[] values;
	
	public ArrayParams(Object... values){
		super();
		this.values = values;
		for(int i=0;i<values.length;i++){
			map.put(PREFIX + i, values[i]);
		}
	}
	
	public Object[] array() {
		return values;
	}
	
	@Override
    public boolean isEmpty() {
	    return null != values && values.length > 0;
    }

	@Override
    public boolean isIndexed() {
	    return true;
    }

	@Override
	public boolean isArray() {
		return true;
	}

	@Override
    public Object get(int i) throws IllegalStateException {
	    return values[i];
    }

}