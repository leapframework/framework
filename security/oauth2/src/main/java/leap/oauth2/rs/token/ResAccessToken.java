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
package leap.oauth2.rs.token;

import leap.core.security.token.TokenCredentials;
import leap.web.security.authc.credentials.ParameterizedCredentials;

public interface ResAccessToken extends TokenCredentials, ParameterizedCredentials {
    
    /**
     * Optional. The token type.
     */
    String getType();

    /**
     * Returns the parameter value or <code>null</code>.
     */
    Object getParameter(String name);
    
    /**
     * Returns <code>true</code> if the token is <code>'Bearer'</code> type.
     */
    boolean isBearer();

    /**
     * Returns <code>true</code> if the token is a jwt token.
     * @return
     */
    boolean isJwt();
}