/*
 * Copyright 2002-2009 the original author or authors.
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

import java.net.MalformedURLException;
import java.net.URL;

import leap.lang.Args;
import leap.lang.Classes;
import leap.lang.net.Urls;
import leap.lang.path.Paths;

/**
 * Default implementation of the {@link ResourceLoader} interface. 
 * 
 * <p>
 * Will return a {@link UrlResource} if the location value is a URL, and a {@link ClassPathResource} if it is a non-URL
 * path or a "classpath:" pseudo-URL.
 * 
 * @author Juergen Hoeller
 * @see FileResourceLoader
 */
public class DefaultResourceLoader implements ResourceLoader {

	private ClassLoader	classLoader;

	public DefaultResourceLoader() {
		
	}

	public DefaultResourceLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public ClassLoader getClassLoader() {
		return (this.classLoader != null ? this.classLoader : Classes.getClassLoader());
	}

	public Resource getResource(String name) {
		Args.notNull(name, "name");
		if (name.startsWith(Urls.CLASSPATH_ONE_URL_PREFIX)) {
			return new ClassPathResource(name.substring(Urls.CLASSPATH_ONE_URL_PREFIX.length()), getClassLoader());
		} else {
			try {
				// Try to parse the location as a URL...
				URL url = new URL(name);
				return new UrlResource(url);
			} catch (MalformedURLException ex) {
				// No URL -> resolve as resource path.
				return getResourceByPath(name);
			}
		}
	}

	protected Resource getResourceByPath(String path) {
		return new ClassPathContextResource(path, getClassLoader());
	}

	private static class ClassPathContextResource extends ClassPathResource implements ContextResource {

		public ClassPathContextResource(String path, ClassLoader classLoader) {
			super(path, classLoader);
		}

		public String getPathWithinContext() {
			return getPath();
		}

		@Override
		public Resource createRelative(String relativePath) {
			String pathToUse = Paths.applyRelative(getPath(), relativePath);
			return new ClassPathContextResource(pathToUse, getClassLoader());
		}
	}
}
