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

import java.util.Map;

import leap.lang.http.MimeTypes;
import leap.lang.json.JSON;
import leap.web.WebTestCase;
import leap.webunit.client.THttpRequest;
import leap.webunit.client.THttpResponse;

import org.junit.Test;

import app.models.products.Product;

public class ResponseBodyControllerTest extends WebTestCase {

	@Test
	public void testGetProduct() {
		String json = get("/response_body/get_product").getContent();
		assertNotEmpty(json);
		
		Product product = JSON.decode(json, Product.class);
		assertEquals(new Integer(100), product.getId());
		assertEquals("Iphone6", product.getTitle());
		
		Map<String, Object> map = JSON.decodeMap(json);
		assertTrue(map.containsKey("summary"));
	}
	
	@Test
	public void testGetProductIgnoreNull() {
		String json = get("/response_body/get_product_ignore_null").getContent();
		assertNotEmpty(json);
		
		Product product = JSON.decode(json, Product.class);
		assertEquals(new Integer(100), product.getId());
		assertEquals("Iphone6", product.getTitle());
		
		Map<String, Object> map = JSON.decodeMap(json);
		assertFalse(map.containsKey("summary"));
	}

	@Test
	public void testGetMapWithLowerUnderscore(){
		String json = get("/response_body/get_json_stringable_with_lower_underscore").getContent();
		Map<String, Object> map = JSON.decodeMap(json);
		assertEquals("lowerUnderScore", map.get("lower_camel"));
	}

	@Test
	public void testGetMapWithLowerCamel(){
		String json = get("/response_body/get_map_with_lower_underscore").getContent();
		Map<String, Object> map = JSON.decodeMap(json);
		assertEquals("userId", map.get("user_id"));
		assertEquals("userName", ((Map<String, Object>)map.get("user_property")).get("user_name"));
	}

	@Test
	public void testGetModel1() {
		String json = get("/response_body/get_model1").getContent();
		
		Map<String, Object> map = JSON.decodeMap(json);
		assertEquals("s1",map.get("attr1"));
		assertEquals("s2", map.get("unknow"));
	}
	
	@Test
	public void testProductWithJsonPost() {
		Product product = new Product();
		product.setId(101);
		product.setTitle("Iphone6 Plus");

		String json = JSON.encode(product);
		
		THttpResponse resp = usePost("/response_body/product")
							.setContentType(MimeTypes.APPLICATION_JSON_TYPE)
							.setBody(json)
							.send();

		resp.assertContentEquals(json);
	}
	
	@Test
	public void testProductWithFormPost() {
		Product product = new Product();
		product.setId(101);
		product.setTitle("Iphone6 Plus");

		String json = JSON.encode(product);

		THttpRequest form = client().request("/response_body/product")
		                            .addFormParam("id", "101")
		                            .addFormParam("title", "Iphone6 Plus");

		THttpResponse resp = form.send();
		
		resp.assertContentEquals(json);
	}
}
