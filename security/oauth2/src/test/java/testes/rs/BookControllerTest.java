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
package testes.rs;

import testes.OAuth2TestBase;
import testes.TokenResponse;
import leap.webunit.client.THttpResponse;

import org.junit.Test;

public class BookControllerTest extends OAuth2TestBase {
    
	@Test
	public void testBadRequest() {
	    serverContextPath = "";
	    
	    logout();
		THttpResponse resp = ajaxGet("/book");
		resp.assertNotOk();
		
	 	//resp.assert400();
	 	/*
		JsonValue json = resp.getJson();
		assert(json.isMap());
		
		assertEquals(json.asMap().get("error"), OAuth2Errors.ERROR_INVALID_REQUEST);
		*/
	}
	
	@Test
	public void testLocalAccessTokenStore() {
	    serverContextPath = "";
	    
	    login();
	    
	    TokenResponse token = obtainAccessTokenImplicit();
	    
	    withAccessToken(forGet("/book"), token.accessToken).send().assertOk();
	}
	
    @Test
    public void testRemoteAccessTokenStore() {
        serverContextPath = "/server";
        
        login();

        TokenResponse token = obtainAccessTokenImplicit();

        withAccessToken(forGet("/resapp/book"), token.accessToken).send().assertOk();
    }

}