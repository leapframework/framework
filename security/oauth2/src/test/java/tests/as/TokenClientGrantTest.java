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
        logout();
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
    @Test
    public void testAuthenticateTokenClientWithJwtToken(){
        logout();
        TokenResponse token = obtainAccessTokenByPassword(USER_XIAOMING, PASS_XIAOMING,Global.TEST_CLIENT_ID);
        JwtTokenResponse jwt = obtainAccessTokenInfoWithJwtResponse(token.accessToken);
        assertNotEmpty(jwt.jwtToken);
        Map<String, Object> info = verifier.verify(jwt.jwtToken);
        assertEquals(USER_XIAOMING,info.get("username"));
        assertNull(info.get("client_id"));
        TokenResponse newToken = obtainAccessTokenByTokenClient(jwt.jwtToken, Global.TEST_CLIENT_ID, Global.TEST_CLIENT_SECRET);
        JwtTokenResponse newJwt = obtainAccessTokenInfoWithJwtResponse(newToken.accessToken);
        assertNotEmpty(newJwt.jwtToken);
        Map<String, Object> newInfo = verifier.verify(newJwt.jwtToken);
        assertEquals(USER_XIAOMING,newInfo.get("username"));
        assertEquals(Global.TEST_CLIENT_ID,newInfo.get("client_id"));
    }

}
