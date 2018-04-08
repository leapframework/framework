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
package leap.oauth2.server.endpoint.userinfo;

import leap.lang.http.ContentTypes;
import leap.lang.json.JsonWriter;
import leap.oauth2.server.userinfo.AuthzUserInfo;
import leap.oauth2.server.userinfo.SimpleAuthzUserInfo;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.user.UserDetails;

import java.util.Map;

public class DefaultUserInfoHandler implements UserInfoHandler {

    @Override
    public boolean handleUserInfoResponse(Request request, Response response, UserDetails details) throws Throwable {
        AuthzUserInfo userInfo = createAuthzUserInfo(request,response,details);
        writeClaims(request, response, createClaims(userInfo));
        return true;
    }

    protected AuthzUserInfo createAuthzUserInfo(Request request, Response response, UserDetails details){
        SimpleAuthzUserInfo userInfo = new SimpleAuthzUserInfo();
        userInfo.setSubject(details.getIdAsString());
        userInfo.setFullName(details.getName());
        userInfo.putExtProperty("login_name",details.getLoginName());
        return userInfo;
    }
    
    protected Map<String, Object> createClaims(AuthzUserInfo userInfo) throws Throwable{
        //see http://openid.net/specs/openid-connect-core-1_0.html#StandardClaims
        //sub, name, email, gender
        //TODO :
        return userInfo.toMap();
    }

    protected void writeClaims(Request request, Response response, Map<String, Object> claim) throws Throwable {
        response.setContentType(ContentTypes.APPLICATION_JSON_UTF8);

        JsonWriter w = response.getJsonWriter();

        w.map(claim);

        return;
    }
}