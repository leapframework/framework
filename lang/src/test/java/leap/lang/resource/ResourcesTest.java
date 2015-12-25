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

import java.io.File;
import java.util.function.Predicate;

import leap.junit.concurrent.ConcurrentTestCase;
import leap.lang.Classes;

import org.junit.Test;

public class ResourcesTest extends ConcurrentTestCase {
	
	@Test
	public void testGetClasspathResourceFile() throws Exception {
		Resource r = Resources.getResource(Classes.getClassResourcePath(ResourcesTest.class));
		assertNotNull(r);
		
		File file = r.getFile();
		
		assertNotNull(file);
		assertFalse(file.isDirectory());
	}
	
	@Test
	public void testGetClasspathResourceDir() throws Exception {
		Resource r = Resources.getResource(Classes.getPackageResourcePath(ResourcesTest.class));
		assertNotNull(r);
		
		File file = r.getFile();
		
		assertNotNull(file);
		assertTrue(file.isDirectory());
	}
	
	@Test
	public void testGetResourceInClassPackage() throws Exception {
		Resource res = Resources.getResource(Resource.class,"Resource.class");
		assertNotNull(res);
		assertTrue(res.exists());
	}
	
	@Test
	public void testScanResorucesSinglePattern(){
		ResourceSet rs = Resources.scan("classpath*:/junit/textui/*.class");
		assertFalse(rs.isEmpty());
		assertEquals(3, rs.size());
		
		for(Resource r : rs){
			assertNotNull(r.getClasspath());
			assertTrue(Resources.matcher.match("junit/textui/*.class", r.getClasspath()));
		}
		
		rs = Resources.scan("classpath*:/leap/test/**/*.*");
		assertFalse(rs.isEmpty());
		
		for(Resource r : rs){
			assertNotNull(r.getClasspath());
			assertTrue(Resources.matcher.match("leap/test/**/*.class", r.getClasspath()));
		}
	}
	
	@Test
	public void testScanResorucesMultiPatterns(){
		ResourceSet rs = Resources.scan("classpath*:/junit/textui/*.class","classpath*:/leap/test/**/*.class","classpath*:/META-INF/leap/test/**/*.*");
		assertFalse(rs.isEmpty());
		assertEquals(3 + 4 + 4, rs.size());
		
		Resource[] propertyResources = rs.searchUrls("**/META-INF/**/*.properties");
		assertEquals(2,propertyResources.length);
		
		propertyResources = rs.searchClasspaths("/META-INF/**/*.properties");
		assertEquals(2,propertyResources.length);
		
		Resource[] classeResources = rs.searchUrls("**/leap/**/C*.class");
		assertEquals(4,classeResources.length);
		
		classeResources = rs.searchClasspaths("/leap/**/C*.class");
		assertEquals(4,classeResources.length);
		
		classeResources = rs.search(new Predicate<Resource>(){
			@Override
            public boolean test(Resource input) {
	            return input.getFilename().endsWith(".class");
            }
		});
		assertEquals(7,classeResources.length);
		
		Class<?>[] classes = rs.searchClasses("junit");
		assertEquals(3,classes.length);
		
		classes = rs.searchClasses("leap.test");
		assertEquals(4,classes.length);		
		
		classes = rs.searchClasses("leap.test.sub");
		assertEquals(2,classes.length);
		
		classes = rs.searchClasses("leap.test",new Predicate<Class<?>>() {
			@Override
			public boolean test(Class<?> input) {
				return input.getName().endsWith("C1");
			}
		});
		assertEquals(2,classes.length);
	}
}