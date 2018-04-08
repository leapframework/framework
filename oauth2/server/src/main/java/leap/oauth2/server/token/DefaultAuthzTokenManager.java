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

import java.util.HashMap;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.lang.beans.DynaBean;
import leap.oauth2.server.authc.AuthzAuthentication;
import leap.oauth2.server.OAuth2AuthzServerConfig;
import leap.oauth2.server.client.AuthzClient;
import leap.web.security.user.UserDetails;

public class DefaultAuthzTokenManager implements AuthzTokenManager {

	protected @Inject OAuth2AuthzServerConfig      config;
	protected @Inject AuthzAccessTokenGenerator    defaultAccessTokenGenerator;
	protected @Inject AuthzRefreshTokenGenerator   defaultRefreshTokenGenerator;

	protected @Inject BeanFactory                  factory;
    protected @Inject CreateAccessTokenProcessor[] processors;

    @Override
    public AuthzAccessToken createAccessToken(AuthzAuthentication authc) {
        AuthzClient client      = authc.getClientDetails();
        UserDetails               user        = authc.getUserDetails();
        AuthzAccessTokenGenerator atGenerator = getAccessTokenGenerator(authc);
        boolean                   rtCreated   = false;

        //Create access token.
        SimpleAuthzAccessToken at = new SimpleAuthzAccessToken();
        SimpleAuthzRefreshToken rt = new SimpleAuthzRefreshToken();

        if(user instanceof DynaBean && ((DynaBean)user).getProperties()!=null){
			if(at.getExtendedParameters()==null){
				at.setExtendedParameters(new HashMap<>());
			}
			at.getExtendedParameters().putAll(((DynaBean)user).getProperties());
		}

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
            if(client.isAuthenticated()){
                at.setAuthenticated(client.isAuthenticated());
            }
        }

        if(null != user) {
            at.setUserId(user.getId().toString());
            rt.setUserId(at.getUserId());
            at.setUsername(user.getLoginName());
        }

        //Merge scope.
        String scope = null;
        if(client != null){
            if(config.isRequestLevelScopeEnabled()){
                scope = mergeScope(client, authc);
            }else if(client.isAuthenticated()){
                scope = client.getGrantedScope();
            }
        }
        //Scope
        at.setScope(scope);
        rt.setScope(scope);

        //Client Authenticated
        at.setAuthenticated(authc.getClientDetails().isAuthenticated());

        if(processors != null && processors.length>0){
            for(CreateAccessTokenProcessor merger : processors){
                merger.process(client, authc, at,rt);
            }
        }

        //Store the token
        config.getTokenStore().saveAccessToken(at);

        if(rtCreated) {
            config.getTokenStore().saveRefreshToken(rt);
        }

        return at;
    }

    protected String mergeScope(AuthzClient client, AuthzAuthentication authc) {
        if(!client.isAuthenticated()){
            return authc.getScope();
        }
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

	protected boolean isAllowRefreshToken(AuthzClient client) {
	    return null == client || client.isAllowRefreshToken();
	}

	protected AuthzAccessTokenGenerator getAccessTokenGenerator(AuthzAuthentication authc) {
	    return defaultAccessTokenGenerator;
	}

	protected AuthzRefreshTokenGenerator getRefreshTokenGenerator(AuthzAuthentication authc) {
	    return defaultRefreshTokenGenerator;
	}

}