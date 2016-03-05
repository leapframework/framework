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

import app.models.Model1;
import app.models.products.Product;
import leap.lang.Strings;
import leap.lang.enums.Bool;
import leap.web.ResponseEntity;
import leap.web.action.ControllerBase;
import leap.web.json.JsonSerialize;

public class ResponseEntityController extends ControllerBase {

	public ResponseEntity getProduct(String id) {
		Product product = new Product();
		
		product.setId(100);
		product.setTitle("Iphone6");

        if(Strings.equals("1",id)) {
            return ResponseEntity.ok(product);
        }else{
            return ResponseEntity.NOT_FOUND;
        }
	}

    @JsonSerialize(ignoreNull=Bool.TRUE)
    public ResponseEntity getProductIgnoreNull() {
        Product product = new Product();

        product.setId(100);
        product.setTitle("Iphone6");

        return ResponseEntity.ok(product);
    }

}
