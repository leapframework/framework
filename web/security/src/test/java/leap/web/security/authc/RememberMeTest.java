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

import leap.web.security.SecurityConstants;
import leap.web.security.SecurityTestCase;

import org.junit.Ignore;
import org.junit.Test;

@Ignore //todo: failed when running full unit test.
public class RememberMeTest extends SecurityTestCase {
	
	@Test
	public void testNonCrossContextRememberMe() {
		logout("/app1");
		logout("/app3");
		
		ajaxGet("/app1/").assert401();
		ajaxGet("/app3/").assert401();
		
		forLogin("/app1").addFormParam(SecurityConstants.DEFAULT_REMEMBERME_PARAMETER, "1").sendAjax();
		forLogin("/app3").addFormParam(SecurityConstants.DEFAULT_REMEMBERME_PARAMETER, "1").sendAjax();
		
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
	public void testCrossContextRememberMe() {
		logout("/app1");
		logout("/app2");
		logout("/app3");
		
		ajaxGet("/app1/").assert401();
		ajaxGet("/app2/").assert401();
		ajaxGet("/app3/").assert401();
		
		forLogin("/app1").addFormParam(SecurityConstants.DEFAULT_REMEMBERME_PARAMETER, "1").sendAjax();
		ajaxGet("/app1/").assertOk();
		ajaxGet("/app2/").assertOk();
		ajaxGet("/app3/").assert401();
		
		get("/app1/invalidate_session");
		ajaxGet("/app1/").assertOk();
		
		logout("/app2");
		ajaxGet("/app2/").assert401();	
		get("/app1/invalidate_session");
		ajaxGet("/app1/").assert401();
		
		forLogin("/app3").addFormParam(SecurityConstants.DEFAULT_REMEMBERME_PARAMETER, "1").sendAjax();
		ajaxGet("/app3/").assertOk();
		ajaxGet("/app1/").assert401();
		ajaxGet("/app2/").assert401();
		logout("/app3");
	}
	
}
