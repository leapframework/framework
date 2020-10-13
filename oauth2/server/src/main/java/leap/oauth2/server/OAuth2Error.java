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
package leap.oauth2.server;

import java.util.Map;

public interface OAuth2Error {
    
    /**
     * Required. Returns the standards error code.
     */
    String getError();

    /**
     * Required. Returns the detail error code
     */
    String getErrorCode();

    /**
     * Optional. Returns the suggest for this error
     */
    String getReferral();
    
    /**
     * Optional. Returns the error description.
     */
    String getErrorDescription();

    /**
     * Required. Returns the http status.
     */
    int getStatus();

    /**
     * Optional. Return the extend properties of this error.
     */
    default Map<String, Object> getProperties(){
        return null;
    }
}
