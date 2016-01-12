/*
 * Copyright 2013 the original author or authors.
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
package leap.oauth2.as.endpoint.userinfo;

import leap.lang.http.ContentTypes;
import leap.lang.json.JsonWriter;
import leap.oauth2.as.token.AuthzAccessToken;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.user.UserDetails;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultUserInfoHandler implements UserInfoHandler {

    @Override
    public boolean handleUserInfoResponse(Request request, Response response, AuthzAccessToken at, UserDetails details) throws Throwable {
        writeClaims(request, response, createClaims(at, details));
        return true;
    }

    protected Map<String,Object> createClaims(AuthzAccessToken at, UserDetails user) throws Throwable{
        Map<String,Object> claims = new LinkedHashMap<>();

        //see http://openid.net/specs/openid-connect-core-1_0.html#StandardClaims
        //sub, name, email, gender

        return claims;
    }

    protected void writeClaims(Request request, Response response, Map<String, Object> claims) throws Throwable {
        response.setContentType(ContentTypes.APPLICATION_JSON_UTF8);

        JsonWriter w = response.getJsonWriter();

        w.map(claims);

        return;
    }
}