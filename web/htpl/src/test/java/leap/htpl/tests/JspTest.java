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
package leap.htpl.tests;

import org.junit.Test;

import leap.htpl.HtplTestCase;
import leap.lang.jsoup.nodes.Document;
import leap.lang.jsoup.nodes.Element;
import leap.webunit.client.THttpResponse;

public class JspTest extends HtplTestCase {

	@Test
	public void testJspIncludeHtml() {
		THttpResponse response = client().get("/tags/include");
		
		response.assertContentTypeHtml().assertContentContains("<h1>Hello world!</h1>");
		
		Document html = response.getDocument();
		Element testHref = html.getElementById("testHref");
		assertNotNull(testHref);
		assertEquals("/test", testHref.attr("href"));
		
		get("/tags/include");
	}
	
	@Test
	public void testHtmlIncludeJsp() {
		get("/jsp/index").assertContentContains("Jsp include 100")
						 .assertContentContains("Jsp include 101");
	}
	
	@Test
	public void testHtmlAutoIncludeJsp() {
		get("/jsp/index1?$debug=0").assertContentEquals("Jsp auto include 100");
		get("/jsp/index2?$debug=0").assertContentEquals("Jsp auto include 100");
		
		get("/jsp/index3").assertContentContains("Jsp auto include 100")
						  .assertContentContains("Jsp auto include layout 100")
						  .assertContentContains("Jsp auto include 103");
	}
}
