/*
 * Copyright 2015 the original author or authors.
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

import leap.web.WebTestCase;

public class FailureControllerTest extends WebTestCase {

	@Test
	public void testValidationFailure() {
		get("/failure/validation_error").assert400();
		get("/failure/validation_error?value=1").assertOk();
		
		get("/failure/validation_error1").assert500().assertContentEquals("Validation Error");
		get("/failure/validation_error?value=1").assertOk();
	}

	@Test
	public void testIntercepted() {
        get("/failure/intercepted").assertOk().assertContentEquals("OK");
	}

    @Test
    public void testResponseException() {
        get("/failure/response_exception").assertOk().assertContentEquals("OK");
    }
}
