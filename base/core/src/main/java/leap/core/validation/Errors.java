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
package leap.core.validation;

import java.util.List;
import java.util.Locale;

import leap.lang.Emptiable;
import leap.lang.NamedError;
import leap.lang.exception.EmptyElementsException;

public interface Errors extends Iterable<NamedError>,Emptiable {
	
	/**
	 * Returns the total number of errors.
	 */
	int size();
	
	/**
	 * Clears all the errors.
	 */
	void clear();
	
	/**
	 * Returns <code>null</code> or the {@link Locale} for resolving the error messages.
	 */
	Locale getLocale();
	
	/**
	 * Returns the first error's message or <code>null</code> if empty.
	 */
	String getMessage();

	/**
	 * Returns the exception cause of the error or null.
	 */
	Throwable getCause();
	
	/**
	 * 
	 */
	List<String> getMessages(String name);
	
	/**
	 * Returns the first error.
	 * 
	 * @throws EmptyElementsException if errors is empty.
	 */
	NamedError first() throws EmptyElementsException;
	
	/**
	 * Returns the first error or <code>null</code> if errors is empty.
	 */
	NamedError firstOrNull();
	
	/**
	 * 
	 */
	boolean maxErrorsReached(int maxErrors);
	
	/**
	 * 
	 */
	boolean contains(String name);

	/**
	 * Adds an error to the end of this collection.
	 */
	void add(String name,String message);
	
	/**
	 * Adds an error to the end of this collection.
	 */
	void add(NamedError e);
	
	/**
	 * 
	 */
	void addAll(Errors errors);
	
	/**
	 * 
	 */
	void addAll(Errors errors,int maxErrors);
	
	/**
	 * 
	 */
	void addAll(String namePrefix,Errors errors);
	
	/**
	 * 
	 */
	void addAll(String namePrefix,Errors errors,int maxErrors);
	
}
