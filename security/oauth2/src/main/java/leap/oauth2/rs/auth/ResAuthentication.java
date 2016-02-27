package leap.oauth2.rs.auth;

import leap.oauth2.rs.token.ResAccessToken;
import leap.core.security.Authentication;

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