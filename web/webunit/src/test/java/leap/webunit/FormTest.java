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
package leap.webunit;

import java.io.IOException;

import leap.webunit.client.THttpMultipart;
import leap.webunit.client.THttpResponse;

import org.apache.http.entity.ContentType;
import org.junit.Test;

public class FormTest extends WebTestBase {

	@Test
	public void testFormPost(){
		post("/form.jsp","test","Hello").assertContentEquals("Hello");
	}
	
	@Test
	public void testMultipartForm() throws IOException {
		THttpMultipart multipart = client().request("/upload").multipart();
		
        multipart.addBytes("bin", new byte[]{1})
    	         .addText("comment", "A binary file of some kind", ContentType.TEXT_PLAIN.getMimeType());
    
        THttpResponse response = multipart.send();

        response.assertContentEquals("bin,comment");
	}

}