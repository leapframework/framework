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

import java.util.Map;

import leap.lang.collection.WrappedCaseInsensitiveMap;

public class ParamsMap extends WrappedCaseInsensitiveMap<Object> implements Params {

	private static final long serialVersionUID = 2508710025944765055L;

	@Override
	public Map<String, Object> map() {
		return this;
	}

	@Override
	public boolean contains(String name) {
		return containsKey(name);
	}

	@Override
	public Object get(String name) {
		return super.get(name);
	}

	@Override
	public Params set(String name, Object value) {
		put(name,value);
		return this;
	}

	@Override
	public Params setAll(Map<String, ? extends Object> m) {
		putAll(m);
		return this;
	}

	@Override
    public boolean isIndexed() {
	    return false;
    }
	
	@Override
    public boolean isNamed() {
	    return true;
    }
	
	@Override
    public int maxIndex() {
	    return -1;
    }

	@Override
    public Object get(int i) throws IllegalStateException, IndexOutOfBoundsException {
		throw new IllegalStateException("Not an indexed params");
	}
}