package leap.oauth2.as.authc;

import leap.oauth2.OAuth2Params;
import leap.oauth2.as.client.AuthzClient;
import leap.oauth2.as.code.AuthzCode;
import leap.oauth2.as.token.AuthzAccessToken;
import leap.web.security.user.UserDetails;

public interface AuthzAuthenticationManager {

	AuthzAuthentication createAuthzAuthentication(OAuth2Params oauthParam,AuthzClient client,UserDetails ud);

	AuthzAuthentication createAuthzAuthentication(OAuth2Params oauthParam,AuthzClient client,AuthzCode authzCode);

	AuthzAuthentication createAuthzAuthentication(OAuth2Params oauthParam,AuthzClient client,AuthzAccessToken at);
}
