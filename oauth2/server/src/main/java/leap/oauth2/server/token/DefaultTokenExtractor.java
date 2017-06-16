/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.oauth2.server.token;

import leap.lang.Strings;
import leap.lang.http.Headers;
import leap.oauth2.OAuth2Constants;
import leap.web.Request;

public class DefaultTokenExtractor implements TokenExtractor {

    @Override
    public Token extractTokenFromRequest(Request request) {
        String v = request.getHeader(Headers.AUTHORIZATION);
        if (Strings.isEmpty(v)) {
            v = request.getParameter(OAuth2Constants.ACCESS_TOKEN);
        } else if (Strings.startsWithIgnoreCase(v, OAuth2Constants.BEARER_TYPE)) {
            v = v.substring(OAuth2Constants.BEARER_TYPE.length()).trim();
        }

        if (Strings.isEmpty(v)) {
            return null;
        }

        return new SimpleToken(v);
    }

    protected static final class SimpleToken implements Token {

        private final String value;

        public SimpleToken(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

}
