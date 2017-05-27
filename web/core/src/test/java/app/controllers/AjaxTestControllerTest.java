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

import org.junit.Test;

import leap.lang.http.Headers;
import leap.web.WebTestCase;
import leap.web.ajax.DefaultAjaxDetector;
import leap.webunit.client.THttpResponse;

public class AjaxTestControllerTest extends WebTestCase {
	
	@Test
	public void testIsAjax() {
		//Non ajax
		forGet("/ajax_test/get_is_ajax").send().assertOk().assertContentEquals("");
		
		forGet("/ajax_test/get_is_ajax")
			.setHeader(Headers.X_REQUESTED_WITH, DefaultAjaxDetector.ANDROID_41_X_REQUESTED_WITH_HEADER)
			.send().assertContentEquals("");		
		
		//Ajax
		forGet("/ajax_test/get_is_ajax")
			.setHeader(Headers.X_REQUESTED_WITH, "XMLHttpRequest")
			.send().assertContentEquals("ajax");
		// todo: is not empty string?
		/*
		forGet("/ajax_test/get_is_ajax")
			.setHeader(Headers.X_REQUESTED_WITH, "NotEmtptyString")
			.send().assertContentEquals("ajax");
		*/
		
	}
	
	@Test
	public void testExceptionWithChineseMessage() {
		THttpResponse resp = ajaxGet("/ajax_test/throw_exception");
		resp.assertContentContains("中文");
	}

}