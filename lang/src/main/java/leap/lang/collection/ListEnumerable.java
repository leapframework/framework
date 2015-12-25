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

import java.util.Iterator;
import java.util.List;

import leap.lang.Args;
import leap.lang.Enumerable;

public class ListEnumerable<E> extends AbstractEnumerable<E> implements Enumerable<E> {

	protected final List<E> l;
	
	public ListEnumerable(List<E> list){
		Args.notNull(list,"list");
		this.l = list;
	}

	@Override
    public Iterator<E> iterator() {
	    return l.iterator();
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
    public E get(int index) throws IndexOutOfBoundsException {
	    return l.get(index);
    }

	@Override
    public boolean contains(Object element) {
	    return l.contains(element);
    }
}