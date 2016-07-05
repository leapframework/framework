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

import java.util.Collections;
import java.util.Map;

public class EmptyParams implements Params {
	
	public static EmptyParams INSTANCE = new EmptyParams();
	
    @SuppressWarnings("unchecked")
    private final Map<String,Object> map = (Map<String,Object>)Collections.EMPTY_MAP;
	
	public EmptyParams() {
		
    }
	
	@Override
    public boolean isEmpty() {
	    return true;
    }

	@Override
    public Params set(String name, Object value) {
	    throw new IllegalStateException("Empty Parameters is readonly");
    }
	
	@Override
    public Params setAll(Map<String, ? extends Object> m) {
		throw new IllegalStateException("Empty Parameters is readonly");
    }

	@Override
    public Map<String, Object> map() {
	    return map;
    }

	@Override
    public boolean contains(String name) {
	    return false;
    }

	@Override
    public Object get(String name) {
	    return null;
    }

	@Override
    public boolean isReadonly() {
		return true;
	}

	@Override
    public boolean isIndexed() {
	    return true;
    }
	
	@Override
    public boolean isNamed() {
		return true;
	}

	@Override
    public int maxIndex() {
	    return 0;
    }

	@Override
    public Object get(int i) throws IllegalStateException {
	    throw new IndexOutOfBoundsException("No values in this params");
    }
}