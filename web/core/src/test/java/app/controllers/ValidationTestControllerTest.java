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

import leap.web.WebTestCase;

import org.junit.Test;

public class ValidationTestControllerTest extends WebTestCase {

	@Test
	public void testAnnotationRequired() {
		get("/validation_test/required");
		assertFalse(response.isSuccess());
		
		get("/validation_test/required?v=x").assertOk().assertContentEquals("x");
	}
	
    @Test
    public void testAnnotationRequired1() {
        get("/validation_test/required1").assertOk().assertContentEquals("OK");
    }
}
