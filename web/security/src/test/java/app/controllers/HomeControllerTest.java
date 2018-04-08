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

import leap.core.annotation.Inject;
import leap.web.security.SecurityTestCase;

import org.junit.Test;
import tested.bean.AdditionalBean;

public class HomeControllerTest extends SecurityTestCase {

	private @Inject AdditionalBean additionalBean;
	@Test
	public void testAdditionBean(){
		assertNotNull(additionalBean);
	}
	
	
	@Test
	public void testLoginView() {
		String content = get("/").assert401().getContent();
		assertContains(content,"return_url");		
	}
	
	@Test
	public void testLoginAndLogout() {
		login("","admin","1");
		get("/").assertContentContains("Hello");
		logout();
		get("/").assertContentContains("return_url");
	}

    @Test
	public void testPermission1() {
        login();

		get("/permission1").assert403();

        logout();
    }

    @Test
	public void testPermission2() {
        login();
        get("/permission2").assertOk();
        logout();
    }

}