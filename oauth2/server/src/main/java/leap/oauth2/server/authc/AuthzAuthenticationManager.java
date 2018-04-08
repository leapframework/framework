package leap.oauth2.server.authc;

import leap.oauth2.server.OAuth2Params;
import leap.oauth2.server.client.AuthzClient;
import leap.oauth2.server.code.AuthzCode;
import leap.oauth2.server.token.AuthzAccessToken;
import leap.web.security.user.UserDetails;

public interface AuthzAuthenticationManager {

	AuthzAuthentication createAuthzAuthentication(OAuth2Params oauthParam,AuthzClient client,UserDetails ud);

	AuthzAuthentication createAuthzAuthentication(OAuth2Params oauthParam,AuthzClient client,AuthzCode authzCode);

	AuthzAuthentication createAuthzAuthentication(OAuth2Params oauthParam,AuthzClient client,AuthzAccessToken at);
}
