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
package leap.web.security.logout;

import leap.web.Request;
import leap.web.Response;
import leap.web.security.SecurityContextHolder;

public interface LogoutManager {

    /**
     * Handles an user logout request.
     *
     * <p/>
     * Returns <code>true</code> if manager handles the request.
     *
     * <p/>
     * Returns <code>false</code> if current request is not a logout request.
     */
    boolean handleLogoutRequest(Request request, Response response, LogoutContext context) throws Throwable;

    /**
     * Logout user in current session.
     */
    void logout(Request request, Response response) throws Throwable;

    /**
     * Logout user in current session with the {@link SecurityContextHolder}
     */
    void logout(Request request, Response response, LogoutContext context) throws Throwable;

}