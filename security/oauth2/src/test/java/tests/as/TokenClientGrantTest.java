package tests.as;

import app.Global;
import org.junit.Test;
import tests.JwtTokenResponse;
import tests.OAuth2TestBase;
import tests.TokenInfoResponse;
import tests.TokenResponse;

import java.util.Map;

/**
 * Created by kael on 2016/6/14.
 */
public class TokenClientGrantTest extends OAuth2TestBase {
    @Test
    public void testAuthenticateTokenClient(){
        if(isLogin()) {
            logoutAuthzServer();
        }
        TokenResponse token = obtainAccessTokenByPassword(USER_XIAOMING, PASS_XIAOMING,Global.TEST_CLIENT_ID);
        assertFalse(token.isError());
        TokenInfoResponse userToken = testAccessTokenInfo(token);
        assertEmpty(userToken.clientId);
        assertEmpty(userToken.scope);
        TokenResponse newToken = obtainAccessTokenByTokenClient(token.accessToken, Global.TEST_CLIENT_ID, Global.TEST_CLIENT_SECRET);
        assertNotEquals(token.accessToken,newToken.accessToken);
        TokenInfoResponse clientToken = testAccessTokenInfo(newToken);
        assertNotEmpty(clientToken.clientId);
        assertNotEmpty(clientToken.scope);
        assertEquals(Global.TEST_CLIENT_GRANTED_SCOPE,clientToken.scope);
    }

}
