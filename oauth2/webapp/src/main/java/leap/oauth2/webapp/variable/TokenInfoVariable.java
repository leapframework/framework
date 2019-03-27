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

package leap.oauth2.webapp.variable;

import leap.core.variable.Variable;
import leap.oauth2.webapp.authc.DefaultOAuth2Authenticator;
import leap.oauth2.webapp.token.TokenInfo;
import leap.web.Request;

import java.util.Optional;

/**
 * @author kael.
 */
public class TokenInfoVariable implements Variable {
    @Override
    public Object getValue() {
        TokenInfo tokenInfo = Optional.ofNullable(Request.tryGetCurrent())
                .map(request -> (TokenInfo)request.getAttribute(DefaultOAuth2Authenticator.TOKENINFO_ATTR_NAME))
                .orElse(null);
        return tokenInfo;
    }
}
