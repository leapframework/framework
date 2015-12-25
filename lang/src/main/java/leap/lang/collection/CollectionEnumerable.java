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

import java.util.Collection;
import java.util.Iterator;

import leap.lang.Args;
import leap.lang.Enumerable;
import leap.lang.Iterables;

public class CollectionEnumerable<E> extends AbstractEnumerable<E> implements Enumerable<E> {

	protected final Collection<E> c;
	
	public CollectionEnumerable(Collection<E> c){
		Args.notNull(c,"collection");
		this.c = c;
	}

	@Override
    public Iterator<E> iterator() {
	    return c.iterator();
    }

	@Override
    public boolean isEmpty() {
	    return c.isEmpty();
    }

	@Override
    public int size() {
	    return c.size();
    }

	@Override
    public E get(int index) throws IndexOutOfBoundsException {
	    return Iterables.get(c, index);
    }

	@Override
    public boolean contains(Object element) {
	    return c.contains(element);
    }
}