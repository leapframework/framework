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

package server;import leap.lang.net.Urls;

public interface OAuth2TestData {

    String TEST_CLIENT_ID                   = "test";
    String TEST_CLIENT_SECRET               = "test_secret";
    String TEST_CLIENT_GRANTED_SCOPE		= "admin:test";
    String TEST_CLIENT_REDIRECT_URI         = "/oauth2/redirect_uri";
    String TEST_CLIENT_REDIRECT_URI_ENCODED = Urls.encode(TEST_CLIENT_REDIRECT_URI);

    String USER_ADMIN    = "admin";
    String PASS_ADMIN    = "1";
    String USER_XIAOMING = "xiaoming";
    String PASS_XIAOMING = "123";

}
