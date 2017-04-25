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
        TokenResponse token = obtainAccessTokenByPassword(USER_XIAOMING, PASS_XIAOMING,Global.TEST_CLIENT_ID,Global.TEST_CLIENT_SECRET);
        assertFalse(token.isError());
        TokenInfoResponse userToken = testAccessTokenInfo(token);
        assertNotEmpty(userToken.clientId);
        assertNotEmpty(userToken.scope);
    }

}
