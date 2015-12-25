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
package leap.web;

import leap.lang.Enumerable;
import leap.lang.path.AntPathMatcher;

public interface FilterMappings extends Enumerable<FilterMapping> {
	
	/**
	 * Adds a new {@link FilterMapping} object.
	 */
	FilterMappings add(FilterMapping m);
	
	/**
	 * Adds a {@link Filter} handles all request.
	 */
	FilterMappings add(Filter filter);
	
	/**
	 * Adds a {@link Filter} handles some requests of the given path pattern.
	 * 
	 * <p>
	 * The path pattern is ant style.
	 * 
	 * @see AntPathMatcher
	 */
	FilterMappings add(String path,Filter filter);
	
	/**
	 * Adds all the filter mappings in the given {@link Iterable} object.
	 */
	FilterMappings addAll(Iterable<FilterMapping> i);
}