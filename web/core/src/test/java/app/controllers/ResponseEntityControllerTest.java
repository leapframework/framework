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

import app.models.products.Product;
import leap.lang.http.MimeTypes;
import leap.lang.json.JSON;
import leap.web.WebTestCase;
import leap.webunit.client.THttpRequest;
import leap.webunit.client.THttpResponse;
import org.junit.Test;

import java.util.Map;

public class ResponseEntityControllerTest extends WebTestCase {

	@Test
	public void testGetProduct() {
		String json = get("/response_entity/get_product?id=1").getContent();
		assertNotEmpty(json);
		
		Product product = JSON.decode(json, Product.class);
		assertEquals(new Integer(100), product.getId());
		assertEquals("Iphone6", product.getTitle());
		
		Map<String, Object> map = JSON.decodeToMap(json);
		assertTrue(map.containsKey("summary"));

        get("/response_entity/get_product?id=2").assert404().assertContentEmpty();
	}

	@Test
	public void testGetProductIgnoreNull() {
		String json = get("/response_entity/get_product_ignore_null").getContent();
		assertNotEmpty(json);
		
		Product product = JSON.decode(json, Product.class);
		assertEquals(new Integer(100), product.getId());
		assertEquals("Iphone6", product.getTitle());
		
		Map<String, Object> map = JSON.decodeToMap(json);
		assertFalse(map.containsKey("summary"));
	}

}
