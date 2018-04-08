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
package leap.oauth2.server.code;

import java.util.Map;

import leap.lang.expirable.TimeExpirable;

/**
 * The authorization code issued by authorization server.
 */
public interface AuthzCode extends TimeExpirable {

    /**
     * Required. The string value of code.
     */
    String getCode();

    /**
     * Returns the client id or <code>null</code>.
     */
    String getClientId();

    /**
     * Required. Returns the id of user.
     */
    String getUserId();

    /**
     * Return the session id of this code.
     */
    String getSessionId();
    
    /**
	 * Optional.
	 */
	Map<String, Object> getExtendedParameters();

	/**
	 * Optional, add an extend parameter to this token.
	 */
	void addExtendedParameters(String key, Object value);
}