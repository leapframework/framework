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

import org.junit.Test;

import leap.lang.http.HTTP;
import leap.web.WebTestCase;

public class ErrorTestControllerTest extends WebTestCase {

	@Test
	public void test404() {
		get("/not_exists_action").assertStatusEquals(HTTP.SC_NOT_FOUND).assertContentContains("404");
		get("/error_test/err404").assertStatusEquals(HTTP.SC_NOT_FOUND).assertContentContains("404");
	}
	
	@Test
	public void test403() {
		get("/error_test/err403").assertStatusEquals(HTTP.SC_FORBIDDEN).assertContentContains("403");
	}
	
	@Test
	public void test500() {
		get("/error_test/err500").assertStatusEquals(HTTP.SC_INTERNAL_SERVER_ERROR).assertContentContains("err");
	}
	
	@Test
	public void testCustome() {
		get("/error_test/err_custom").assertStatusEquals(HTTP.SC_INTERNAL_SERVER_ERROR).assertContentContains("custom:err");
	}
}
