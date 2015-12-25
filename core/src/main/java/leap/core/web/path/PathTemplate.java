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
package leap.core.web.path;

import java.util.List;
import java.util.Map;

import leap.lang.path.PathPattern;

/**
 * JAX-RS specific URI template.
 */
public interface PathTemplate extends Comparable<PathTemplate>, PathPattern {
	
	/**
	 * Returns the path template as a string.
	 */
	String getTemplate();
	
	/**
	 * Returns the path before any variabels in path template.
	 * 
	 * <p>
	 * Example : 
	 * <pre>
	 * /user/{id}        -> /user
	 * /user	         -> /user
	 * /user/{id}/delete -> /user/
	 * </pre>
	 */
	String getTemplateBeforeVariables();
	
	/**
	 * Returns <code>true</code> if this template has variables.
	 */
	boolean hasVariables();
	
	/**
	 * Returns an immutable {@link List} contains the variable names defined in the path template.
	 */
	List<String> getTemplateVariables();

	/**
	 * Returns <code>true</code> if this template match the given path.
	 * 
	 * <p>
	 * If this template has variables, the resolved variable values will be putted into the given map.
	 */
	boolean match(String path,Map<String,String> variables);
}