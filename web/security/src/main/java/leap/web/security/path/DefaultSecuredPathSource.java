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
package leap.web.security.path;

import leap.core.annotation.Inject;
import leap.core.cache.Cache;
import leap.core.cache.SimpleLRUCache;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.PathPattern;
import leap.web.Request;
import leap.web.security.SecurityConfig;
import leap.web.security.SecurityContextHolder;

public class DefaultSecuredPathSource implements SecuredPathSource {

    private static final Log log = LogFactory.get(DefaultSecuredPathSource.class);
	
	private static final PathPattern ANY_PATTERN = new PathPattern() {
		
		@Override
		public boolean matches(String path) {
			return true;
		}
		
		@Override
		public String pattern() {
			return "/**";
		}

        @Override
        public String toString() {
            return pattern();
        }
    };
	
	private static final SecuredPath ANY  = new DefaultSecuredPathBuilder().setPattern(ANY_PATTERN).build();
	private static final SecuredPath NULL = new DefaultSecuredPathBuilder().setPattern(ANY_PATTERN).build();

    protected @Inject SecuredPathResolver[] resolvers;
    protected @Inject SecurityConfig        config;

	protected Cache<String, SecuredPath> cachedPaths = new SimpleLRUCache<>(1024);
	
	@Override
	public SecuredPath getSecuredPath(SecurityContextHolder context, Request request) {
        SecuredPath securedPath;
        for(SecuredPathResolver resolver : resolvers) {
            if((securedPath = resolver.resolveSecuredPath(context)) != null) {
                return securedPath;
            }
        }

		String cacheKey = genCacheKey(request);
	    securedPath = cachedPaths.get(cacheKey);
	    if(null != securedPath) {
	        return securedPath == NULL ? null : securedPath;
	    }

        log.debug("Matching request {} ...", request.getPath());
	    
		for(SecuredPath p : config.getSecuredPaths()){
			if(matches(p,request)) {
                log.debug("Matches -> {} {}", p.getRoute()==null?"*":p.getRoute().getMethod(), p.getPattern());
			    cachedPaths.put(cacheKey, p);
				return p;
			}
            log.debug("Not matches -> {}", p.getPattern());
		}
		
		if(config.isAuthenticateAnyRequests()) {
		    cachedPaths.put(request.getPath(), ANY);
		    return ANY;
		}
		
		cachedPaths.put(request.getPath(), NULL);
		return null;
	}

	private boolean matches(SecuredPath p,Request request){
		return p.matches(request) &&
					(p.getRoute() == null ||
						Strings.equals(p.getRoute().getMethod(),"*") ||
						Strings.equalsIgnoreCase(p.getRoute().getMethod(),request.getMethod()));
	}

	private String genCacheKey(Request request){
		return request.getMethod()+"$"+request.getPath();
	}
}
