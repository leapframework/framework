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
import leap.lang.Emptiable;
import leap.lang.convert.ConvertException;

public interface QueryResult<T> extends Emptiable {
	
	/**
	 * Returns the size of returned records. 
	 */
	int size();

	/**
	 * Returns the first record or throws {@link EmptyRecordsException} if no records.
	 */
	T first() throws EmptyRecordsException;
	
	/**
	 * Returns the first record or <code>null</code> if no records.
	 */
	T firstOrNull();
	
	/**
	 * Returns the first record or throws {@link EmptyRecordsException} if no records
	 * or throws {@link TooManyRecordsException} if two or more records returned. 
	 */
	T single() throws EmptyRecordsException,TooManyRecordsException;
	
	/**
	 * Returns the first record or <code>null</code> if no records.
	 * 
	 * <p>
	 * Throws {@link TooManyRecordsException} if two or more records returned.
	 */
	T singleOrNull() throws TooManyRecordsException;
	
	/**
	 * Returns a {@link List} contains all the records.
	 */
	List<T> list();

	/**
	 * Converts all the record to the given element type and returns a {@link List} contains all the converted elements.
	 * 
	 * @throws ConvertException if cannot convert the record to the element type.
	 */
	<E> List<E> list(Class<E> elementType) throws ConvertException;
}