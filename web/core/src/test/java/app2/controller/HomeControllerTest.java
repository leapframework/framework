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
package app2.controller;

import java.util.Map;

import org.junit.Test;

import app.models.products.Product;
import leap.lang.json.JSON;
import leap.web.WebTestCase;

public class HomeControllerTest extends WebTestCase {

	@Test
	public void testIndex() {
		get("/app2/mvc").assertOk();
		get("/app2/mvc/").assertOk();
		get("/app2/mvc/index").assertOk();
	}
	
	@Test
	public void testViewAction() {
		get("/app2/mvc/index1").assertOk();
	}
	
	@Test
	public void testGetProduct() {
		String json = get("/app2/mvc/get_product").getContent();
		assertNotEmpty(json);
		
		Product product = JSON.decode(json, Product.class);
		assertEquals(new Integer(100), product.getId());
		assertEquals("Iphone6", product.getTitle());
		
		Map<String, Object> map = JSON.decodeToMap(json);
		assertFalse(map.containsKey("summary"));
	}
	
}
