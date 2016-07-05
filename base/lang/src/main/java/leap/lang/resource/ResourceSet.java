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
package leap.lang.resource;

import leap.lang.Enumerable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface ResourceSet extends Enumerable<Resource> {
	
	Resource[] toResourceArray();

	Resource getClasspathResource(String name);
	
	Resource[] search(Predicate<Resource> predicate);
	
	Resource[] searchUrls(String... urlPatterns);
	
	Resource[] searchClasspaths(String... classpathPatterns);
	
	/**
	 * Search all the classes in this resource collection.
	 */
	Class<?>[] searchClasses();
	
	/**
	 * search all the classes in this resource collection which starts with the given package.
	 */
	Class<?>[] searchClasses(String basePackage);
	
	Class<?>[] searchClasses(String basePackage,Predicate<Class<?>> predicate);
	
	Class<?>[] searchClasses(Predicate<Class<?>> predicate);
	
	void process(Consumer<Resource> processor);
	
	void processClasses(Consumer<Class<?>> processor);
}