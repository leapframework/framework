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
package leap.orm.query;


public interface PageIndex {

	/**
	 * Returns <code>true</code> if current page is first page.
	 */
	boolean isFirst();
	
	/**
	 * Returns <code>true</code> if current page is last page.
	 */
	boolean isLast();
	
	/**
	 * Returns current page's index, starts from 1.
	 */
	int getCurrent();
	
	/**
	 * Returns previous page's index, start from 1.
	 * 
	 * <p>
	 * Returns current page if current page is first page.
	 */
	int getPrev();
	
	/**
	 * Returns next page's index.
	 * 
	 * <p>
	 * Returns current page if current page is last page.
	 */
	int getNext();
	
	/**
	 * Returns the number of total pages.
	 */
	int getTotal();
}