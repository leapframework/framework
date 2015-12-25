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

import leap.lang.enums.Bool;
import leap.web.action.ControllerBase;
import leap.web.json.JsonSerialize;
import app.models.Model1;
import app.models.products.Product;

public class ResponseBodyController extends ControllerBase {

	public Product getProduct() {
		Product product = new Product();
		
		product.setId(100);
		product.setTitle("Iphone6");
		
		return product;
	}
	
	@JsonSerialize(ignoreNull=Bool.TRUE)
	public Product getProductIgnoreNull() {
		Product product = new Product();
		
		product.setId(100);
		product.setTitle("Iphone6");
		
		return product;
	}
	
	public Product product(Product product) {
		return product;
	}
	
	public Model1 getModel1() {
		Model1 m = new Model1();
		m.setAttr1("s1");
		m.set("unknow", "s2");
		return m;
	}
}
