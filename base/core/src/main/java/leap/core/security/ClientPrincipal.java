/*
 * Copyright 2015 the original author or authors.
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
package leap.core.security;

import java.util.Collections;
import java.util.Map;

public interface ClientPrincipal extends Principal {

    /**
     * Returns appId of current client.
     *
     * <p/>
     * Returns {@link #getIdAsString()} as appId if no appId property exists.
     */
    default String getAppId() {
        Object v = getProperties().get("app_id");
        if(null == v) {
            v = getProperties().get("appId");
        }
        return null == v ? getIdAsString() : v.toString();
    }

    /**
     * Returns the details property.
     */
    default Map<String, Object> getProperties() {
        return Collections.emptyMap();
    }

}