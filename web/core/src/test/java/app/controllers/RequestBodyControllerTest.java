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
import leap.lang.codec.Hex;
import leap.lang.http.MimeTypes;
import leap.lang.json.JSON;
import leap.web.WebTestCase;
import leap.webunit.client.THttpResponse;
import org.junit.Test;

public class RequestBodyControllerTest extends WebTestCase {

	@Test
	public void testStringBodyWithGet() {
		THttpResponse resp = forGet("/request_body/string_body").setBody("hello").send();
		resp.assertContentEquals("hello");

		resp = forGet("/request_body/string_body1?p1=x").setBody("hello").send();
		resp.assertContentEquals("hello:x");
	}

	@Test
	public void testStringBody() {
		THttpResponse resp = forPost("/request_body/string_body").setBody("hello").send();
		resp.assertContentEquals("hello");
		
		resp = forPost("/request_body/string_body1?p1=x").setBody("hello").send();
		resp.assertContentEquals("hello:x");
	}
	
	@Test
	public void testBytesBody() {
		THttpResponse resp = forPost("/request_body/bytes_body").setBody("hello").send();
		resp.assertContentEquals(Hex.encode("hello".getBytes()));
	}

    @Test
    public void testJsonBody() {
        Product product = new Product();
        product.setId(100);
        product.setTitle("Hello");

        String json = JSON.encode(product);

        THttpResponse resp = forPost("/request_body/json_body")
                .setContentType(MimeTypes.APPLICATION_JSON_TYPE)
                .setBody(json).send();

        resp.assertContentEquals(json);
    }
	
	@Test
	public void testEntityBody() {
		Product product = new Product();
		product.setId(100);
		product.setTitle("Hello");
		
		String json = JSON.encode(product);
		
		THttpResponse resp = forPost("/request_body/entity_body")
							.setContentType(MimeTypes.APPLICATION_JSON_TYPE)
							.setBody(json).send();
		
		resp.assertContentEquals(json);
	}

    @Test
	public void testPeekInputStream() {
        Product product = new Product();
        product.setId(100);
        product.setTitle("Hello");
        String json = JSON.encode(product);

        String content =
                forPost("/request_body/peek_input_stream")
                .setContentType(MimeTypes.APPLICATION_JSON_TYPE)
                .setBody(json)
                .send()
                .assertOk().getContent();

        assertEquals(content, json + json);

        forPost("/request_body/peek_input_stream_err")
                .setContentType(MimeTypes.APPLICATION_JSON_TYPE)
                .setBody(json)
                .send()
                .assert500();
    }
}