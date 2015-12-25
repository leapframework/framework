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
import leap.web.pjax.DefaultPjaxDetector;

import org.junit.Test;

public class PjaxTestControllerTest extends WebTestCase {
	
	@Test
	public void testIsPjax() {
		//Non pjax
		forGet("/pjax_test/get_is_pjax").send().assertOk().assertContentEquals("");	
		
		//pjax
		forGet("/pjax_test/get_is_pjax")
			.setHeader(DefaultPjaxDetector.X_PJAX_HEADER, "true")
			.send().assertContentEquals("pjax");
		
		forGet("/pjax_test/get_is_pjax")
			.setHeader(DefaultPjaxDetector.X_PJAX_HEADER, "NotEmtptyString")
			.send().assertContentEquals("pjax");		
		
	}
}