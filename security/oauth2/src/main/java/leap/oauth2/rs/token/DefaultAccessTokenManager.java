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
package leap.oauth2.rs.token;

import java.util.HashMap;
import java.util.Map;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ioc.PostCreateBean;
import leap.lang.Result;
import leap.lang.Strings;
import leap.oauth2.as.OAuth2ServerConfig;
import leap.oauth2.rs.ResourceServerConfig;

public class DefaultAccessTokenManager implements AccessTokenManager, PostCreateBean {
    
    protected @Inject BeanFactory           factory;
    protected @Inject OAuth2ServerConfig     asConfig;
    protected @Inject ResourceServerConfig  rsConfig;
    
    protected Map<String, AccessTokenStore> typedTokenStores = new HashMap<String, AccessTokenStore>();
    protected AccessTokenStore              bearerTokenStore = null;
    
    @Override
    public Result<AccessTokenDetails> getAccessTokenDetails(AccessToken token) {
        return getAccessTokenStore(token).loadAccessTokenDetails(token);
    }

    @Override
    public void removeAccessToken(AccessToken token) {
        getAccessTokenStore(token).removeAccessToken(token);
    }

    @Override
    public void postCreate(BeanFactory factory) throws Throwable {
        this.typedTokenStores.putAll(factory.getNamedBeans(AccessTokenStore.class));
    }
    
    protected AccessTokenStore getAccessTokenStore(AccessToken token) {
        AccessTokenStore store;
        
        if(!token.isBearer()) {
            store = typedTokenStores.get(token.getType());
            
            if(null == store) {
                throw new IllegalStateException("No AccessTokenStore for token type '" + token.getType() + "'");
            }
        }else{
            if(null == bearerTokenStore) {
                this.bearerTokenStore = factory.tryGetBean(BearerAccessTokenStore.class);
                
                if(null == bearerTokenStore) {
                    if(!Strings.isEmpty(rsConfig.getRemoteTokenInfoUrl())) {
                        this.bearerTokenStore = factory.getBean(BearerAccessTokenStore.class, "remote");
                    }else if(asConfig.isEnabled()){
                        this.bearerTokenStore = factory.getBean(BearerAccessTokenStore.class, "local");    
                    }else {
                        throw new IllegalStateException("BearerAccessTokenStore must be specified");
                    }
                }
            }
            
            store = bearerTokenStore;
        }
        
        return store;
    }
    
}