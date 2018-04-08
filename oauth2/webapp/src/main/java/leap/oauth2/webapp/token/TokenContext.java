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

import leap.oauth2.webapp.token.at.AccessToken;
import leap.web.Request;

public class TokenContext {

    public static final String KEY = "oauth2.access_token";

    public static AccessToken getAccessToken() {
        Request request = Request.tryGetCurrent();
        return null == request ? null : getAccessToken(request);
    }

    public static AccessToken getAccessToken(Request request) {
        return (AccessToken)request.getAttribute(KEY);
    }

    public static void setAccessToken(Request request, AccessToken at) {
        request.setAttribute(KEY, at);
    }

    protected TokenContext() {

    }

}
