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
package app.controllers;

import org.junit.Test;

import leap.lang.http.MimeTypes;
import leap.web.WebTestCase;

public class Servlet3ControllerTest extends WebTestCase {

	@Test
	public void testResource(){
		
		get("/servlet3/test_resource").assertContentEquals("Running");
		
	}
	
	@Test
	public void testResource1(){
		get("/servlet3/test_resource1").assertContentEquals("Running");
	}
	
	@Test
	public void testResource2(){
		get("/servlet3/test_resource2").assertContentEquals("ok");
	}
	
	@Test
	public void testResourceInJar() {
		get("/servlet3/test").assertContentContains("OK");
	}
	
	@Test
	public void testWebjarResource(){
		get("/servlet3/test_webjar_resource");
		response.assertContentTypeEquals(MimeTypes.TEXT_CSS);
	}
	
}
