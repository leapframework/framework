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

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.cache.Cache;
import leap.core.cache.CacheManager;
import leap.core.ioc.PostCreateBean;
import leap.core.security.ClientPrincipal;
import leap.core.security.UserPrincipal;
import leap.lang.Strings;
import leap.lang.expirable.TimeExpirableMs;
import leap.lang.expirable.TimeExpirableSeconds;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.oauth2.webapp.OAuth2Config;
import leap.oauth2.webapp.token.AccessToken;
import leap.oauth2.webapp.token.AccessTokenDetails;
import leap.oauth2.webapp.token.TokenManager;
import leap.oauth2.webapp.user.UserInfoLookup;

/**
 * Default implementation of {@link OAuth2Authenticator}.
 */
public class DefaultOAuth2Authenticator implements OAuth2Authenticator, PostCreateBean {
    private static final Log log = LogFactory.get(DefaultOAuth2Authenticator.class);

    protected @Inject OAuth2Config   config;
    protected @Inject TokenManager   tokenManager;
    protected @Inject UserInfoLookup userInfoLookup;
    protected @Inject CacheManager   cacheManager;

    protected Cache<String, CachedAuthentication> authcCache;
    protected int                                 cacheSize = 2048;              //caches max {cacheSize} access tokens.
    protected int                                 cacheExpiresInMs = 120 * 1000; //2 minutes

    @Override
    public OAuth2Authentication authenticate(AccessToken token) {
        //Resolve from cache.
        CachedAuthentication cached = getCachedAuthentication(token);
        if(null != cached) {

            //Check expiration of token.
            if(cached.isTokenExpired()) {
                log.debug("Access token '{}' was expired", token.getToken());
                removeCachedAuthentication(token, cached);
                return null;
            }

            //Check expiration of the cached item.
            if(cached.isCacheExpired()) {
                log.debug("Cached authentication expired, remove it from cache only");
                removeCachedAuthentication(token, cached);
            }else{
                log.debug("Returns the cached authentication of access token : {}", token.getToken());
                return cached.authentication;
            }
        }

        AccessTokenDetails details = tokenManager.loadAccessTokenDetails(token);
        if(null == details) {
            log.debug("Access token '{}' not found", token.getToken());
            return null;
        }

        if(details.isExpired()) {
            log.debug("Access token '{}' was expired", token.getToken());
            tokenManager.removeAccessTokenDetails(token);
            return null;
        }

        String clientId = details.getClientId();
        String userId   = details.getUserId();

        UserPrincipal   user   = null;
        ClientPrincipal client = null;

        if(!Strings.isEmpty(userId)) {
            //user info lookup
            user = userInfoLookup.lookupUserDetails(token, userId);
            if(null == user) {
                //todo: exception?
                log.warn("User info not exists in oauth2 server, user id -> {}, access token -> {}", userId, token.getToken());
                return null;
            }
        }

        if(!Strings.isEmpty(clientId)) {
            //todo : client info lookup
            client = new OAuth2ClientPrincipal(clientId);
        }

        OAuth2Authentication authc = new SimpleOAuth2Authentication(token, user, client);
        if(null != details.getScope()) {
            authc.setPermissions(Strings.split(details.getScope(), ","));
        }

        cacheAuthentication(token, details, authc);

        return authc;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public int getCacheExpiresInMs() {
        return cacheExpiresInMs;
    }

    public void setCacheExpiresInMs(int cacheExpiresInMs) {
        this.cacheExpiresInMs = cacheExpiresInMs;
    }

    @Override
    public void postCreate(BeanFactory factory) throws Throwable {
        authcCache = cacheManager.createSimpleLRUCache(cacheSize);
    }

    protected CachedAuthentication getCachedAuthentication(AccessToken at) {
        return authcCache.get(at.getToken());
    }

    protected void cacheAuthentication(AccessToken at, AccessTokenDetails tokenDetails, OAuth2Authentication authc) {
    	int cachedMs=cacheExpiresInMs;
    	if(tokenDetails instanceof TimeExpirableSeconds){
    		cachedMs=((TimeExpirableSeconds)tokenDetails).getExpiresInFormNow()*1000;
    	}
        authcCache.put(at.getToken(), new CachedAuthentication(tokenDetails, authc, cachedMs));
    }

    protected void removeCachedAuthentication(AccessToken at, CachedAuthentication cached) {
        authcCache.remove(at.getToken());
    }

    protected static final class CachedAuthentication {
        public final AccessTokenDetails   tokenDetails;
        public final OAuth2Authentication authentication;

        private final TimeExpirableMs expirable;

        public CachedAuthentication(AccessTokenDetails d, OAuth2Authentication a, int expiresInMs) {
            this.tokenDetails = d;
            this.authentication = a;
            this.expirable = new TimeExpirableMs(expiresInMs);
        }

        public boolean isTokenExpired() {
            return tokenDetails.isExpired();
        }

        public boolean isCacheExpired() {
            return expirable.isExpired();
        }
    }
}