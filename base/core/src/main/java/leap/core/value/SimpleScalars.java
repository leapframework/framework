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
package leap.core.value;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import leap.lang.convert.ConvertException;
import leap.lang.convert.Converts;
import leap.lang.exception.EmptyElementsException;
import leap.lang.exception.TooManyElementsException;

public class SimpleScalars implements Scalars {
	
	private final List<Object> l;
	
	public SimpleScalars(List<Object> l) {
		this.l = Collections.unmodifiableList(l);
	}
	
	@Override
    public boolean isEmpty() {
	    return l.isEmpty();
    }

	@Override
    public int size() {
	    return l.size();
    }

	@Override
	public Object first() throws EmptyElementsException {
		if(l.isEmpty()) {
			throw new EmptyElementsException("No scalar values, cannot return the first one");
		}
		return l.get(0);
	}

	@Override
	public Object firstOrNull() {
		return l.isEmpty() ? null : l.get(0);
	}

	@Override
	public Object single() throws EmptyElementsException, TooManyElementsException {
		if(l.isEmpty()) {
			throw new EmptyElementsException("No scalar values, cannot return the single one");
		}
		if(l.size() > 1) {
			throw new TooManyElementsException("There are " + l.size() + " scalar values, cannot return the single one");
		}
		return l.get(0);
	}

	@Override
	public Object singleOrNull() throws TooManyElementsException {
		if(l.isEmpty()){
			return null;
		}
		if(l.size() > 1) {
			throw new TooManyElementsException("There are " + l.size() + " scalar values, cannot return the single one");
		}
		return l.get(0);
	}

	@Override
	public List<Object> list() {
		return l;
	}

	@Override
	public <T> List<T> list(Class<T> elementType) throws ConvertException {
		List<T> cl = new ArrayList<T>(l.size());
		
		if(!l.isEmpty()) {
			for(Object v : l){
				cl.add(Converts.convert(v, elementType));
			}
		}
		
		return cl;
	}

	public Object arrayObject(Class<?> componentType) {
    	Object a = Array.newInstance(componentType, l.size());

    	if(!l.isEmpty()) {
			for(int i=0;i<l.size();i++) {
				Array.set(a, i, Converts.convert(l.get(i), componentType));
			}
		}
		
	    return a;
	}
}
