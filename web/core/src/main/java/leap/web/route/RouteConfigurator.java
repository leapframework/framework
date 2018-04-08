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
package leap.web.route;

import leap.lang.http.HTTP;
import leap.web.Handler;

public interface RouteConfigurator {
	
	default RouteConfigurator get(String path, Handler handler) {
		return handle(HTTP.Method.GET, path, handler);
	}
	
	default RouteConfigurator get(String path, Runnable handler) {
		return handle(HTTP.Method.GET, path, handler);
	}
	
	default RouteConfigurator post(String path, Handler handler) {
		return handle(HTTP.Method.POST, path, handler);
	}
	
	default RouteConfigurator post(String path, Runnable handler) {
		return handle(HTTP.Method.POST, path, handler);
	}
	
	default RouteConfigurator put(String path, Handler handler) {
		return handle(HTTP.Method.PUT, path, handler);
	}
	
	default RouteConfigurator put(String path, Runnable handler) {
		return handle(HTTP.Method.PUT, path, handler);
	}
	
	default RouteConfigurator delete(String path, Handler handler) {
		return handle(HTTP.Method.DELETE, path, handler);
	}
	
	default RouteConfigurator delete(String path, Runnable handler) {
		return handle(HTTP.Method.DELETE, path, handler);
	}
	
	default RouteConfigurator handle(String path, Handler handler) {
		return handle(null, path, handler);
	}
	
	default RouteConfigurator handle(String path, Runnable handler) {
		return handle(null, path, handler);
	}
	
	RouteConfigurator handle(HTTP.Method method, String path, Handler handler);
	
	RouteConfigurator handle(HTTP.Method method, String path, Runnable handler);
	
	RouteConfigurator setSupportsMultipart(boolean v);

	RouteConfigurator setCorsEnabled(boolean enabled);
	
	RouteConfigurator setCsrfEnabled(boolean enabled);
	
	RouteConfigurator setHttpsOnly(boolean httpsOnly);
	
	RouteConfigurator setAllowAnonymous(boolean allow);
	
	RouteConfigurator setAllowClientOnly(boolean allow);
	
	default RouteConfigurator enableCors() {
		return setCorsEnabled(true);
	}
	
	default RouteConfigurator disableCors() {
		return setCorsEnabled(false);
	}
	
	default RouteConfigurator disableCsrf() {
		return setCsrfEnabled(false);
	}
	
	default RouteConfigurator enableCsrf() {
		return setCsrfEnabled(true);
	}

	default RouteConfigurator allowAnonymous() {
	    return setAllowAnonymous(true);
	}
	
	default RouteConfigurator allowClientOnly() {
	    return setAllowClientOnly(true);
	}

    default RouteConfigurator allowAny() {
        return allowAnonymous().enableCors().disableCsrf();
    }

	/**
	 * Apply the route.
	 */
	Route apply();
}