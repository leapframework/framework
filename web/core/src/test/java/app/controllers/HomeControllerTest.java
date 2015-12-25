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
package app.controllers;

import leap.junit.contexual.Contextual;
import leap.lang.Strings;
import leap.lang.http.HTTP;
import leap.lang.http.MimeTypes;
import leap.web.WebTestCase;
import leap.webunit.client.THttpResponse;

import org.junit.Test;

@Contextual
public class HomeControllerTest extends WebTestCase {
	
	@Test
	public void testInject() {
		get("/test_inject").assertOk();
	}
	
	@Test
	public void testHomeControllerIndex(){
		response = get("");
		
		assertTrue(response.isOk());
		assertEquals("UTF-8",Strings.upperCase(response.getCharset()));
		assertEquals(MimeTypes.TEXT_HTML, response.getMediaType());
		assertEquals("<h1>Hello world!</h1>", response.getContent());
		
		get("/index").assertContentEquals("<h1>Hello world!</h1>");
		get("/index.do").assertContentEquals("<h1>Hello world!</h1>");
		//TODO : get("/index;jsessionid=x").assertContentEquals("<h1>Hello world!</h1>");
	}

	@Test
	public void testHomeControllerText(){
		response = get("/text");
		
		assertTrue(response.isOk());
		assertEquals("UTF-8",Strings.upperCase(response.getCharset()));
		assertEquals(MimeTypes.TEXT_PLAIN, response.getMediaType());
		assertEquals("Hello world!", response.getContent());
	}
	
	@Test
	public void testHomeControllerHtml(){
		response = get("/html"); 

		assertTrue(response.isOk());
		assertEquals("UTF-8",Strings.upperCase(response.getCharset()));
		assertEquals(MimeTypes.TEXT_HTML, response.getMediaType());
		assertEquals("<h1>Hello world!</h1>", response.getContent());
	}
	
	@Test
	public void testHomeControllerHtml1(){
		response = get("/html1"); 

		assertTrue(response.isOk());
		assertEquals("UTF-8",Strings.upperCase(response.getCharset()));
		assertEquals(MimeTypes.TEXT_HTML, response.getMediaType());
		assertEquals("<h1>Hello world!</h1>", response.getContent());
	}
	
	@Test
	public void testHomeControllerHtml2(){
		response = get("/html2"); 

		assertTrue(response.isOk());
		assertEquals("UTF-8",Strings.upperCase(response.getCharset()));
		assertEquals(MimeTypes.TEXT_HTML, response.getMediaType());
		assertEquals("<h1>Hello world!</h1>", response.getContent());
	}
	
	@Test
	public void testHomeControllerHtml3(){
		response = get("/html3"); 

		assertTrue(response.isOk());
		assertEquals("UTF-8",Strings.upperCase(response.getCharset()));
		assertEquals(MimeTypes.TEXT_HTML, response.getMediaType());
		assertEquals("<h1>Hello world!</h1>", response.getContent());
	}
	
	@Test
	public void testHomeControllerJsonString() {
		response = get("/json_string");
		
		response.assertOk()
				.assertContentTypeEquals(MimeTypes.APPLICATION_JSON)
				.assertContentEquals("\"Hello json\"");
	}
	
	@Test
	public void testHomeControllerNoContent(){
		response = get("/no_content"); 
		assertTrue(response.getStatus() == HTTP.SC_NO_CONTENT);
	}
	
	@Test
	public void testHomeControllerNothing(){
		response = get("/nothing"); 
		assertTrue(response.getStatus() == HTTP.SC_OK);
		assertEquals("", response.getContent());
	}
	
	@Test
	public void testHomeControllerNotImplemented(){
		response = get("/not_implemented"); 
		assertTrue(response.getStatus() == HTTP.SC_NOT_IMPLEMENTED);
	}
	
	@Test
	public void testHomeControllerPostAction(){
		get("/post_action");
		assertTrue(!response.isSuccess());
		
		post("/post_action");
		assertTrue(response.isOk());
		assertEquals("METHOD:POST",response.getContent());
	}
	
	@Test
	public void testHomeControllerRawResponse(){
		response = get("/raw_response");
		assertTrue(response.isOk());
		assertEquals("UTF-8",Strings.upperCase(response.getCharset()));
		assertEquals(MimeTypes.TEXT_PLAIN, response.getMediaType());
		assertEquals("RawResponse", response.getContent());
	}
	
	@Test
	public void testHomeControllerRenderView(){
		response = get("/render_view");
		assertTrue(response.isOk());
		assertEquals("UTF-8",Strings.upperCase(response.getCharset()));
		assertEquals(MimeTypes.TEXT_HTML, response.getMediaType());
		assertEquals("<h1>Hello world!</h1>", response.getContent());
	}
	
	@Test
	public void testHomeControllerRedirectTo(){
		response = get("/redirect_to");
		assertTrue(response.getStatus() == HTTP.SC_FOUND);
		
		String redirectUrl = response.getLocation();
		assertTrue(redirectUrl.endsWith("/redirect_to_notfound_url"));
	}
	
	@Test
	public void testHomeControllerRedirectTo1(){
		response = get("/redirect_to1");
		assertTrue(response.getStatus() == HTTP.SC_FOUND);
		
		String redirectUrl = response.getLocation();
		assertTrue(redirectUrl.endsWith("/redirect_to_notfound_url"));
	}
	
	@Test
	public void testHomeControllerRedirectTo3(){
		response = get("/redirect_to3");
		assertTrue(response.getStatus() == HTTP.SC_FOUND);
		
		String redirectUrl = response.getLocation();
		assertTrue(redirectUrl.endsWith("/redirect_to_notfound_url"));
	}
	
	@Test
	public void testForwardToView(){
		response = get("/forward_to");
		assertTrue(response.isOk());
		assertEquals(MimeTypes.TEXT_HTML, response.getMediaType());
		assertEquals("forward", response.getContent());
	}
	
	@Test
	public void testForwardToResoruce(){
		response = get("/forward_to1");
		assertTrue(response.isOk());
		assertEquals(MimeTypes.TEXT_HTML, response.getMediaType());
		assertEquals("forward1", response.getContent());
	}
	
	@Test
	public void testNonAction() {
		get("/non_action").assertNotFound();
	}
	
	@Test
	public void testException1() {
		THttpResponse resp = ajaxGet("/exception1");
		resp.assertStatusEquals(500).assertContentEquals("Test Exception");
	}
	
	@Test
	public void testGetControllerPath() {
		get("/controller_path").assertContentEquals("");
	}
	
	@Test
	public void testArbitraryPath() {
		get("/arbitrary_path/p1").assertContentEquals("p1");
		get("/arbitrary_path/p1/p2").assertContentEquals("p1/p2");
		get("/arbitrary_path/f1.doc").assertContentEquals("f1.doc");
		get("/arbitrary_path/p1/f1.doc").assertContentEquals("p1/f1.doc");
		get("/arbitrary_path/p1/p2/f1.doc").assertContentEquals("p1/p2/f1.doc");	
	}
	
	@Test
	public void testJspView() {
	    String content = get("/jsp").getContent();
	    assertEquals("jsp", Strings.trim(content));
	}
	
	@Test
	public void testHttpsOnly() {
	    get("/https_only").assert400();
	    httpsClient().get("/https_only").assertOk().assertContentEquals("OK");
	}
}