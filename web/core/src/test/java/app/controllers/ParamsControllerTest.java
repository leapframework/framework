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

import leap.web.WebTestCase;

public class ParamsControllerTest extends WebTestCase {

	@Test
	public void testGet() {
		get("/params/get?s=sss").assertContentEquals("sss");
		get("/params/get?s=").assertContentEquals("");
		get("/params/get").assertContentEquals("");
	}
	
	@Test
	public void testGetInt() {
		get("/params/get_int?i=100").assertContentEquals("100");
		get("/params/get_int?i=").assertContentEquals("null");
		get("/params/get_int").assertContentEquals("null");
	}
	
	@Test
	public void testGetArray() {
		get("/params/get_array?s=a&s=b").assertContentEquals("a,b");
		get("/params/get_array?s=,a,b").assertContentEquals(",a,b");
		get("/params/get_array").assertContentEquals("");
	}
	
	@Test
	public void testGetIntArray() {
		get("/params/get_int_array?i=1&i=2").assertContentEquals("1,2");
		get("/params/get_int_array?i=1,2").assertContentEquals("1,2");
		get("/params/get_int_array").assertContentEquals("");
	}
	
	@Test
	public void testQueryParam() {
	    get("/params/query_param?s=a").assertContentEquals("a");
	    post("/params/query_param/","s","a").assertContentEquals("");
	}
}
