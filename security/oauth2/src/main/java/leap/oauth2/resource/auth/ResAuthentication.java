package leap.oauth2.resource.auth;

import leap.oauth2.resource.token.ResAccessToken;
import leap.web.security.authc.Authentication;

public interface ResAuthentication extends Authentication {

    /**
     * The credentials must be the type of {@link ResAccessToken}.
     */
    ResAccessToken getCredentials();

    /**
     * Returns the granted scopes.
     */
    default String[] getGrantedScope() {
        return null;
    }

}