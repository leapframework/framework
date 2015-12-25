/*
 * Copyright 2012 the original author or authors.
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
package leap.junit.contexual;

import org.junit.runner.Description;

public interface ContextualProvider {

	/**
	 * Returns a {@link Iterable} object contains all the contextual names.
	 */
	Iterable<String> names(Description description);
	
	/**
	 * Called before running test case.
	 * 
	 * @param description see {@link Description}
	 * @param name the test case name
	 */
	void beforeTest(Description description,String name) throws Exception;
	
	/**
	 * Called when test case finished.
	 * 
	 * @param description see {@link Description}
	 * @param name the test case name
	 */
	default void afterTest(Description description,String name) throws Exception {}
	
	/**
	 * Called when all test cases finished.
	 * 
	 * @param description see {@link Description}
	 */
	default void finishTests(Description description) throws Exception {}

}