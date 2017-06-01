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
package leap.web.security.tests;

import leap.web.security.SecurityTestCase;

import org.junit.Test;

public class LoginLogoutTest extends SecurityTestCase {

    @Test
    public void testLoginReturnUrl() {
        String returnUrlValue = "value=\"/test_return_url?q=1\"";
        
        get("/login").assertContentContains("value=\"/\"");
        get("/test_return_url?q=1").assertContentContains(returnUrlValue);
        
        
        returnUrlValue = "value=\"/test_return_url\"";
        get("/test_return_url").assertContentContains(returnUrlValue);
    }
    
}
