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

import java.util.List;

import leap.core.exception.EmptyRecordsException;
import leap.core.exception.TooManyRecordsException;

public abstract class AbstractQueryResult<T> implements QueryResult<T> {
	
	@Override
    public T first() throws EmptyRecordsException {
		List<T> list = list();
		if(list.isEmpty()){
			throw new EmptyRecordsException("The result must not be empty of query : " + getQueryDescription());
		}
	    return list.get(0);
    }

	@Override
    public T firstOrNull() {
		List<T> list = list();
		if(list.isEmpty()){
			return null;
		}
	    return list.get(0);
    }

	@Override
    public T single() throws EmptyRecordsException, TooManyRecordsException {
		List<T> list = list();
		if(list.isEmpty()){
			throw new EmptyRecordsException("The results must not be empty of query : " + getQueryDescription());
		}
		if(list.size() > 1){
			throw new TooManyRecordsException("The results(" + list.size() + " rows) must contains one row data only of query : " + getQueryDescription());
		}
	    return list.get(0);
    }

	@Override
    public T singleOrNull() throws TooManyRecordsException {
		List<T> list = list();
		if(list.isEmpty()){
			return null;
		}
		if(list.size() > 1){
			throw new TooManyRecordsException("The results(" + list.size() + " rows) must contains zero or one row data only of query : " + getQueryDescription());
		}
	    return list.get(0);
    }
	
	protected abstract String getQueryDescription();
}
