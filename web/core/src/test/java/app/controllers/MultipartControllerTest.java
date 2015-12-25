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

import leap.web.WebTestCase;
import leap.webunit.client.THttpMultipart;
import leap.webunit.client.THttpResponse;

import org.apache.http.entity.ContentType;
import org.junit.Test;

public class MultipartControllerTest extends WebTestCase {

	@Test
	public void testUpload0() {
		THttpMultipart form = client().request("/multipart/upload0").multipart();
		
        form.addBytes("bin", new byte[]{1})
    	.addText("comment", "A binary file of some kind", ContentType.TEXT_PLAIN.getMimeType());
    
        THttpResponse response = form.send();

        response.assertContentEquals("bin,comment");
	}
	
	@Test
	public void testUpload1() {
		THttpMultipart form = client().request("/multipart/upload1").multipart();
	
		form.addText("s1", "Hello");
		form.addBytes("p1", "World!".getBytes());
		
		form.send().assertContentEquals("Hello World!");
	}
	
	@Test
	public void testUploadFile() {
		THttpMultipart form = client().request("/multipart/upload_file").multipart();
	
		form.addFile("file1", "Hello", "hello.txt");
		
		form.send().assertContentEquals("hello.txt!Hello");
	}
}