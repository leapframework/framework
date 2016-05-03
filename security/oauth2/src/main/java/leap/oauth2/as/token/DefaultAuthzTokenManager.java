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
package leap.oauth2.as.token;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.oauth2.as.authc.AuthzAuthentication;
import leap.oauth2.as.OAuth2AuthzServerConfig;
import leap.oauth2.as.client.AuthzClient;
import leap.web.security.user.UserDetails;

public class DefaultAuthzTokenManager implements AuthzTokenManager {
	
	protected @Inject OAuth2AuthzServerConfig    config;
	protected @Inject AuthzAccessTokenGenerator  defaultAccessTokenGenerator;
	protected @Inject AuthzRefreshTokenGenerator defaultRefreshTokenGenerator;
    protected @Inject AuthzLoginTokenGenerator   defaultLoginTokenGenerator;
	protected @Inject BeanFactory                factory;
	
    @Override
    public AuthzAccessToken createAccessToken(AuthzAuthentication authc) {
        AuthzClient client      = authc.getClientDetails();
        UserDetails               user        = authc.getUserDetails();
        AuthzAccessTokenGenerator atGenerator = getAccessTokenGenerator(authc);
        boolean                   rtCreated   = false;
        
        //Create access token.
        SimpleAuthzAccessToken at = new SimpleAuthzAccessToken();
        SimpleAuthzRefreshToken rt = new SimpleAuthzRefreshToken();
        
        //Generate token value
        at.setToken(atGenerator.generateAccessToken(authc));
        
        //Create refresh token
        if(isAllowRefreshToken(client)) {
            rtCreated = true;
            rt.setToken(getRefreshTokenGenerator(authc).generateRefreshToken(authc));
            rt.setExpiresIn(getRefreshTokenExpires(client));
        }
        
        //Set refresh token.
        at.setRefreshToken(rt.getToken());
        
        //Set expires & created
        at.setExpiresIn(getAccessTokenExpires(client));
        at.setCreated(System.currentTimeMillis());
        rt.setCreated(at.getCreated());
        
        //Set client & user info
        if(null != client) {
            at.setClientId(client.getId());
            rt.setClientId(client.getId());
        }
        
        if(null != user) {
            at.setUserId(user.getId().toString());
            rt.setUserId(at.getUserId());
        }

        //Merge scope.
        String scope = config.isRequestLevelScopeEnabled()?mergeScope(client, authc):client.getGrantedScope();

        //Scope
        at.setScope(scope);
        rt.setScope(scope);
        
        //Store the token
        config.getTokenStore().saveAccessToken(at);
        
        if(rtCreated) {
            config.getTokenStore().saveRefreshToken(rt);
        }
        
        return at;
    }

    protected String mergeScope(AuthzClient client, AuthzAuthentication authc) {
        if(Strings.isEmpty(client.getGrantedScope()) && Strings.isEmpty(authc.getScope())) {
            return null;
        }

        if(Strings.isEmpty(client.getGrantedScope())) {
            return authc.getScope();
        }

        if(Strings.isEmpty(authc.getScope())) {
            return client.getGrantedScope();
        }
        return client.getGrantedScope() + "," + authc.getScope();
    }
    
    @Override
    public AuthzAccessToken createAccessToken(AuthzAuthentication authc, AuthzRefreshToken rt) {
        //TODO : creates a new rt ?
        
        //create new one
        AuthzAccessToken at = createAccessToken(authc);
        
        //removes the old
        removeRefreshToken(rt);
        
        return at;
    }

    @Override
    public AuthzLoginToken createLoginToken(AuthzAuthentication authc) {
        AuthzClient              client    = authc.getClientDetails();
        UserDetails              user      = authc.getUserDetails();
        AuthzLoginTokenGenerator generator = getLoginTokenGenerator(authc);

        //Creates the token
        SimpleAuthzLoginToken token = new SimpleAuthzLoginToken();
        token.setToken(getLoginTokenGenerator(authc).generateLoginToken(authc));
        token.setClientId(client.getId());
        token.setUserId(user.getId().toString());
        token.setCreated(System.currentTimeMillis());
        token.setExpiresIn(getLoginTokenExpires(client));

        //Saves the token.
        config.getTokenStore().saveLoginToken(token);

        return token;
    }

    @Override
    public AuthzLoginToken consumeLoginToken(String loginToken) {
        return config.getTokenStore().removeAndLoadLoginToken(loginToken);
    }

    @Override
    public AuthzAccessToken loadAccessToken(String accessToken) {
        return config.getTokenStore().loadAccessToken(accessToken);
    }

    @Override
    public AuthzRefreshToken loadRefreshToken(String refreshToken) {
        return config.getTokenStore().loadRefreshToken(refreshToken);
    }
    
	@Override
    public void removeAccessToken(AuthzAccessToken token) {
	    removeAccessTokenOnly(token.getToken());
    }

    @Override
    public void removeRefreshToken(AuthzRefreshToken token) {
	    removeRefreshTokenOnly(token.getToken());
    }
    
    protected void removeAccessTokenOnly(String token) {
        config.getTokenStore().removeAccessToken(token);
    }
	
	protected void removeRefreshTokenOnly(String token) {
	    config.getTokenStore().removeRefreshToken(token);
	}

    protected int getAccessTokenExpires(AuthzClient client) {
	    int expires = config.getDefaultAccessTokenExpires();
	    
	    if(null != client && client.getAccessTokenExpires() != null) {
	        expires = client.getAccessTokenExpires();
	    }
	    
	    return expires;
	}
	
    protected int getRefreshTokenExpires(AuthzClient client) {
        int expires = config.getDefaultRefreshTokenExpires();

        if (null != client && client.getRefreshTokenExpires() != null) {
            expires = client.getRefreshTokenExpires();
        }

        return expires;
    }

    protected int getLoginTokenExpires(AuthzClient client) {
        return config.getDefaultLoginTokenExpires();
    }
	
	protected boolean isAllowRefreshToken(AuthzClient client) {
	    return null == client || client.isAllowRefreshToken();
	}

	protected AuthzAccessTokenGenerator getAccessTokenGenerator(AuthzAuthentication authc) {
	    return defaultAccessTokenGenerator;
	}
	
	protected AuthzRefreshTokenGenerator getRefreshTokenGenerator(AuthzAuthentication authc) {
	    return defaultRefreshTokenGenerator;
	}

    protected AuthzLoginTokenGenerator getLoginTokenGenerator(AuthzAuthentication authc) {
        return defaultLoginTokenGenerator;
    }
}