/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.oauth2.webunit;

import junit.framework.TestCase;
import leap.lang.http.HTTP;
import leap.lang.http.Headers;
import leap.lang.http.QueryStringParser;
import leap.lang.net.Urls;
import leap.oauth2.as.OAuth2AuthzServerConfigurator;
import leap.webunit.client.THttpClient;
import leap.webunit.client.THttpRequest;

import java.util.Map;

/**
 * The testing util for oauth2.
 */
public class TOAuth2 {

    public static final String DEFAULT_USERNAME            = "user1";
    public static final String DEFAULT_PASSWORD            = "1";
    public static final String DEFAULT_CLIENT_ID           = "client1";
    public static final String DEFAULT_CLIENT_SECRET       = "1";
    public static final String DEFAULT_CLIENT_REDIRECT_URI = "/oauth2/redirect_uri";

    private final THttpClient client;
    private final String      serverContextPath;

    private String authorizeEndpoint        = OAuth2AuthzServerConfigurator.DEFAULT_AUTHZ_ENDPOINT_PATH;
    private String defaultUsername          = DEFAULT_USERNAME;
    private String defaultPassword          = DEFAULT_PASSWORD;
    private String defaultClientId          = DEFAULT_CLIENT_ID;
    private String defaultClientSecret      = DEFAULT_CLIENT_SECRET;
    private String defaultClientRedirectUri = DEFAULT_CLIENT_REDIRECT_URI;

    public TOAuth2(THttpClient client, String serverContextPath) {
        this.client            = client;
        this.serverContextPath = serverContextPath;
    }

    /**
     * Sets the default username and password.
     */
    public TOAuth2 withDefaultUser(String username, String password) {
        this.defaultUsername = username;
        this.defaultPassword = password;
        return this;
    }

    /**
     * Sets the default client's info.
     */
    public TOAuth2 withDefaultClient(String clientId, String secret, String redirectUri) {
        this.defaultClientId = clientId;
        this.defaultClientSecret = secret;
        this.defaultClientRedirectUri = redirectUri;
        return this;
    }

    /**
     * Login with default user and client and returns the access token from oauth2 server.
     */
    public String obtainAccessTokenImplicit() {
        return obtainAccessTokenImplicit(defaultClientId, defaultClientRedirectUri);
    }

    /**
     * Login with default user and the given client and returns the access token from oauth2 server.
     */
    public String obtainAccessTokenImplicit(String clientId, String redirectUri) {
        String uri = serverContextPath + authorizeEndpoint +
                "?client_id=" + clientId + "&redirect_uri=" + Urls.encode(redirectUri) + "&response_type=token";

        login();
        String redirectUrl = client.get(uri).assertRedirect().getRedirectUrl();

        Map<String,Object> params = QueryStringParser.parse(Urls.getQueryString(redirectUrl)).getParameters();
        if(params.containsKey("error")) {
            TestCase.fail("oauth2 error : " + params.get("error"));
        }

        return (String)params.get("access_token");
    }

    /**
     * Login the oauth2 server with default user.
     */
    public void login() {
        forLogin(defaultUsername, defaultPassword).sendAjax().assertOk();
    }

    /**
     * Login the oauth2 server with the given username and password.
     */
    public void login(String username,String password) {
        forLogin(username, password).sendAjax().assertOk();
    }

    /**
     * Logout from the oauth2 server.
     */
    public void logout() {
        client.request(serverContextPath + "/logout").setMethod(HTTP.Method.POST).sendAjax().assertOk();
    }

    /**
     * Sets the access token header in the http request.
     */
    public THttpRequest withAccessToken(THttpRequest request, String token) {
        return request.setHeader(Headers.AUTHORIZATION, "Bearer " + token).ajax();
    }

    protected THttpRequest forLogin(String username, String password) {
        return client.request(serverContextPath + "/login").addFormParam("username", username).addFormParam("password", password);
    }

}
