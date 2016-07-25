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

package leap.oauth2.as.token;

import leap.oauth2.as.authc.AuthzAuthentication;
import leap.oauth2.as.client.AuthzClient;

/**
 * Created by kael on 2016/7/12.
 */
public interface ScopeMerger {
    /**
     * merge client scope and user scope as access token scope
     * @param client client
     * @param authc user authentication
     * @param scope existing scope
     * @return
     */
    String merge(AuthzClient client, AuthzAuthentication authc, String scope);

}
