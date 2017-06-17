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

package leap.oauth2.webapp.token;

import leap.core.security.token.SimpleTokenCredentials;
import leap.lang.Strings;
import leap.oauth2.OAuth2Constants;

import java.util.Map;

public class JwtAccessToken extends SimpleTokenCredentials implements AccessToken  {

    protected final String type;
    protected final boolean bearer;

    public JwtAccessToken(String type, String token) {
        super(token);
        this.type = type;
        this.bearer = Strings.isEmpty(type) || OAuth2Constants.BEARER_TYPE.equalsIgnoreCase(type);;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public boolean isBearer() {
        return this.bearer;
    }

    public boolean isJwt() {
        return true;
    }
}
