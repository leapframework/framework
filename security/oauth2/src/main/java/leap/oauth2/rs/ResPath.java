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
package leap.oauth2.rs;

import leap.lang.path.PathPattern;
import leap.oauth2.rs.auth.ResAuthentication;
import leap.web.Request;
import leap.web.security.SecuredPath;
import leap.web.security.SecuredRule;
import leap.web.security.authc.AuthenticationContext;

public class ResPath extends SecuredPath {
	
	protected final ResScope scope;

	public ResPath(PathPattern pathPattern,
				   boolean allowAnonymous, boolean allowClientOnly, boolean allowRememberMe,
				   SecuredRule[] rules, ResScope scope) {
	    
		super(pathPattern, allowAnonymous, allowClientOnly, allowRememberMe, rules);
		
		this.scope = scope;
	}

	public ResScope getScope() {
		return scope;
	}

    public boolean isAllow(Request request, AuthenticationContext context, ResAuthentication authc) {
    	if(!super.isAllow(request, context, authc)) {
    		return false;
    	}
    	
		//Check scope.
		if(null != scope) {
			if(null != authc.getGrantedScope()) {
				for(String s : authc.getGrantedScope()) {
					if(scope.matches(s)) {
						return true;
					}
				}
				return false;
			}
		}
		
		return true;
    }
}