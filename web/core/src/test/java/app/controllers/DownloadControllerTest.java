/*
 * Copyright 2015 the original author or authors.
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

import java.net.URLEncoder;

import leap.lang.http.Headers;
import leap.web.WebTestCase;
import leap.webunit.client.THttpResponse;

import org.junit.Test;

public class DownloadControllerTest extends WebTestCase {
	
	@Test
	public void testDownloadTest() {
		THttpResponse response = get("/download/test");
		
		response.assertContentEquals("中文");
		
		String header = response.getHeader(Headers.CONTENT_DISPOSITION);
		assertNotEmpty(header);
		assertTrue(header.contains("filename=\"test.txt\""));
	}
	
	@Test
	public void testDownloadTest1() {
		THttpResponse response = get("/download/test1");
		
		response.assertContentEquals("中文");
		
		String header = response.getHeader(Headers.CONTENT_DISPOSITION);
		assertNotEmpty(header);
		assertTrue(header.contains("filename=\"test.txt\""));
	}
	
	@Test
	public void testDownloadTest2() throws Exception{
		THttpResponse response = get("/download/test2");
		
		response.assertContentEquals("中文");
		
		String header = response.getHeader(Headers.CONTENT_DISPOSITION);
		assertNotEmpty(header);
		assertTrue(header.contains("filename=\"" + URLEncoder.encode("中文文件.txt", "UTF-8") + "\""));
	}
	
	@Test
	public void testDownloadTest3() throws Exception {
	    THttpResponse response = get("/download/test3");
	    
	    response.assertContentEquals("中文流");
	    
        String header = response.getHeader(Headers.CONTENT_DISPOSITION);
        assertNotEmpty(header);
        assertTrue(header.contains("filename=\"stream.txt\""));
	}

	@Test
	public void testDownloadAny() throws Exception {
		THttpResponse response = get("/download/test.txt");
		response.assertContentEquals("中文");
		
		/*
		response = get("/download/中文名.txt");
		response.assertContentEquals("中文文字");
		*/
	}
	
}
