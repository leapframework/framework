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

import leap.core.annotation.Inject;
import leap.lang.intercepting.State;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.SecurityConfig;

public class DefaultCsrfHandler implements CsrfHandler {
	
	protected @Inject SecurityConfig config;
	protected @Inject CsrfManager	 manager;
	
	@Override
    public State handleRequest(Request request, Response response) throws Throwable {
		//Ignore if csrf not enabled.
		if(!config.isCsrfEnabled()) {
			return State.CONTINUE;
		}
		
		CsrfToken token = null;
		
		String savedToken = manager.loadToken(request);
		
		if(null == savedToken) {
			savedToken = manager.generateToken(request);
			token = new SaveOnAccessCsrfToken(config, savedToken, request, manager);
		}else{
			token = new SimpleCsrfToken(config, savedToken, false);
		}
		
		//Set attributes
		CSRF.setGeneratedToken(request, token);
		request.setAttribute(config.getCsrfParameterName(), token);
		
		return State.CONTINUE;
    }
	
	protected static class SaveOnAccessCsrfToken extends SimpleCsrfToken{
		
		private final Request	  request;
		private final CsrfManager manager;
		
		private boolean saved;
		
		public SaveOnAccessCsrfToken(SecurityConfig config, String token, Request request, CsrfManager manager) {
			super(config,token, true);
			this.request = request;
			this.manager   = manager;
		}

		@Override
        public String getToken() {
			if(!saved) {
				try {
	                manager.saveToken(request, token);
                } catch (Throwable e) {
                	throw new IllegalStateException("Error saving csrf token , " + e.getMessage(), e);
                }
				saved = true;
			}
	        return token;
        }

		@Override
        public String toString() {
			return getToken();
		}
	}
	
}
