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
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.PathPattern;
import leap.web.Request;

public class DefaultSecurityPathSource implements SecurityPathSource {

    private static final Log log = LogFactory.get(DefaultSecurityPathSource.class);
	
	private static final PathPattern ANY_PATTERN = new PathPattern() {
		
		@Override
		public boolean matches(String path) {
			return true;
		}
		
		@Override
		public String getPattern() {
			return "/**";
		}

        @Override
        public String toString() {
            return getPattern();
        }
    };
	
	private static final SecurityPath ANY  = new SecurityPathBuilder().setPathPattern(ANY_PATTERN).build();
	private static final SecurityPath NULL = new SecurityPathBuilder().setPathPattern(ANY_PATTERN).build();

	protected @Inject @M SecurityConfig config;

	protected Cache<String, SecurityPath> cachedPaths = new SimpleLRUCache<>(1024);
	
	@Override
	public SecurityPath getSecuredPath(SecurityContextHolder context, Request request) {
	    SecurityPath securityPath = cachedPaths.get(request.getPath());
	    if(null != securityPath) {
	        return securityPath == NULL ? null : securityPath;
	    }

        log.debug("Matching request {} ...", request.getPath());
	    
		SecurityPath[] urls = config.getSecuredPaths();
		for(int i=0;i<urls.length;i++){
			SecurityPath u = urls[i];
			if(u.matches(request)) {
                log.debug("Matches -> {}", u.pathPattern);
			    cachedPaths.put(request.getPath(), securityPath);
				return u;
			}
            log.debug("Not matches -> {}", u.pathPattern);
		}
		
		if(config.isAuthenticateAnyRequests()) {
		    cachedPaths.put(request.getPath(), ANY);
		    return ANY;
		}
		
		cachedPaths.put(request.getPath(), NULL);
		return null;
	}
}
