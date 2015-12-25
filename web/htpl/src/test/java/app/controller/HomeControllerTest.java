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
package app.controller;

import leap.htpl.HtplTestCase;
import leap.lang.jsoup.nodes.Document;
import leap.lang.jsoup.nodes.Element;
import leap.webunit.client.THttpResponse;

import org.junit.Test;

public class HomeControllerTest extends HtplTestCase {

	@Test
	public void testIndex(){
		THttpResponse response = client().get("/");
		
		response.assertContentTypeHtml().assertContentContains("<h1>Hello world!</h1>").assertContentContains("<html");
		
		Document html = response.getDocument();
		Element testHref = html.getElementById("testHref");
		assertNotNull(testHref);
		assertEquals("/test", testHref.attr("href"));
	}
	
	@Test
	public void testIndexPjax() {
		
		THttpResponse response = forGet("/").setHeader("X-PJAX", "1").send();
		
		response.assertContentTypeHtml().assertContentContains("<h1>Hello world!</h1>");
		
		assertFalse(response.getContent().contains("<html"));
		
		Document html = response.getDocument();
		Element testHref = html.getElementById("testHref");
		assertNotNull(testHref);
		assertEquals("/test", testHref.attr("href"));
		
	}
	
	@Test
	public void testRenderView() {
		get("/html").assertContentContains("<html>");
	}
	
}