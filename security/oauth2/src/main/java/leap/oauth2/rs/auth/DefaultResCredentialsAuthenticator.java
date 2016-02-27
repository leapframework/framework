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
package leap.oauth2.rs.auth;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.cache.Cache;
import leap.core.cache.CacheManager;
import leap.core.ioc.PostCreateBean;
import leap.core.security.ClientPrincipal;
import leap.core.security.UserPrincipal;
import leap.lang.Result;
import leap.lang.Strings;
import leap.lang.expirable.TimeExpirableMs;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.oauth2.rs.OAuth2ResServerConfig;
import leap.oauth2.rs.token.ResAccessToken;
import leap.oauth2.rs.token.ResAccessTokenDetails;
import leap.oauth2.rs.token.ResTokenManager;
import leap.web.security.user.UserDetails;
import leap.web.security.user.UserManager;

/**
 * Default implementation of {@link ResCredentialsAuthenticator}.
 */
public class DefaultResCredentialsAuthenticator implements ResCredentialsAuthenticator, PostCreateBean {
    private static final Log log = LogFactory.get(DefaultResCredentialsAuthenticator.class);

    protected @Inject BeanFactory           factory;
    protected @Inject OAuth2ResServerConfig config;
    protected @Inject ResTokenManager       tokenManager;
    protected @Inject UserManager           userManager;
    protected @Inject CacheManager          cacheManager;

    protected Cache<String, CachedAuthentication> authcCache;
    protected int                                 cacheSize = 2048;              //caches max {cacheSize} access tokens.
    protected int                                 cacheExpiresInMs = 120 * 1000; //2 minutes

    @Override
    public Result<ResAuthentication> authenticate(ResAccessToken at) {
        //Resolve from cache.
        CachedAuthentication cached = getCachedAuthentication(at);
        if(null != cached) {

            //Check expiration of token.
            if(cached.isTokenExpired()) {
                log.debug("Access token '{}' was expired", at.getToken());
                removeCachedAuthentication(at, cached);
                return Result.empty();
            }

            //Check expiration of the cached item.
            if(cached.isCacheExpired()) {
                log.debug("Cached authentication expired, remove it from cache only");
                removeCachedAuthentication(at, cached);
            }else{
                log.debug("Returns the cached authentication of access token : {}", at.getToken());
                return Result.of(cached.authentication);
            }
        }

        Result<ResAccessTokenDetails> result = tokenManager.loadAccessTokenDetails(at);
        if(!result.isPresent()) {
            log.debug("Access token '{}' not found", at.getToken());
            return Result.empty();
        }
        
        ResAccessTokenDetails details = result.get();
        if(details.isExpired()) {
            log.debug("Access token '{}' was expired", at.getToken());
            tokenManager.removeAccessToken(at);
            return Result.empty();
        }
        
        String clientId = details.getClientId();
        String userId   = details.getUserId();
        
        UserPrincipal   user   = null;
        ClientPrincipal client = null;
        
        if(!Strings.isEmpty(userId)) {
            UserDetails userDetails = userManager.loadUserDetails(userId);
            
            if(null == userDetails) {
                log.debug("The user id '{}' created with access token '{}' is not found", userId, at.getToken());
                return Result.empty();
            }else{
                user = userDetails;
            }
        }
        
        if(!Strings.isEmpty(clientId)) {
            client = new ResClientPrincipal(clientId);
        }
        
        ResAuthentication authc = new SimpleResAuthentication(at, user, client);
        
        cacheAuthentication(at, details, authc);

        return Result.of(authc);
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
    
    protected CachedAuthentication getCachedAuthentication(ResAccessToken at) {
        return authcCache.get(at.getToken());
    }
    
    protected void cacheAuthentication(ResAccessToken at, ResAccessTokenDetails tokenDetails, ResAuthentication authc) {
        authcCache.put(at.getToken(), new CachedAuthentication(tokenDetails, authc, cacheExpiresInMs));
    }
    
    protected void removeCachedAuthentication(ResAccessToken at, CachedAuthentication cached) {
        authcCache.remove(at.getToken());
    }
    
    protected static final class CachedAuthentication {
        public final ResAccessTokenDetails tokenDetails;
        public final ResAuthentication     authentication;
        
        private final TimeExpirableMs expirable;
        
        public CachedAuthentication(ResAccessTokenDetails d, ResAuthentication a, int expiresInMs) {
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