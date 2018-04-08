package leap.oauth2.server.authc;

import java.util.Map;

import leap.core.annotation.Inject;
import leap.lang.Out;
import leap.lang.beans.DynaBean;
import leap.oauth2.server.OAuth2Params;
import leap.oauth2.server.client.AuthzClient;
import leap.oauth2.server.code.AuthzCode;
import leap.oauth2.server.token.AuthzAccessToken;
import leap.web.security.SecurityConfig;
import leap.web.security.user.UserDetails;
import leap.web.security.user.UserStore;

public class DefaultAuthzAuthenticationManager implements AuthzAuthenticationManager {

	protected @Inject AuthzAuthenticationHandler[] authzAuthenticationHandlers;
	protected @Inject SecurityConfig    sc;

	public AuthzAuthentication createAuthzAuthentication(OAuth2Params oauthParam,AuthzClient client,UserDetails ud){

		AuthzAuthentication authc=createAuthzAuthenticationFromHandler(oauthParam,client,ud);

		if(authc==null ) {
			authc=new SimpleAuthzAuthentication(oauthParam, client, ud);
		}

	    return authc;
	}

	public AuthzAuthentication createAuthzAuthentication(OAuth2Params oauthParam,AuthzClient client,AuthzCode authzCode){
		AuthzAuthentication authc=createAuthzAuthenticationFromHandler(oauthParam,client,authzCode);
		if(authc==null){
			UserStore us = sc.getUserStore();
			UserDetails userDetails = us.loadUserDetailsByIdString(authzCode.getUserId());
			if(null == userDetails) {
				return null;
			}
			return new SimpleAuthzAuthentication(oauthParam, client, userDetails);
		}
		return authc;
	}

	private AuthzAuthentication createAuthzAuthenticationFromHandler(OAuth2Params oauthParam,AuthzClient client,Object ud){
		AuthzAuthenticationContext context=new AuthzAuthenticationContext(oauthParam, client, ud);

		Out<AuthzAuthentication> authc = new Out<>();

		for(AuthzAuthenticationHandler handler : authzAuthenticationHandlers) {
			if(handler.createAuthzAuthentication(context, authc)){
				break;
			}
		}
		return authc.getValue();
	}

	@Override
	public AuthzAuthentication createAuthzAuthentication(OAuth2Params oauthParam, AuthzClient client,AuthzAccessToken at) {

		AuthzAuthentication authc=createAuthzAuthenticationFromHandler(oauthParam,client,at);
		if(authc==null){
			UserStore us = sc.getUserStore();
			UserDetails userDetails = us.loadUserDetailsByIdString(at.getUserId());
			if(null == userDetails) {
				return null;
			}
			if(userDetails instanceof DynaBean && at.getExtendedParameters()!=null){
				DynaBean dyUser=(DynaBean)userDetails;
				for (Map.Entry<String, Object> entry : at.getExtendedParameters().entrySet()) {
					dyUser.setProperty(entry.getKey(), entry.getValue());
				}
			}
			return new SimpleAuthzAuthentication(oauthParam, client, userDetails);
		}
		return authc;
	}

}
