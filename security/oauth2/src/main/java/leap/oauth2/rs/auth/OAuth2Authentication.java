package leap.oauth2.rs.auth;

import leap.oauth2.rs.token.AccessToken;
import leap.web.security.authc.Authentication;

public interface OAuth2Authentication extends Authentication {

    AccessToken getCredentials();
    
    default String[] getGrantedScope() {
        return null;
    }

}