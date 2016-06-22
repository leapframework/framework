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
package app;

import leap.core.AppConfig;
import leap.core.annotation.Inject;
import org.junit.Test;

import leap.junit.contexual.Contextual;
import leap.web.WebTestCase;

@Contextual
public class GlobalTest extends WebTestCase {

    protected static @Inject AppConfig config;

	@Test
	public void testGlobalLoaded(){
        assertNotNull(config);
		assertNotNull(app);
		assertEquals(app.getClass().getName(), Global.class.getName());
	}
	
	@Test
	public void testApplicationEvents(){
		assertTrue(Boolean.TRUE == servletContext.getAttribute(Global.APPLICATION_INIT_CALLED_ATTRIBUTE));
		assertTrue(Boolean.TRUE == servletContext.getAttribute(Global.APPLICATION_START_CALLED_ATTRIBUTE));
	}
	
	@Test
	public void testAppConfig() {
		assertTrue(app.config().isDebug());
	}
}
