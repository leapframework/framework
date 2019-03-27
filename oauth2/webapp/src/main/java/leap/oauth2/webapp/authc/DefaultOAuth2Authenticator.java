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
package leap.oauth2.webapp.authc;

import leap.core.AppConfigException;
import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.cache.Cache;
import leap.core.cache.CacheManager;
import leap.core.ioc.PostCreateBean;
import leap.core.security.ClientPrincipal;
import leap.core.security.UserNotFoundException;
import leap.core.security.UserPrincipal;
import leap.lang.Strings;
import leap.lang.expirable.TimeExpirableMs;
import leap.lang.expirable.TimeExpirableSeconds;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.oauth2.webapp.OAuth2Config;
import leap.oauth2.webapp.client.OAuth2Client;
import leap.oauth2.webapp.token.Token;
import leap.oauth2.webapp.token.TokenInfo;
import leap.oauth2.webapp.token.TokenInfoLookup;
import leap.oauth2.webapp.token.TokenVerifier;
import leap.oauth2.webapp.user.UserDetailsLookup;
import leap.oauth2.webapp.user.UserInfoLookup;
import leap.web.Request;

import java.util.Map;
import java.util.Optional;

/**
 * Default implementation of {@link OAuth2Authenticator}.
 */
public class DefaultOAuth2Authenticator implements OAuth2Authenticator, PostCreateBean {
    public static final String TOKENINFO_ATTR_NAME = DefaultOAuth2Authenticator.class.getName()+"$tokeninfo";
    
    private static final Log log = LogFactory.get(DefaultOAuth2Authenticator.class);
    
    protected @Inject OAuth2Config      config;
    protected @Inject TokenInfoLookup   tokenInfoLookup;
    protected @Inject UserInfoLookup    userInfoLookup;
    protected @Inject UserDetailsLookup userDetailsLookup;
    protected @Inject CacheManager      cacheManager;

    protected Map<String, TokenVerifier>          typedAccessTokenVerifiers;
    protected Cache<String, CachedAuthentication> cache;
    protected int                                 cacheSize        = 2048;              //caches max {cacheSize} access tokens.
    protected int                                 cacheExpiresInMs = 120 * 1000; //2 minutes

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public void setCacheExpiresInMs(int cacheExpiresInMs) {
        this.cacheExpiresInMs = cacheExpiresInMs;
    }

    @Override
    public void postCreate(BeanFactory factory) throws Throwable {
        cache = cacheManager.createSimpleLRUCache(cacheSize);
        typedAccessTokenVerifiers = factory.getNamedBeans(TokenVerifier.class);
    }

    @Override
    public OAuth2Authentication authenticate(Token at) {
        //Resolve from cache.
        CachedAuthentication cached = getCachedAuthentication(at);
        if (null != cached) {

            //Check expiration of token.
            if (cached.isTokenExpired()) {
                log.debug("Access token '{}' was expired", at.getToken());
                removeCachedAuthentication(at, cached);
                return null;
            }

            //Check expiration of the cached item.
            if (cached.isCacheExpired()) {
                log.debug("Cached authentication expired, remove it from cache only");
                removeCachedAuthentication(at, cached);
            } else {
                log.debug("Returns the cached authentication of access token : {}", at.getToken());
                Optional.ofNullable(Request.tryGetCurrent())
                        .ifPresent(request -> request.setAttribute(TOKENINFO_ATTR_NAME, cached.tokenInfo));
                return cached.authentication;
            }
        }

        //verify access token and returns token info.
        TokenInfo tokenInfo;

        if (!Strings.isEmpty(at.getType())) {
            TokenVerifier verifier = typedAccessTokenVerifiers.get(at.getType());
            if (null == verifier) {
                throw new AppConfigException("Cannot handle access token type '" + at.getType() + "'");
            }
            tokenInfo = verifier.verifyToken(at);
        } else {
            tokenInfo = tokenInfoLookup.lookupByAccessToken(at.getToken());
        }

        if (null == tokenInfo) {
            log.info("Access token '{}' not found", at.getToken());
            return null;
        }

        if (tokenInfo.isExpired()) {
            log.info("Access token '{}' was expired", at.getToken());
            return null;
        }

        //creates authentication.
        String clientId = tokenInfo.getClientId();
        String userId   = tokenInfo.getUserId();

        UserPrincipal   user   = tokenInfo.getUserInfo();
        ClientPrincipal client = null;

        if (null != user) {
            user = lookupUserDetails(user);
        } else if (!Strings.isEmpty(userId)) {
            //user info lookup
            log.debug("lookup user info at oauth2 server");
            user = userInfoLookup.lookupUserInfo(at.getToken(), userId);
            if (null == user) {
                log.error("User info not exists in oauth2 server, user id -> {}, access token -> {}", userId, at.getToken());
                throw new UserNotFoundException();
            }
            user = lookupUserDetails(user);
        }

        if (!Strings.isEmpty(clientId)) {
            client = new OAuth2Client(clientId, tokenInfo.getClaims());
        }

        OAuth2Authentication authc = new SimpleOAuth2Authentication(at, user, client);
        if (null != tokenInfo.getScope()) {
            authc.setPermissions(Strings.split(tokenInfo.getScope(), ',', ' '));
        }
        
        cacheAuthentication(at, tokenInfo, authc);
        Optional.ofNullable(Request.tryGetCurrent())
                .ifPresent(request -> request.setAttribute(TOKENINFO_ATTR_NAME, tokenInfo));
        return authc;
    }

    protected UserPrincipal lookupUserDetails(UserPrincipal user) {
        if (null != userDetailsLookup && userDetailsLookup.isEnabled()) {
            log.debug("lookup user details by '{}'", userDetailsLookup.getClass().getSimpleName());
            user = userDetailsLookup.lookupUserDetails(user.getIdAsString(), user.getName(), user.getLoginName());
            if (null == user) {
                log.error("User details '{}' '{}' not found", user.getId(), user.getLoginName());
                throw new UserNotFoundException();
            }
        }
        return user;
    }

    protected CachedAuthentication getCachedAuthentication(Token at) {
        return cache.get(at.getToken());
    }

    protected void cacheAuthentication(Token at, TokenInfo tokenDetails, OAuth2Authentication authc) {
        int cachedMs = cacheExpiresInMs;
        if (tokenDetails instanceof TimeExpirableSeconds) {
            cachedMs = ((TimeExpirableSeconds) tokenDetails).getExpiresInFormNow() * 1000;
        }
        cache.put(at.getToken(), new CachedAuthentication(tokenDetails, authc, cachedMs));
    }

    protected void removeCachedAuthentication(Token at, CachedAuthentication cached) {
        cache.remove(at.getToken());
    }

    protected static final class CachedAuthentication {
        public final TokenInfo            tokenInfo;
        public final OAuth2Authentication authentication;

        private final TimeExpirableMs expirable;

        public CachedAuthentication(TokenInfo info, OAuth2Authentication a, int expiresInMs) {
            this.tokenInfo = info;
            this.authentication = a;
            this.expirable = new TimeExpirableMs(expiresInMs);
        }

        public boolean isTokenExpired() {
            return tokenInfo.isExpired();
        }

        public boolean isCacheExpired() {
            return expirable.isExpired();
        }
    }
}