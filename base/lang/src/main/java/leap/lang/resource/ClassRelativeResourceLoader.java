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

import leap.lang.Args;
import leap.lang.path.Paths;

/**
 * {@link ResourceLoader} implementation that interprets plain resource paths as relative to a given
 * <code>java.lang.Class</code>.
 * 
 * @author Juergen Hoeller
 * @see java.lang.Class#getResource(String)
 * @see ClassPathResource#ClassPathResource(String, Class)
 */
@SuppressWarnings("rawtypes")
class ClassRelativeResourceLoader extends DefaultResourceLoader {

	private final Class	clazz;

	public ClassRelativeResourceLoader(Class clazz) {
		Args.notNull(clazz, "clazz");
		this.clazz = clazz;
		setClassLoader(clazz.getClassLoader());
	}

	protected Resource getResourceByPath(String path) {
		return new ClassRelativeContextResource(path, this.clazz);
	}

	private static class ClassRelativeContextResource extends ClassPathResource implements ContextResource {

		private final Class	clazz;

		public ClassRelativeContextResource(String path, Class clazz) {
			super(path, clazz);
			this.clazz = clazz;
		}

		public String getPathWithinContext() {
			return getPath();
		}

		@Override
		public Resource createRelative(String relativePath) {
			String pathToUse = Paths.applyRelative(getPath(), relativePath);
			return new ClassRelativeContextResource(pathToUse, this.clazz);
		}
	}
}
