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
package leap.webunit;

import leap.lang.jsoup.nodes.Document;
import leap.lang.jsoup.nodes.Element;
import leap.webunit.client.THttpResponse;
import org.junit.Test;

public class BaseTest extends WebTestBase {

	@Test
	public void testWebServerStarted(){
		assertTrue(server.getServletContexts().size() > 0);
		assertTrue(server.isAllWebAppsAvailable());
		get("").assertSuccess();
	}
	
	@Test
	public void testHttpsWebServerStarted() {
	    runHttps(() -> get("").assertSuccess() );
	}
	
	@Test
	public void testHtmlDocument(){
		THttpResponse response = client().get("/html.jsp");
		
		Document document = response.getDocument();
		assertEquals("test html document", document.title());
		
		Element text = document.getElementById("text");
		assertNotNull(text);
		assertEquals("Hello",text.text());
	}
	
	@Test
	public void testDnsHostName() {
		client().addHostName("www.example.com");
		
        assertTestDnsHostNameResponse(client().get("http://www.example.com:8080/html.jsp"));
        assertTestDnsHostNameResponse(client().get("http://www.example.com:8081/html.jsp"));

        assertTestDnsHostNameResponse(client().get("https://www.example.com:8443/html.jsp"));
        assertTestDnsHostNameResponse(client().get("https://www.example.com:8444/html.jsp"));
	}

    private void assertTestDnsHostNameResponse(THttpResponse response) {
        Document document = response.getDocument();
        assertEquals("test html document", document.title());

        Element text = document.getElementById("text");
        assertNotNull(text);
        assertEquals("Hello",text.text());
    }
}
