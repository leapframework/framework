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
package leap.web.security.csrf;

import leap.core.Session;
import leap.web.Request;

public class SessionCsrfStore implements CsrfStore {
	
	private static final String KEY = SessionCsrfStore.class.getName() + "$token";

	@Override
	public String loadToken(Request request) throws Throwable {
		Session session = request.getSession(false);
		
		if(null == session) {
			return null;
		}
		
		return (String)session.getAttribute(KEY);
	}

	@Override
	public void saveToken(Request request, String token) throws Throwable {
		Session session = request.getSession();
		session.setAttribute(KEY, token);
	}

	@Override
	public void removeToken(Request request) throws Throwable {
		Session session = request.getSession(false);
		
		if(null != session) {
			session.removeAttribute(KEY);
		}
		
	}
}
