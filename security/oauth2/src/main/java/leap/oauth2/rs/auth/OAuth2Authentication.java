package leap.oauth2.rs.auth;

import leap.oauth2.rs.token.AccessToken;
import leap.web.security.authc.Authentication;

public interface OAuth2Authentication extends Authentication {

    /**
     * The credentials must be the type of {@link AccessToken}.
     */
    AccessToken getCredentials();

    /**
     * Returns the granted scopes.
     */
    default String[] getGrantedScope() {
        return null;
    }

}