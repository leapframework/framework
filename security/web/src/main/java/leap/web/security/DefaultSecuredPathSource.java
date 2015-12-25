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

import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.cache.Cache;
import leap.core.cache.SimpleLRUCache;
import leap.lang.path.PathPattern;
import leap.web.Request;

public class DefaultSecuredPathSource implements SecuredPathSource {
	
	private static final PathPattern ANY_PATTERN = new PathPattern() {
		
		@Override
		public boolean matches(String path) {
			return true;
		}
		
		@Override
		public String getPattern() {
			return "/**";
		}
	};
	
	private static final SecuredPath ANY  = new SecuredPathBuilder().setPathPattern(ANY_PATTERN).build();
	private static final SecuredPath NULL = new SecuredPathBuilder().setPathPattern(ANY_PATTERN).build();

	protected @Inject @M SecurityConfig config;

	protected Cache<String, SecuredPath> cachedPaths = new SimpleLRUCache<>(1024);
	
	@Override
	public SecuredPath getSecuredPath(SecurityContextHolder context, Request request) {
	    SecuredPath securedPath = cachedPaths.get(request.getPath());
	    if(null != securedPath) {
	        return securedPath == NULL ? null : securedPath;
	    }
	    
		SecuredPath[] urls = config.getSecuredPaths();
		for(int i=0;i<urls.length;i++){
			SecuredPath u = urls[i];
			if(u.matches(request)) {
			    cachedPaths.put(request.getPath(), securedPath);
				return u;
			}
		}
		
		if(config.isAuthenticateAnyRequests()) {
		    cachedPaths.put(request.getPath(), ANY);
		    return ANY;
		}
		
		cachedPaths.put(request.getPath(), NULL);
		return null;
	}
}
