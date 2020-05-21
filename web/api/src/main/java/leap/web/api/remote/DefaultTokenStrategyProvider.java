/*
 * Copyright 2020 the original author or authors.
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

package leap.web.api.remote;

import leap.core.security.Authentication;
import leap.lang.http.HTTP;
import leap.lang.http.client.HttpRequest;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.oauth2.webapp.authc.OAuth2Authentication;
import leap.oauth2.webapp.code.DefaultCodeVerifier;
import leap.oauth2.webapp.token.TokenInfo;
import leap.oauth2.webapp.token.at.AccessToken;
import leap.oauth2.webapp.token.at.SimpleAccessToken;
import leap.web.Request;
import net.jodah.expiringmap.ExpiringMap;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class DefaultTokenStrategyProvider extends DefaultCodeVerifier implements TokenStrategyProvider {

    private static final Log log = LogFactory.get(DefaultTokenStrategyProvider.class);

    protected TokenStrategy originalTokenStrategy     = new OriginalTokenStrategy();
    protected TokenStrategy forceWithAppTokenStrategy = new WithAppTokenStrategy(true);
    protected TokenStrategy tryWithAppTokenStrategy   = new WithAppTokenStrategy(false);
    protected TokenStrategy appOnlyTokenStrategy      = new AppOnlyTokenStrategy();

    @Override
    public TokenStrategy getOriginalStrategy() {
        return originalTokenStrategy;
    }

    @Override
    public TokenStrategy getForceWithAppStrategy() {
        return forceWithAppTokenStrategy;
    }

    @Override
    public TokenStrategy getTryWithAppStrategy() {
        return tryWithAppTokenStrategy;
    }

    @Override
    public TokenStrategy getAppOnlyStrategy() {
        return appOnlyTokenStrategy;
    }

    protected AccessToken fetchAccessTokenWithApp(String at) {
        if (null == config.getTokenUrl()) {
            return null;
        }

        HttpRequest request = httpClient.request(config.getTokenUrl())
                .addQueryParam("grant_type", "token_client_credentials")
                .addQueryParam("access_token", at)
                .setMethod(HTTP.Method.POST);

        return fetchAccessToken(request);
    }

    protected AccessToken fetchAppOnlyAccessToken() {
        if (null == config.getTokenUrl()) {
            return null;
        }

        HttpRequest request = httpClient.request(config.getTokenUrl())
                .addQueryParam("grant_type", "client_credentials")
                .setMethod(HTTP.Method.POST);

        return fetchAccessToken(request);
    }

    public AccessToken refreshAppOnlyAccessToken(AccessToken old) {
        HttpRequest request = httpClient.request(config.getTokenUrl())
                .addQueryParam("grant_type", "refresh_token")
                .addQueryParam("refresh_token", old.getRefreshToken())
                .setMethod(HTTP.Method.POST);
        return fetchAccessToken(request);
    }

    protected class OriginalTokenStrategy implements TokenStrategy {

        @Override
        public Token getToken() {
            final Request request = Request.tryGetCurrent();
            if (null == request) {
                return appOnlyTokenStrategy.getToken();
            }

            final Authentication authc = request.getAuthentication();
            if (null == authc || !(authc instanceof OAuth2Authentication)) {
                return appOnlyTokenStrategy.getToken();
            }

            return doGetToken((OAuth2Authentication) authc);
        }

        protected Token doGetToken(OAuth2Authentication oauthc) {
            return new TokenImpl(new SimpleAccessToken(oauthc.getToken()));
        }
    }

    protected class WithAppTokenStrategy extends OriginalTokenStrategy {
        private boolean                        force;
        private ExpiringMap<String, TokenImpl> tokens =
                ExpiringMap.builder().maxSize(5000).expiration(8, TimeUnit.HOURS).build(); //todo: configure it

        public WithAppTokenStrategy(boolean force) {
            this.force = force;
        }

        @Override
        protected Token doGetToken(OAuth2Authentication oauthc) {
            final String at = oauthc.getCredentials().getToken();
            if (force || !oauthc.hasClient()) {
                TokenImpl token = tokens.get(at);
                if (null == token || token.isExpired()) {
                    final AccessToken appAccessToken = fetchAccessTokenWithApp(at);
                    if (null != appAccessToken) {
                        final AccessToken newAccessToken = new WithAppAccessToken(oauthc.getTokenInfo(), appAccessToken);
                        token = new TokenImpl(newAccessToken);
                        tokens.put(at, token);
                    }
                }
                if (null != token) {
                    return token;
                }
            }
            return new TokenImpl(new SimpleAccessToken(at));
        }
    }

    protected class AppOnlyTokenStrategy implements TokenStrategy {
        private volatile TokenImpl token;

        public Token getToken() {
            if (null == token || token.isExpired()) {
                return fetch();
            } else {
                return token;
            }
        }

        protected Token fetch() {
            AccessToken at = fetchAppOnlyAccessToken();
            return null == at ? null : (token = new TokenImpl(at, this::refresh));
        }

        protected Token refresh(TokenImpl t) {
            AccessToken at;
            try {
                at = refreshAppOnlyAccessToken(t.at);
            } catch (Exception e) {
                log.error("Error refresh token '{}' by '{}'", t.getValue(), t.at.getRefreshToken(), e);
                at = fetchAppOnlyAccessToken();
            }
            return new TokenImpl(at, this::refresh);
        }
    }

    protected static class WithAppAccessToken extends SimpleAccessToken {
        private final TokenInfo info;

        public WithAppAccessToken(TokenInfo originalTokenInfo, AccessToken appAccessToken) {
            super(appAccessToken.getToken());
            this.info = originalTokenInfo;
        }

        @Override
        public boolean isExpired() {
            return info.isExpired();
        }
    }

    protected static class TokenImpl implements Token {
        private final AccessToken                at;
        private final Function<TokenImpl, Token> refresh;

        public TokenImpl(AccessToken at) {
            this(at, null);
        }

        public TokenImpl(AccessToken at, Function<TokenImpl, Token> refresh) {
            this.at = at;
            this.refresh = refresh;
        }

        @Override
        public String getValue() {
            return at.getToken();
        }

        @Override
        public Token refresh() {
            return null == refresh ? null : refresh.apply(this);
        }

        public boolean isExpired() {
            return at.isExpired();
        }
    }
}
