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

package tests.as;

import leap.oauth2.OAuth2Errors;
import leap.webunit.client.THttpResponse;
import org.junit.Test;
import tests.OAuth2TestBase;

/**
 * Created by kael on 2017/3/1.
 */
public class I18NTest extends OAuth2TestBase {
    
    protected static final String SERVER_URL="/server/oauth2_error_resp";
    
    @Test
    public void testInvalidRequest(){
        String errorUri = SERVER_URL+"?key="+ OAuth2Errors.ERROR_INVALID_REQUEST_KEY;
        THttpResponse response = post(errorUri);
        
        String message = response.assertFailure().getJson().asJsonObject().getString("error_description");
        assertEquals("无效的请求。",message);
        errorUri = errorUri+"&locale=en_US";
        response = post(errorUri);
        message = response.assertFailure().getJson().asJsonObject().getString("error_description");
        assertEquals("The request is invalid.",message);

        errorUri = SERVER_URL+"?key=not_exit_key";
        response = post(errorUri);
        message = response.assertFailure().getJson().asJsonObject().getString("error_description");
        assertEquals("defaultValue",message);
    }
}
