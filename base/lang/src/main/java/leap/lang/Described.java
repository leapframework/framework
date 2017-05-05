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
package leap.lang;


public interface Described {
	
	/**
	 * Returns the short description.
	 */
	String getSummary();
	
	/**
	 * Returns the long description.
	 */
	String getDescription();

	/**
	 * Returns the description or summary if the description is empty.
	 */
	default String descOrSummary() {
		return Strings.isEmpty(getDescription()) ? getSummary() : getDescription();
	}
	
}