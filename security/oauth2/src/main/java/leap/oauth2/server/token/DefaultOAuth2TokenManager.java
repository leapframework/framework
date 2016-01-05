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
package leap.oauth2.server.token;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.oauth2.server.authc.OAuth2Authentication;
import leap.oauth2.server.OAuth2ServerConfig;
import leap.oauth2.server.client.OAuth2Client;
import leap.web.security.user.UserDetails;

public class DefaultOAuth2TokenManager implements OAuth2TokenManager {
	
	protected @Inject OAuth2ServerConfig          config;
	protected @Inject OAuth2AccessTokenGenerator  defaultAccessTokenGenerator;
	protected @Inject OAuth2RefreshTokenGenerator defaultRefreshTokenGenerator;
	protected @Inject BeanFactory                 factory;
	
    @Override
    public OAuth2AccessToken createAccessToken(OAuth2Authentication authc) {
        OAuth2Client client      = authc.getClientDetails();
        UserDetails               user        = authc.getUserDetails();
        OAuth2AccessTokenGenerator atGenerator = getAccessTokenGenerator(authc);
        boolean                   rtCreated   = false;
        
        //Create access token.
        SimpleOAuth2AccessToken at = new SimpleOAuth2AccessToken();
        SimpleAuthzRefreshToken rt = new SimpleAuthzRefreshToken();
        
        //Generate token value
        at.setToken(atGenerator.generateAccessToken(authc));
        
        //Create refresh token
        if(isAllowRefreshToken(client)) {
            rtCreated = true;
            rt.setToken(getRefreshTokenGenerator(authc).generateRefreshToken(authc));
            rt.setExpiresIn(getRefreshTokenExpires(client));
        }
        
        //Set token refereces.
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
        
        //Scope
        at.setScope(authc.getScope());
        rt.setScope(authc.getScope());
        
        //Store the token
        config.getTokenStore().saveAccessToken(at);
        
        if(rtCreated) {
            config.getTokenStore().saveRefreshToken(rt);
        }
        
        return at;
    }
    
    @Override
    public OAuth2AccessToken createAccessToken(OAuth2Authentication authc, OAuth2RefreshToken rt) {
        //TODO : creates a new rt ?
        
        //create new one
        OAuth2AccessToken at = createAccessToken(authc);
        
        //removes the old
        removeRefreshToken(rt);
        
        return at;
    }

	@Override
    public OAuth2AccessToken loadAccessToken(String accessToken) {
        return config.getTokenStore().loadAccessToken(accessToken);
    }

    @Override
    public OAuth2RefreshToken loadRefreshToken(String refreshToken) {
        return config.getTokenStore().loadRefreshToken(refreshToken);
    }
    
	@Override
    public void removeAccessToken(OAuth2AccessToken token) {
	    removeAccessTokenOnly(token.getToken());
    }

    @Override
    public void removeRefreshToken(OAuth2RefreshToken token) {
	    removeRefreshTokenOnly(token.getToken());
    }
    
    protected void removeAccessTokenOnly(String token) {
        config.getTokenStore().removeAccessToken(token);
    }
	
	protected void removeRefreshTokenOnly(String token) {
	    config.getTokenStore().removeRefreshToken(token);
	}

    protected int getAccessTokenExpires(OAuth2Client client) {
	    int expires = config.getDefaultAccessTokenExpires();
	    
	    if(null != client && client.getAccessTokenExpires() != null) {
	        expires = client.getAccessTokenExpires();
	    }
	    
	    return expires;
	}
	
    protected int getRefreshTokenExpires(OAuth2Client client) {
        int expires = config.getDefaultRefreshTokenExpires();

        if (null != client && client.getRefreshTokenExpires() != null) {
            expires = client.getRefreshTokenExpires();
        }

        return expires;
    }
	
	protected boolean isAllowRefreshToken(OAuth2Client client) {
	    return null == client || client.isAllowRefreshToken();
	}

	protected OAuth2AccessTokenGenerator getAccessTokenGenerator(OAuth2Authentication authc) {
	    return defaultAccessTokenGenerator;
	}
	
	protected OAuth2RefreshTokenGenerator getRefreshTokenGenerator(OAuth2Authentication authc) {
	    return defaultRefreshTokenGenerator;
	}
	
}