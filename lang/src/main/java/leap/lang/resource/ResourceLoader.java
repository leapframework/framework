/*
 * Copyright 2002-2007 the original author or authors.
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

package leap.lang.resource;

/**
 * Strategy interface for loading resources (e.. class path or file system resources). An
 * {@link org.springframework.context.ApplicationContext} is required to provide this functionality, plus extended
 * {@link org.ResourceScanner.core.io.support.ResourcePatternResolver} support.
 * 
 * <p>
 * {@link DefaultResourceLoader} is a standalone implementation that is usable outside an ApplicationContext, also used
 * by {@link ResourceEditor}.
 * 
 * <p>
 * Bean properties of type Resource and Resource array can be populated from Strings when running in an
 * ApplicationContext, using the particular context's resource loading strategy.
 * 
 * @author Juergen Hoeller
 */
interface ResourceLoader {

	/**
	 * Return a Resource handle for the specified resource. The handle should always be a reusable resource descriptor,
	 * allowing for multiple {@link Resource#getInputStream()} calls.
	 * <p>
	 * <ul>
	 * <li>Must support fully qualified URLs, e.g. "file:C:/test.dat".
	 * <li>Must support classpath pseudo-URLs, e.g. "classpath:test.dat".
	 * <li>Should support relative file paths, e.g. "WEB-INF/test.dat". 
	 * </ul>
	 * <p>
	 * Note that a Resource handle does not imply an existing resource; you need to invoke {@link Resource#exists} to
	 * check for existence.
	 * 
	 * @param location the resource location
	 * @return a corresponding Resource handle
	 */
	Resource getResource(String location);

	/**
	 * Expose the ClassLoader used by this ResourceLoader.
	 * <p>
	 * Clients which need to access the ClassLoader directly can do so in a uniform manner with the ResourceLoader,
	 * rather than relying on the thread context ClassLoader.
	 * 
	 * @return the ClassLoader (never <code>null</code>)
	 */
	ClassLoader getClassLoader();
}
