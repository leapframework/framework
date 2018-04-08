/*
 * Copyright 2014 the original author or authors.
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
package leap.web.security;

import leap.core.web.RequestBase;
import leap.core.security.Authentication;

public interface SecuritySessionManager {
	
	/**
	 * Returns <code>null</code> if no {@link Authentication} in current session.
	 */
	Authentication getAuthentication(RequestBase request);
	
	/**
	 * Saves the {@link Authentication} in current session.
	 */
	void saveAuthentication(RequestBase request, Authentication authentication);
	
	/**
	 * Removes the {@link Authentication} in current session.
	 *
	 * <p/>
	 * Returns the removed {@link Authentication} or <code>null</code> if no {@link Authentication} in session.
	 */
	Authentication removeAuthentication(RequestBase request);
}