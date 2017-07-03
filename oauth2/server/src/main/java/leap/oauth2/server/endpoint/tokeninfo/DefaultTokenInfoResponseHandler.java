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

package leap.oauth2.server.endpoint.tokeninfo;

import leap.lang.http.ContentTypes;
import leap.lang.json.JsonWriter;
import leap.oauth2.server.token.AuthzAccessToken;
import leap.web.Request;
import leap.web.Response;

import java.util.Map;

/**
 * Created by kael on 2016/7/28.
 */
public class DefaultTokenInfoResponseHandler implements TokenInfoResponseHandler {
    @Override
    public void writeTokenInfo(Request request, Response response, AuthzAccessToken at) {
        response.setContentType(ContentTypes.APPLICATION_JSON_UTF8);

        JsonWriter w = response.getJsonWriter();

        w.startObject()

                .property("user_id",       at.getUserId())
                .property("username",      at.getUsername())
                .property("expires_in",    at.getExpiresInFormNow())
                .propertyOptional("scope", at.getScope());

        if(at.isAuthenticated()){
            w.property("client_id",     at.getClientId());
        }

        if(at.hasExtendedParameters()) {
            for(Map.Entry<String, Object> entry : at.getExtendedParameters().entrySet()) {
                w.property(entry.getKey(), entry.getValue());
            }
        }

        w.endObject();
    }
}
