/*
 * Copyright 2013 the original author or authors.
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

import leap.lang.Emptiable;
import leap.lang.http.HTTP;
import leap.web.Handler;

import java.util.Map;
import java.util.function.Supplier;

public interface Routes extends Iterable<Route>,Emptiable {

	/**
	 * Returns the size of routes.
     */
	int size();

	/**
	 * Returns the path prefix.
	 */
	String getPathPrefix();

	/**
	 * Returns a new created {@link RouteConfigurator}.
	 */
	RouteConfigurator create();
	
	/**
	 * Adds a route handling a http get request. 
	 */
	default Routes get(String path, Handler handler) {
		return add(HTTP.Method.GET, path, handler);
	}
	
	/**
	 * Adds a route handling a http get request. 
	 */
	default Routes get(String path, Runnable handler) {
		return add(HTTP.Method.GET, path, handler);
	}
	
	/**
	 * Adds a route handling a http get request. 
	 */
	default <T> Routes get(String path, Supplier<T> handler) {
		return add(HTTP.Method.GET, path, handler);
	}
	
	/**
	 * Adds a route handling a http post request. 
	 */
	default Routes post(String path, Handler handler) {
		return add(HTTP.Method.POST, path, handler);
	}
	
	/**
	 * Adds a route handling a http post request. 
	 */
	default Routes post(String path, Runnable handler) {
		return add(HTTP.Method.POST, path, handler);
	}
	
	/**
	 * Adds a route handling a http post request. 
	 */
	default <T> Routes post(String path, Supplier<T> handler) {
		return add(HTTP.Method.POST, path, handler);
	}
	
	/**
	 * Adds a route handling a http put request. 
	 */
	default Routes put(String path, Handler handler) {
		return add(HTTP.Method.PUT, path, handler);
	}
	
	/**
	 * Adds a route handling a http put request. 
	 */
	default Routes put(String path, Runnable handler) {
		return add(HTTP.Method.PUT, path, handler);
	}
	
	/**
	 * Adds a route handling a http put request. 
	 */
	default <T> Routes put(String path, Supplier<T> handler) {
		return add(HTTP.Method.PUT, path, handler);
	}
	
	/**
	 * Adds a route handling a http delete request. 
	 */
	default Routes delete(String path, Handler handler) {
		return add(HTTP.Method.DELETE, path, handler);
	}
	
	/**
	 * Adds a route handling a http delete request. 
	 */
	default Routes delete(String path, Runnable handler) {
		return add(HTTP.Method.DELETE, path, handler);
	}
	
	/**
	 * Adds a route handling a http delete request. 
	 */
	default <T> Routes delete(String path, Supplier<T> handler) {
		return add(HTTP.Method.DELETE, path, handler);
	}
	
	/**
	 * Adds a route handling a http request. 
	 */
	default Routes add(String path, Handler handler) {
		return add(null, path, handler);
	}
	
	/**
	 * Adds a route handling a http request. 
	 */
	default Routes add(String path, Runnable handler) {
		return add(null, path, handler);
	}
	
	/**
	 * Adds a route handling a http request. 
	 */
	default <T> Routes add(String path, Supplier<T> handler) {
		return add(null, path, handler);
	}
	
	/**
	 * Adds a route handling a http request. 
	 */
	Routes add(HTTP.Method method, String path, Handler handler);
	
	/**
	 * Adds a route handling a http request. 
	 */
	Routes add(HTTP.Method method, String path, Runnable handler);
	
	/**
	 * Adds a route handling a http request. 
	 */
	<T> Routes add(HTTP.Method method, String path, Supplier<T> handler);
	
	/**
	 * Adds a route.
	 */
	Routes add(Route route);
	
	/**
	 * Adds all the routes. 
	 */
	Routes addAll(Iterable<Route> routes);

    /**
     * Returns true if a {@link Route} with the given path tempalte exists.
     */
    default boolean exists(String pathTemplate) {
        for(Route route : this) {
            if(route.getPathTemplate().getTemplate().equals(pathTemplate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the given {@link Route} exists.
     */
    boolean exists(Route route);

    /**
     * Removes the {@link Route}.
     */
    boolean remove(Route route);

    /**
     * Returns the matched {@link Route} or <code>null</code> if no route matched.
     */
    Route match(String method, String path);
	
	/**
	 * Returns a matched {@link Route} or <code>null</code> if no route matched.
	 */
	Route match(String method,String path,Map<String,Object> inParameters, Map<String,String> outVariables);

}