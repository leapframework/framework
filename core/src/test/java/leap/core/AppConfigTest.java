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
package leap.core;

import leap.core.junit.AppTestBase;
import org.junit.Test;

public class AppConfigTest extends AppTestBase {

	@Test
	public void testDefaultAppConfig(){
		AppConfig config = AppContext.config();
		assertNotNull(config);
		assertNotNull(config.getProfile());
		assertEquals(AppProfile.DEVELOPMENT.getName(), config.getProfile());
		assertTrue(config.isDebug());
		assertNotNull(config.getDefaultCharset());
		assertNotNull(config.getDefaultLocale());
		assertTrue(config.isReloadEnabled());
		assertEquals("sys.val1", config.getProperty("sys.prop1"));
		assertEquals("imp.val1", config.getProperty("imp.prop1"));
		assertEquals("test.val1",config.getProperty("test.prop1"));
		assertEquals("test.val2",config.getProperty("test.prop2"));
		assertEquals("test.val3",config.getProperty("test.prop3"));
		
		assertTrue(config.getResources().size() > 5);
	}
	
	@Test
	public void testPlaceholder() {
	    assertEquals("${notExistProperty}", config.getProperty("testNotExistPlaceholder"));
	}
	
	@Test
	public void testPropertyProcessor() {
		assertEquals("abcdabcd",config.getProperty("test3.prop1"));
	}
	
	@Test
	public void testProfile(){
		AppConfig config = AppContext.config();
		assertEquals("1", config.getProperty("testProfile.shouldBeExists"));
		assertNull(config.getProperty("testProfile.shouldNotExists"));
	}

    /*
	@Test
	public void testImportNotCheckExistence(){
		DefaultAppConfigSource loader = new DefaultAppConfigSource();
		loader.load(Resources.getResource("classpath:/test/notcheck_existence.xml"));
	}
	
	@Test
	public void testImportCheckExistence(){
		try {
	        DefaultAppConfigSource loader = new DefaultAppConfigSource();
	        loader.load(Resources.getResource("classpath:/test/check_existence.xml"));
	        fail("should throw exception");
        } catch (AppConfigException e) {
        	assertTrue(e.getMessage().contains("not exists"));
        }
	}
	
	@Test
	public void testOverridePropertyCheck(){
		try {
	        DefaultAppConfigSource loader = new DefaultAppConfigSource();
	        loader.load(Resources.getResource("classpath:/test/not_override.xml"));
	        fail("should throw exception");
        } catch (AppConfigException e) {
        	assertTrue(e.getMessage().contains("duplicated"));
        }
	}
	
	@Test
	public void testOverrideProperty(){
        DefaultAppConfigSource loader = new DefaultAppConfigSource();
        loader.load(Resources.getResource("classpath:/test/override.xml"));
        assertEquals("v1_", loader.getProperties().get("p1"));
	}
	*/

	@Test
	public void testPropertiesPrefix() {
		AppConfig config = AppContext.config();
		assertEquals("test1.val1",config.getProperty("test1.prop1"));
		assertEquals("test2.val1",config.getProperty("test2_prop1"));
	}
	
	@Test
	public void testDataSourcePropertiesConfig() {
		
	}
	
	@Test
	public void testGetPrivateKey() {
	    assertNotNull(config.ensureGetPrivateKey());
	}

    @Test
    public void testLoadFromConfigProperties() {
        assertEquals("a", config.getProperty("props.prop1"));
        assertEquals("b", config.getProperty("props.prop2"));
    }
}
