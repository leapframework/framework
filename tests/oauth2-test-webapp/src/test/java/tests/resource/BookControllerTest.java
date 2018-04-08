/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  
 */
package tests.resource;

import org.junit.Test;
import tests.OAuth2TestBase;
import tests.TokenResponse;

public class BookControllerTest extends OAuth2TestBase {
    
	@Test
	public void testBadRequest() {
	    logout("");
		ajaxGet("/book").assertNotOk();

	 	//resp.assert400();
	 	/*
		JsonValue json = resp.getJson();
		assert(json.isMap());
		
		assertEquals(json.asMap().get("error"), OAuth2Errors.ERROR_INVALID_REQUEST);
		*/
	}
	
    @Test
    public void testRemoteAccessTokenStore() {
        TokenResponse token = obtainAccessTokenImplicit();
        withAccessToken(useGet("/book"), token.accessToken).send().assertOk();
    }

}