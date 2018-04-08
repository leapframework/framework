/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  
 */
package tests;

import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.codec.Base64;
import leap.lang.http.Headers;
import leap.lang.http.QueryString;
import leap.lang.http.QueryStringParser;
import leap.lang.naming.NamingStyles;
import leap.lang.net.Urls;
import leap.oauth2.server.OAuth2AuthzServerConfigurator;
import leap.webunit.WebTestBaseContextual;
import leap.webunit.client.THttpRequest;
import leap.webunit.client.THttpResponse;
import server.OAuth2TestData;

import java.util.Map;
import java.util.function.BiConsumer;

public abstract class OAuth2TestBase extends WebTestBaseContextual implements OAuth2TestData {
	
    public static final String AUTHZ_ENDPOINT     = OAuth2AuthzServerConfigurator.DEFAULT_AUTHZ_ENDPOINT_PATH;
    public static final String TOKEN_ENDPOINT     = OAuth2AuthzServerConfigurator.DEFAULT_TOKEN_ENDPOINT_PATH;
    public static final String TOKENINFO_ENDPOINT = OAuth2AuthzServerConfigurator.DEFAULT_TOKENINFO_ENDPOINT_PATH;

    static {
        defaultHttps = true;
    }
    
    protected String serverContextPath = "/server"; //The context path of server.
    
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
        String codeUri = serverContextPath + AUTHZ_ENDPOINT + "?client_id=test&redirect_uri=" + TEST_CLIENT_REDIRECT_URI_ENCODED + "&response_type=code";
        
        loginServer();
        String redirectUrl = get(codeUri).assertRedirect().getRedirectUrl();
        
        QueryString qs = QueryStringParser.parse(Urls.getQueryString(redirectUrl));
        
        return qs.getParameter("code");
    }
    
    protected TokenResponse obtainAccessTokenByCode(String code) {
        String tokenUri = serverContextPath + TOKEN_ENDPOINT + 
                "?grant_type=authorization_code&code=" + code + 
                "&client_id=test" + 
                "&client_secret=" + TEST_CLIENT_SECRET;
        
        return resp(post(tokenUri), new TokenResponse());
    }	
    
    protected TokenResponse obtainAccessTokenImplicit() {
        String uri = serverContextPath + AUTHZ_ENDPOINT + 
                        "?client_id=test&redirect_uri=" + TEST_CLIENT_REDIRECT_URI_ENCODED + "&response_type=token";
        
        loginServer();
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
                        "?client_id=test&redirect_uri=" + TEST_CLIENT_REDIRECT_URI_ENCODED + "&response_type=id_token";
        
        loginServer();
        String redirectUrl = get(uri).assertRedirect().getRedirectUrl();
        
        QueryString qs = QueryStringParser.parse(Urls.getQueryString(redirectUrl));

        TokenResponse resp = new TokenResponse();
        
        if(!setErrorResponse(qs.getParameters(), resp)) {
            setSuccessResponse(qs.getParameters(), resp);
        }

        return resp;
    }

    protected TokenResponse obtainIdTokenTokenImplicit() {
        String uri = serverContextPath + AUTHZ_ENDPOINT +
                "?client_id=test&redirect_uri=" + TEST_CLIENT_REDIRECT_URI_ENCODED + "&response_type=id_token%20token";

        loginServer();
        String redirectUrl = get(uri).assertRedirect().getRedirectUrl();

        QueryString qs = QueryStringParser.parse(Urls.getQueryString(redirectUrl));

        TokenResponse resp = new TokenResponse();

        if(!setErrorResponse(qs.getParameters(), resp)) {
            setSuccessResponse(qs.getParameters(), resp);
        }

        return resp;
    }
    
    protected TokenResponse obtainAccessTokenByPassword(String username, String password) {
        return obtainAccessTokenByPassword(username,password,TEST_CLIENT_ID,TEST_CLIENT_SECRET);
    }

    protected TokenResponse obtainAccessTokenByPassword(String username, String password, String clientId, String clientSecret) {

        THttpRequest request = usePost(serverContextPath + TOKEN_ENDPOINT)
                .addFormParam("grant_type","password")
                .addFormParam("password",Urls.encode(password))
                .addFormParam("username",Urls.encode(username))
                .addHeader("Authorization", "Basic " + Base64.encode(clientId+":"+clientSecret));
        
        return resp(request.send(), new TokenResponse());
    }
    
    
    protected TokenResponse obtainAccessTokenByClient(String clientId, String clientSecret) {
        String tokenUri = serverContextPath + TOKEN_ENDPOINT; 
        
        String token = encodeToBasicAuthcHeader(clientId,clientSecret);

        THttpRequest request = usePost(tokenUri).addHeader("Authorization",token)
                .addFormParam("grant_type","client_credentials");
        
        return resp(request.send(), new TokenResponse());
    }
    
    protected TokenResponse obtainAccessTokenByRefreshToken(String refreshToken) {
        return obtainAccessTokenByRefreshToken(refreshToken, TEST_CLIENT_ID, TEST_CLIENT_SECRET);
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
    
    protected String encodeToBasicAuthcHeader(String clientId, String clientSecret){
        return "Basic " + Base64.encode(clientId+":"+clientSecret);
    }
    
    protected TokenInfoResponse obtainAccessTokenInfo(String accessToken) {
        String uri = serverContextPath + TOKENINFO_ENDPOINT + "?access_token=" + accessToken; 

        return resp(get(uri), new TokenInfoResponse());
    }

    protected boolean isLogin(){
        return "OK".equalsIgnoreCase(ajaxGet("/check_login_state").getContent());
    }

    protected THttpRequest forLogin(String contextPath, String username, String password) {
        return client().request(contextPath + "/login").addFormParam("username", username).addFormParam("password", password);
    }
    
	protected void login(String contextPath, String username,String password) {
		forLogin(contextPath, username, password).sendAjax().assertOk();
	}
	
	protected void logout(String contextPath) {
		ajaxPost(contextPath + "/logout").assertOk();
	}

	protected THttpRequest withAccessToken(THttpRequest request, String token) {
	    return request.setHeader(Headers.AUTHORIZATION, "Bearer " + token).ajax();
	}

    protected void loginServer() {
        login("/server", USER_ADMIN, PASS_ADMIN);
    }

    protected void logoutServer() {
        logout("/server/oauth2");
    }
}