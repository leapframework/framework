/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.security.path;

import leap.core.cache.Cache;
import leap.core.cache.SimpleLRUCache;
import leap.lang.Strings;
import leap.web.Request;
import leap.web.security.SecurityContextHolder;

public class SimpleSecuredPathResolver implements SecuredPathResolver {

    protected static final SecuredPath NULL = new DefaultSecuredPathBuilder().path("/**").build();

    protected Cache<String, SecuredPath> cache = new SimpleLRUCache<>(2048);

    protected SecuredPaths paths = new DefaultSecuredPaths();

    public final SecuredPaths paths() {
        return paths;
    }

    @Override
    public final SecuredPath resolveSecuredPath(SecurityContextHolder context) {
        Request request = context.getRequest();
        String  key     = getCacheKey(context.getRequest());

        SecuredPath path = cache.get(key);
        if(null != path) {
            return path == NULL ? null : path;
        }

        path = resolve(request);
        if(null == path) {
            cache.put(key, NULL);
            return null;
        }else {
            cache.put(key, path);
            return path;
        }
    }

    protected SecuredPath resolve(Request request) {
        return resolve(request, this.paths);
    }

    protected final SecuredPath resolve(Request request, SecuredPaths paths) {
        for(SecuredPath p : paths){
            if(matches(p, request)) {
                return p;
            }
        }
        return null;
    }

    protected final boolean matches(SecuredPath p, Request request){
        return p.matches(request) &&
                (p.getRoute() == null ||
                        Strings.equals(p.getRoute().getMethod(),"*") ||
                        Strings.equalsIgnoreCase(p.getRoute().getMethod(),request.getMethod()));
    }

    protected final String getCacheKey(Request request){
        return request.getMethod() + "$" + request.getPath();
    }
}