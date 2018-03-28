/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.security;

public interface SecuredObject {

    /**
     * Returns true or false if allow or deny the authentication.
     */
    default boolean checkAuthentication(SecurityContextHolder context) {
        Boolean b = tryCheckAuthentication(context);
        return null == b ? true : b;
    }

    /**
     * Returns true or false if allow or deny the authorization.
     */
    default boolean checkAuthorization(SecurityContextHolder context) {
        Boolean b = tryCheckAuthorization(context);
        return null == b ? true : b;
    }

    Boolean tryCheckAuthentication(SecurityContextHolder context);

    Boolean tryCheckAuthorization(SecurityContextHolder context);

}