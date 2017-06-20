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

import leap.core.Session;
import leap.core.annotation.Inject;
import leap.oauth2.webapp.OAuth2Config;
import leap.oauth2.webapp.token.refresh.TokenRefresher;
import leap.web.Request;
import leap.web.security.authc.AuthenticationContext;

/**
 * Simple save the access token in session.
 */
public class DefaultTokenStore implements TokenStore {

    private static final String KEY = TokenDetails.class.getName();

    protected @Inject TokenRefresher refresher;

    @Override
    public TokenDetails loadAccessToken(Request request, AuthenticationContext context) {
        Session session = request.getSession(false);
        if(null == session) {
            return null;
        }

        return (TokenDetails)session.getAttribute(KEY);
    }

    @Override
    public void saveAccessToken(Request request, AuthenticationContext context, TokenDetails at) {
        request.getSession(true).setAttribute(KEY, at);
    }

    @Override
    public TokenDetails refreshAndSaveAccessToken(Request request, AuthenticationContext context, TokenDetails old) {
        TokenDetails theNew = refresher.refreshAccessToken(old);

        saveAccessToken(request, context, theNew);

        return theNew;
    }

}