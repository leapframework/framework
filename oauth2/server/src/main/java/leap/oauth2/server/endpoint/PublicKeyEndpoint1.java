/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.oauth2.server.endpoint;

import leap.core.annotation.Inject;
import leap.lang.http.ContentTypes;
import leap.oauth2.as.OAuth2AuthzServerConfig;
import leap.web.*;
import leap.web.route.Routes;

import java.util.Base64;

public class PublicKeyEndpoint1 implements Endpoint, Handler {

    protected @Inject OAuth2AuthzServerConfig config;

    @Override
    public void startEndpoint(App app, Routes routes) throws Throwable {
        routes.create()
              .allowAny()
              .get(config.getPublicKeyEndpointPath(), this::handle)
              .apply();
    }

    @Override
    public void handle(Request request, Response response) throws Throwable {
        //todo: cache the encoded
        String s = new String(Base64.getMimeEncoder().encode(config.getPublicKey().getEncoded()));

        response.setContentType(ContentTypes.TEXT_PLAIN_UTF8);
        response.getWriter().print(s);
    }

}