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

package leap.oauth2.webapp.client;

import leap.web.security.client.SimpleClientPrincipal;

import java.util.Map;

public class OAuth2Client extends SimpleClientPrincipal {

    private static final long serialVersionUID = -3438856033102626691L;

    public OAuth2Client(String id) {
        super(id);
    }

    public OAuth2Client(String id, Map<String, Object> properties) {
        super(id, properties);
    }
}