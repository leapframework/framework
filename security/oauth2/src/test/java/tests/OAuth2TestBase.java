/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tests;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.function.BiConsumer;

import leap.core.security.token.jwt.JwtVerifier;
import leap.core.security.token.jwt.RsaVerifier;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.http.Headers;
import leap.lang.http.QueryString;
import leap.lang.http.QueryStringParser;
import leap.lang.naming.NamingStyles;
import leap.lang.net.Urls;
import leap.lang.security.RSA;
import leap.oauth2.as.OAuth2AuthzServerConfigurator;
import leap.webunit.WebTestBaseContextual;
import leap.webunit.client.THttpRequest;
import leap.webunit.client.THttpResponse;
import app.Global;

public abstract class OAuth2TestBase extends WebTestBaseContextual implements OAuth2TestData {
	
    public static final String AUTHZ_ENDPOINT     = OAuth2AuthzServerConfigurator.DEFAULT_AUTHZ_ENDPOINT_PATH;
    public static final String TOKEN_ENDPOINT     = OAuth2AuthzServerConfigurator.DEFAULT_TOKEN_ENDPOINT_PATH;
    public static final String TOKENINFO_ENDPOINT = OAuth2AuthzServerConfigurator.DEFAULT_TOKENINFO_ENDPOINT_PATH;
    public static final String LOGOUT_ENDPOINT    = OAuth2AuthzServerConfigurator.DEFAULT_LOGOUT_ENDPOINT_PATH;

    public static final String PUBLIC_KEY         =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDASOjIWexLpnXiJNJF2pL6NzP\n" +
            "fBoF0tKEr2ttAkJ/7f3uUHhj2NIhQ01Wu9OjHfXjCvQSXMWqqc1+O9G1UwB2Xslb\n" +
            "WNwEZFMwmQdP5VleGbJLR3wOl3IzdggkxBJ1Q9rXUlVtslK/CsMtkwkQEg0eZDH1\n" +
            "VeJXqKBlEhsNckYIGQIDAQAB";

    public static JwtVerifier verifier;

    static {
        defaultHttps = true;
        RSAPublicKey publicKey = RSA.decodePublicKey(PUBLIC_KEY);
        verifier = new RsaVerifier(publicKey);
        /*
        Dmo.get().cmdUpgradeSchema().execute();
        
        User.deleteAll();
        
        User admin = new User();
        admin.setLoginName(USERNAME);
        admin.setPassword(SEC.encodePassword(PASSWORD));
        admin.save();
        
        User test1 = new User();
        test1.setLoginName("test1");
        test1.setPassword("bad password");
        test1.save();
        
        User test2 = new User();
        test2.setLoginName("test2");
        test2.setPassword("1");
        test2.setEnabled(false);
        test2.save();
        
        User test3 = new User();
        test3.setLoginName(USERNAME1);
        test3.setPassword(SEC.encodePassword(PASSWORD1));
        test3.save();
        */
    }
    
    protected String serverContextPath = ""; //The context path of server.
    
    protected <T extends AuthzResponse> T resp(THttpResponse httpResp, T resp) {
        return resp(httpResp, resp, null);
    }
    
    protected <T extends AuthzResponse> T resp(THttpResponse httpResp, T resp, BiConsumer<Map<String, Object>, T> callback) {
        Map<String, Object> map = httpResp.getJson().asMap();
        
        if(!setErrorResponse(map, resp)) {
            setSuccessResponse(map, resp);
            if(null != callback) {
                callback.accept(map, resp);    
            }
        }
        
        return resp;
    }
    
    protected String obtainAuthorizationCode() {
        String codeUri = serverContextPath + AUTHZ_ENDPOINT + "?client_id=test&redirect_uri=" + Global.TEST_CLIENT_REDIRECT_URI_ENCODED + "&response_type=code";
        
        login();
        String redirectUrl = get(codeUri).assertRedirect().getRedirectUrl();
        
        QueryString qs = QueryStringParser.parse(Urls.getQueryString(redirectUrl));
        
        return qs.getParameter("code");
    }
    
    protected TokenResponse obtainAccessTokenByCode(String code) {
        String tokenUri = serverContextPath + TOKEN_ENDPOINT + 
                "?grant_type=authorization_code&code=" + code + 
                "&client_id=test" + 
                "&client_secret=" + Global.TEST_CLIENT_SECRET;
        
        return resp(post(tokenUri), new TokenResponse());
    }	
    
    protected TokenResponse obtainAccessTokenImplicit() {
        String uri = serverContextPath + AUTHZ_ENDPOINT + 
                        "?client_id=test&redirect_uri=" + Global.TEST_CLIENT_REDIRECT_URI_ENCODED + "&response_type=token";
        
        login();
        String redirectUrl = get(uri).assertRedirect().getRedirectUrl();
        
        QueryString qs = QueryStringParser.parse(Urls.getQueryString(redirectUrl));

        TokenResponse resp = new TokenResponse();
        
        if(!setErrorResponse(qs.getParameters(), resp)) {
            setSuccessResponse(qs.getParameters(), resp);
        }

        return resp;
    }
    
    protected TokenResponse obtainIdTokenImplicit() {
        String uri = serverContextPath + AUTHZ_ENDPOINT + 
                        "?client_id=test&redirect_uri=" + Global.TEST_CLIENT_REDIRECT_URI_ENCODED + "&response_type=id_token";
        
        login();
        String redirectUrl = get(uri).assertRedirect().getRedirectUrl();
        
        QueryString qs = QueryStringParser.parse(Urls.getQueryString(redirectUrl));

        TokenResponse resp = new TokenResponse();
        
        if(!setErrorResponse(qs.getParameters(), resp)) {
            setSuccessResponse(qs.getParameters(), resp);
        }

        return resp;
    }
    
    protected TokenResponse obtainAccessTokenByPassword(String username, String password) {
        String tokenUri = serverContextPath + TOKEN_ENDPOINT + 
                "?grant_type=password&username=" + Urls.encode(username) + 
                "&password=" + Urls.encode(password) + 
                "&client_id=" + Global.TEST_CLIENT_ID;
        
        return resp(post(tokenUri), new TokenResponse());
    }
    
    protected TokenResponse obtainAccessTokenByClient(String clientId, String clientSecret) {
        String tokenUri = serverContextPath + TOKEN_ENDPOINT + 
                "?grant_type=client_credentials&client_id=" + Urls.encode(clientId) + 
                "&client_secret=" + Urls.encode(clientSecret); 
        
        return resp(post(tokenUri), new TokenResponse());
    }
    
    protected TokenResponse obtainAccessTokenByRefreshToken(String refreshToken) {
        return obtainAccessTokenByRefreshToken(refreshToken, Global.TEST_CLIENT_ID, Global.TEST_CLIENT_SECRET);
    }
    
    protected TokenResponse obtainAccessTokenByRefreshToken(String refreshToken, String clientId, String clientSecret) {
        String tokenUri = serverContextPath + TOKEN_ENDPOINT + 
                "?grant_type=refresh_token&refresh_token=" + Urls.encode(refreshToken) +
                "&client_id=" + Urls.encode(clientId) + 
                "&client_secret=" + Urls.encode(clientSecret); 
        
        return resp(post(tokenUri), new TokenResponse());
    }
    
    protected boolean setErrorResponse(Map<String, Object> map, AuthzResponse resp) {
        String error = (String)map.remove("error");
        if(null != error) {
            resp.error = error;
            resp.errorDescription = (String)map.remove("error_description");
            return true;
        }
        return false;
    }
    
    protected void setSuccessResponse(Map<String, Object> map, AuthzResponse resp) {
        BeanType bt = BeanType.of(resp.getClass());
        
        BeanProperty mapProperty = null;
        
        for(BeanProperty bp : bt.getProperties()) {
            if(bp.isWritable()) {
                String name = bp.getName();
                String lowerUnderscoreName = NamingStyles.LOWER_UNDERSCORE.of(name);

                if(map.containsKey(name)) {
                    bp.setValue(resp, map.remove(name));
                }else if(map.containsKey(lowerUnderscoreName)) {
                    bp.setValue(resp, map.remove(lowerUnderscoreName));
                }else if(Map.class.equals(bp.getType())) {
                    mapProperty = bp;
                }
            }
        }
        
        if(null != mapProperty) {
            mapProperty.setValue(resp, map);
        }
    }
    
    protected TokenInfoResponse obtainAccessTokenInfo(String accessToken) {
        String uri = serverContextPath + TOKENINFO_ENDPOINT + "?access_token=" + accessToken; 

        return resp(get(uri), new TokenInfoResponse());
    }
    protected JwtTokenResponse obtainAccessTokenInfoWithJwtResponse(String accessToken) {
        String uri = serverContextPath + TOKENINFO_ENDPOINT + "?access_token=" + accessToken + "&response_type=jwt";
        JwtTokenResponse token = resp(get(uri), new JwtTokenResponse());
        return token;
    }

    protected TokenInfoResponse testAccessTokenInfo(TokenResponse token) {
        TokenInfoResponse info = obtainAccessTokenInfo(token.accessToken);
        
        assertNotEmpty(info.userId);
        assertEquals(token.expiresIn, info.expiresIn);
        
        return info;
    }

    protected JwtTokenResponse testJwtResponseAccessTokenInfo(TokenResponse token){
        JwtTokenResponse info = obtainAccessTokenInfoWithJwtResponse(token.accessToken);
        return info;
    }

    protected TokenInfoResponse testClientOnlyAccessTokenInfo(TokenResponse token) {
        TokenInfoResponse info = obtainAccessTokenInfo(token.accessToken);
        
        assertNotEmpty(info.clientId);
        assertEquals(token.expiresIn, info.expiresIn);
        
        return info;
    }
    
	protected void login() {
		login(serverContextPath, USER_ADMIN, PASS_ADMIN);
	}
	
	protected void login(String contextPath) {
		login(contextPath, USER_ADMIN, PASS_ADMIN);
	}
	
	protected void assertLogin() {
	    ajaxGet("/check_login_state").assertContentEquals("OK");
	}

    protected boolean isLogin(){
        return "OK".equalsIgnoreCase(ajaxGet("/check_login_state").getContent());
    }
	
   protected void assertLogin(String username) {
        forGet("/check_login_state").addQueryParam("username", username).ajax().get().assertContentEquals("OK");
    }
	
	protected void assertLogout() {
	    ajaxGet("/check_login_state").assert401();
	}
	
	protected THttpRequest forLogin() {
		return forLogin(contextPath, USER_ADMIN, PASS_ADMIN);
	}
	
	protected THttpRequest forLogin(String contextPath) {
		return forLogin(contextPath, USER_ADMIN, PASS_ADMIN);
	}

	protected void login(String contextPath, String username,String password) {
		forLogin(contextPath, username, password).sendAjax().assertOk();
	}
	
	protected THttpRequest forLogin(String contextPath, String username, String password) {
		return client().request(contextPath + "/login").addFormParam("username", username).addFormParam("password", password);
	}
	
	protected void logout() {
		logout(serverContextPath);
	}
	
	protected void logout(String contextPath) {
		ajaxPost(contextPath + "/logout").assertOk();
	}

	protected THttpRequest withAccessToken(THttpRequest request, String token) {
	    return request.setHeader(Headers.AUTHORIZATION, "Bearer " + token).ajax();
	}

    protected void loginAuthzServer() {
        login("/server");
    }

    protected void logoutAuthzServer() {
        logout("/server/oauth2");
    }
}