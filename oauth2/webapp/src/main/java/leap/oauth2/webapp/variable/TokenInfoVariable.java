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

import leap.core.security.Authentication;
import leap.core.security.AuthenticationWrapper;
import leap.core.variable.Variable;
import leap.oauth2.webapp.authc.OAuth2Authentication;
import leap.web.Request;

/**
 * @author kael.
 */
public class TokenInfoVariable implements Variable {

    @Override
    public Object getValue() {
        Request request = Request.tryGetCurrent();
        if (null == request) {
            return null;
        }

        Authentication authc = request.getAuthentication();
        if (null == authc) {
            return null;
        }

        if (authc instanceof AuthenticationWrapper) {
            AuthenticationWrapper wrapper = (AuthenticationWrapper) authc;
            Authentication ac = wrapper.getWrapped();
            if (ac instanceof OAuth2Authentication) {
                return ((OAuth2Authentication) ac).getTokenInfo();
            }
        }

        if (!(authc instanceof OAuth2Authentication)) {
            return null;
        }

        return ((OAuth2Authentication) authc).getTokenInfo();
    }
}
