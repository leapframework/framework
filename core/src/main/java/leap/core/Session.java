/*
 * Copyright 2013 the original author or authors.
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
package leap.core;

import javax.servlet.http.HttpSession;

import leap.lang.accessor.AttributeAccessor;

public interface Session extends AttributeAccessor {

    /**
     * Invalidates this session then unbinds any objects bound to it.
     *
     * @exception IllegalStateException if this method is called on an already
     * invalidated session
     */
    void invalidate();
    
    /**
     * Returns <code>true</code> if this session is valid, that means the the {@link #invalidate()} method was not called.
     */
    boolean valid();
    
    /**
     * Returns the {@link HttpSession} if current environment is servlet environment.
     */
	javax.servlet.http.HttpSession getServletSession() throws IllegalStateException;
}