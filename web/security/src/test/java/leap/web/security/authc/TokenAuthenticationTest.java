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
package leap.web.security.authc;

import leap.lang.http.Cookie;
import leap.web.security.SecurityConstants;
import leap.web.security.SecurityTestCase;

import org.junit.Test;

public class TokenAuthenticationTest extends SecurityTestCase {

    private static String prefixWithDot(String s) {
        return s.startsWith(".") ? s : "." + s;
    }
	
	@Test
	public void testTokenCookieDomain() {
		client().addHostName("www.example.com");
		
		String cookieName = SecurityConstants.DEFAULT_TOKEN_AUTHENTICATION_COOKIE + "_app3";
		logout("/app3");
		client().removeCookie(cookieName);
		
		forLogin("/app3").sendAjax();
		Cookie cookie = client().getCookie(cookieName);
		assertNotNull(cookie);
		assertEquals("localhost", cookie.getDomain());
		
		logout("/app3");
		client().removeCookie(cookieName);
	
		forLogin("http://www.example.com:8080/app3").sendAjax();
		cookie = client().getCookie(cookieName);
		assertNotNull(cookie);
		assertEquals(".example.com", prefixWithDot(cookie.getDomain()));
	}
	
	@Test
	public void testNonCrossContextTokenAuthentication() {
		logout("/app1");
		logout("/app3");
		
		ajaxGet("/app1/").assert401();
		ajaxGet("/app3/").assert401();
		
		forLogin("/app1").sendAjax();
		forLogin("/app3").sendAjax();
		
		ajaxGet("/app1/").assertOk();
		ajaxGet("/app3/").assertOk();
		
		get("/app3/invalidate_session");
		ajaxGet("/app3/").assertOk();
		
		logout("/app3");
		ajaxGet("/app3/").assert401();
		ajaxGet("/app1/").assertOk();
		
		logout("/app1");
		ajaxGet("/app1/").assert401();
	}
	
	@Test
	public void testCrossContextTokenAuthentication() {
		logout("/app1");
		logout("/app2");
		logout("/app3");
		
		ajaxGet("/app1/").assert401();
		ajaxGet("/app2/").assert401();
		ajaxGet("/app3/").assert401();
		
		forLogin("/app1").sendAjax();
		ajaxGet("/app1/").assertOk();
		ajaxGet("/app2/").assertOk();
		ajaxGet("/app3/").assert401();
		
		get("/app1/invalidate_session");
		ajaxGet("/app1/").assertOk();
		
		logout("/app2");
		ajaxGet("/app2/").assert401();	
		ajaxGet("/app1/").assert401();
		
		forLogin("/app3").sendAjax();
		ajaxGet("/app3/").assertOk();
		ajaxGet("/app1/").assert401();
		ajaxGet("/app2/").assert401();
		logout("/app3");
	}

}
