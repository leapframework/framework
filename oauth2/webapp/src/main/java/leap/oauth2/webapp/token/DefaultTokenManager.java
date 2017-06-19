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

import java.util.HashMap;
import java.util.Map;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ioc.PostCreateBean;
import leap.lang.Strings;
import leap.oauth2.webapp.OAuth2Config;

/**
 * The default implementation of {@link TokenManager}.
 */
public class DefaultTokenManager implements TokenManager, PostCreateBean {
    
    protected @Inject OAuth2Config     config;
    protected @Inject AccessTokenStore defaultTokenStore;

    protected final Map<String, AccessTokenStore> typedTokenStores = new HashMap<>();

    @Override
    public AccessTokenDetails loadAccessTokenDetails(AccessToken token) {
        return getAccessTokenStore(token).loadAccessTokenDetails(token);
    }

    @Override
    public void removeAccessTokenDetails(AccessToken token) {
        getAccessTokenStore(token).removeAccessToken(token);
    }

    protected AccessTokenStore getAccessTokenStore(AccessToken token) {
        AccessTokenStore store;

        String type = token.getType();

        if(!Strings.isEmpty(type)) {
            store = typedTokenStores.get(type);
        }else{
            store = defaultTokenStore;
        }
        
        return store;
    }

    @Override
    public void postCreate(BeanFactory factory) throws Throwable {
        this.typedTokenStores.putAll(factory.getNamedBeans(AccessTokenStore.class));
    }
}