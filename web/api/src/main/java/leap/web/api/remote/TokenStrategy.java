/*
 * Copyright 2020 the original author or authors.
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

package leap.web.api.remote;

import leap.oauth2.webapp.OAuth2ConfigBase;

public interface TokenStrategy {

    /**
     * The strategy type enum.
     */
    enum Type {
        DEFAULT,
        ORIGINAL,
        FORCE_WITH_APP,
        TRY_WITH_APP,
        APP_ONLY
    }

    /**
     * Returns the {@link Token} of <code>null</code>.
     */
    default Token getToken() {
        return getToken(null);
    };

    /**
     * Returns the {@link Token} of <code>null</code> that uses another oauth2 config.
     */
    Token getToken(OAuth2ConfigBase oc);

}
