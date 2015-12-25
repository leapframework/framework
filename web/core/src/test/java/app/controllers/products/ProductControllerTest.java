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
package app.controllers.products;

import org.junit.Test;

import app.models.products.Product;

import leap.lang.json.JSON;
import leap.web.WebTestCase;

public class ProductControllerTest extends WebTestCase {

	@Test
	public void testProductControllerIndex(){
		assertTrue(client().get("/products/product").isSuccess());
		assertTrue(client().get("/products/product/").isSuccess());
		assertTrue(client().get("/products/product/list").isSuccess());
	}
	
	@Test
	public void testDoFind(){
		//post with nothing
		Product product = JSON.decode(post("/products/product/find").getContent(),Product.class);
		assertNull(product.getId());
		assertNull(product.getTitle());
		assertNull(product.getSummary());
		
		//post with id
		product = JSON.decode(post("/products/product/find","id","1").getContent(),Product.class);
		assertEquals(new Integer(1),product.getId());
	}
	
}