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
package app.controllers;

import leap.web.security.SecurityTestCase;

import org.junit.Test;

public class CsrfControllerTest extends SecurityTestCase {

	@Test
	public void testCsrfToken() {
		String token0 = get("/public/csrf_token_0?$debug=0").getContent();
		String token1 = get("/public/csrf_token_1?$debug=0").getContent();
		assertNotEmpty(token0);
		assertEquals(token0,token1);
	}
	
	@Test
	public void testCsrfRequest() {
		login();
		try{
			get("/csrf/do_csrf").assertOk();
			post("/csrf/do_csrf").assert500();
			post("/csrf/do_csrf","csrf_token","invalid:token:1").assert500();
			
			String token = get("/csrf/get_token").getContent();
			post("/csrf/do_csrf","csrf_token",token).assertOk();
			forPost("/csrf/do_csrf").ajax().setHeader("X-CSRF-Token", token).send().assertOk();
			
			post("csrf/do_csrf","csrf_attr_test_token",token).assertOk();
			
			post("csrf/do_csrf","csrf_ignored","1").assertOk();
			
			logout();
			forPost("/csrf/do_csrf").ajax().setHeader("X-CSRF-Token", token).send().assertNotOk();
			login();
			forPost("/csrf/do_csrf").ajax().setHeader("X-CSRF-Token", token).send().assertOk();
		}finally{
			logout();
		}
	}
	
	@Test
	public void testCsrfRequest1() {
		login();
		try{
			String token = get("/public/csrf_token_1?$debug=0").getContent();
			post("/csrf/do_csrf?$debug=0","csrf_token",token).assertOk();
			forPost("/csrf/do_csrf").setHeader("X-CSRF-Token", token).send().assertOk();
		}finally{
			logout();
		}
	}
}