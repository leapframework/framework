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
package leap.orm.query;

import java.util.ArrayList;
import java.util.List;

import leap.lang.convert.Converts;

public class DefaultQueryResult<T> extends AbstractQueryResult<T> {

	protected final String     queryDescription;
	protected final List<T>    rows;

	public DefaultQueryResult(String queryDescription, List<T> rows) {
		this.queryDescription = queryDescription;
		this.rows             = rows;
	}
	
	@Override
    public boolean isEmpty() {
	    return rows.isEmpty();
    }

	@Override
    public int size() {
	    return rows.size();
    }

	@Override
    public List<T> list() {
	    return rows;
    }
	
	@Override
    public <E> List<E> list(Class<E> elementType) {
		List<E> cl = new ArrayList<E>();

		if(!rows.isEmpty()) {
			for(T o : rows) {
				cl.add(Converts.convert(o, elementType));
			}
		}
		
		return cl;
    }

	@Override
    protected String getQueryDescription() {
	    return queryDescription;
    }
}