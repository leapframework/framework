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
package leap.web;

import leap.junit.contexual.Contextual;
import leap.web.tested.TestedBean;

import org.junit.Test;

public class WebTestCaseTest extends WebTestCase {
	
	@Test
	public void testServerStarted(){
		assertNotNull(server);
		assertTrue(server.isAllWebAppsAvailable());
		assertNotNull(app);
	}
	
	@Test
	@Contextual
	public void testSimpleRequest(){
		get("/").assertSuccess();
	}
	
	@Test
	public void testBeanPostProcessor(){
		TestedBean testedBean = app.factory().getBean("testedBean");
		assertSame(testedBean.getApplication(), app);
	}
	
	@Test
	@Contextual
	public void testDefaultIgnore(){
		get("/test_style.css").assertContentEquals("#css");
		get("/static/test").assertContentEquals("ignore");
	}
	
}
