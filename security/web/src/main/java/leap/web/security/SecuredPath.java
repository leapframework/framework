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
import leap.core.web.RequestMatcher;
import leap.lang.Args;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.PathPattern;
import leap.web.Request;
import leap.web.security.authc.Authentication;
import leap.web.security.authc.AuthenticationContext;

import java.util.Comparator;

public class SecuredPath implements RequestMatcher,Comparable<SecuredPath> {

	private static final Log log = LogFactory.get(SecuredPath.class);

	public static Comparator<SecuredPath> COMPARATOR = new Comparator<SecuredPath>() {
		@Override
		public int compare(SecuredPath o1, SecuredPath o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return 1;
            }
            if (o2 == null) {
                return -1;
            }
            
            int i = o1.getPathPatternString().length() - o2.getPathPatternString().length();
            if(i != 0){
            	return i;
            }
            
            return -1;
		}
	};
	
	protected final PathPattern   pathPattern;
	protected final boolean		  allowAnonymous;
	protected final boolean       allowClientOnly;
	protected final boolean		  allowRememberMe;
	protected final SecuredRule[] rules;
	
	public SecuredPath(PathPattern pathPattern, 
	                   boolean allowAnonymous, boolean allowClientOnly, boolean allowRememberMe, SecuredRule[] rules) {
		Args.notNull(pathPattern,"path pattern");
		this.pathPattern	 = pathPattern;
	    this.allowAnonymous  = allowAnonymous;
	    this.allowClientOnly = allowClientOnly;
	    this.allowRememberMe = allowRememberMe;
	    this.rules           = rules;
    }
	
	public PathPattern getPathPattern() {
		return pathPattern;
	}
	
	public String getPathPatternString(){
		return pathPattern.getPattern();
	}
	
	@Override
    public boolean matches(RequestBase request) {
	    return pathPattern.matches(request.getPath());
    }
	
	public boolean matches(String path) {
		return pathPattern.matches(path);
	}
	
	public boolean isAllow(Request request, AuthenticationContext context, Authentication authc) {
		if(isAllowAnonymous()) {
			return true;
		}
		
		if(!authc.isAuthenticated()){
            log.debug("path [{}] : not authenticated, deny the request.", pathPattern);
			return false;
		}
		
		if(authc.isRememberMe() && !isAllowRememberMe()){
            log.debug("path [{}] : remember-me authentication not allowed.", pathPattern);
			return false;
		}
		
        if (authc.isClientOnly() && !isAllowClientOnly()) {
            log.debug("path [{}] : client-only authentication not allowed.", pathPattern);
            return false;
        }

		return applyRules(request, context, authc);
	}
	
    public boolean isAllowAnonymous() {
		return allowAnonymous;
	}
    
    public boolean isAllowClientOnly() {
        return allowClientOnly;
    }

	public boolean isAllowRememberMe() {
		return allowRememberMe;
	}
	
	private boolean applyRules(Request request, AuthenticationContext context, Authentication authc)  {
		
		if(null != rules && rules.length > 0){
			for(int i=0;i<rules.length;i++) {
				if(!rules[i].apply(request, context, authc)) {
					return false;
				}
			}
		}
		
	    return true;
    }

	@Override
    public int compareTo(SecuredPath o) {
	    return COMPARATOR.compare(this, o);
    }
}