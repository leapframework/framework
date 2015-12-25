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

import leap.lang.http.Cookie;
import leap.web.WebTestCase;

import org.junit.Test;

public class CaptchaControllerTest extends WebTestCase {

	@Test
	public void testSimpleCaptchaVerify() {
		String token = "abcd";
		
		get("/captcha?token=" + token).assertOk();
		
		Cookie cookie = client().getCookie("captcha_root");
		assertNotNull(cookie);
		assertNotEmpty(cookie.getValue());
		
		get("/captcha/verify?text=" + token).assertContentEquals("ok");
		get("/captcha/verify?text=" + token).assertContentEquals("failed");
		
		get("/captcha?token=" + token).assertOk();
		get("/captcha/verify?text=" + "ccc").assertContentEquals("failed");
	}
	
}
