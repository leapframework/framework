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

import leap.core.annotation.Inject;
import leap.core.web.path.JerseyUriTemplate;
import leap.core.web.path.PathTemplateFactory;
import leap.lang.Args;
import leap.lang.New;
import leap.lang.http.HTTP.Method;
import leap.web.Handler;
import leap.web.action.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public class DefaultRoutes implements Routes {
	
	protected @Inject PathTemplateFactory pathTemplateFactory;
    protected @Inject ActionManager		  actionManager;

    protected final List<Route> list = new CopyOnWriteArrayList<>();
	
	@Override
    public int size() {
	    return list.size();
    }

	@Override
    public boolean isEmpty() {
	    return list.isEmpty();
    }

	@Override
    public Iterator<Route> iterator() {
	    return list.iterator();
    }
	
	@Override
    public RouteConfigurator create() {
		return new DefaultRouteConfigurator(this);
    }

	@Override
    public Routes add(Method method, String path, Runnable handler) {
		Args.notNull(handler, "handler");
		return doAdd(method, path, () -> new RunnableAction(handler));
    }
	
	@Override
    public <T> Routes add(Method method, String path, Supplier<T> handler) {
		Args.notNull(handler, "handler");
		return doAdd(method, path, () -> new SupplierAction(handler));
    }

	@Override
    public Routes add(Method method, String path, Handler handler) {
		Args.notNull(handler, "handler");
		return doAdd(method, path, () -> new HandlerAction(handler));
    }
	
	protected Routes doAdd(Method method, String path, Supplier<Action> action) {
	    return add(createRoute(method, path, action).build());
	}
	
	protected RouteBuilder createRoute(Method method, String path, Supplier<Action> action) {
		return createRoute(method, path, action.get());
	}
	
	protected RouteBuilder createRoute(Method method, String path, Action action) {
		Args.notEmpty(path, "path");
		
		RouteBuilder r = new RouteBuilder();
		
		r.setPathTemplate(pathTemplateFactory.createPathTemplate(path));
		r.setAction(action);
		
		if(null != method) {
			r.setMethod(method.name());
		}else{
			r.setMethod("*");
		}
		
		actionManager.prepareAction(r);
		
	    return r;
	}

	@Override
    public synchronized Routes add(Route route) {
		Args.notNull(route,"route");
		list.add(route);
        Collections.sort(list, Route.COMPARATOR);
	    return this;
    }

    @Override
    public boolean exists(Route route) {
        return list.contains(route);
    }

    @Override
    public boolean remove(Route route) {
        return list.remove(route);
    }

    @Override
    public synchronized Routes addAll(Iterable<Route> routes) {
		Args.notNull(routes,"routes");
		for(Route route : routes){
			add(route);
		}
		return this;
    }

    @Override
    public Route match(String method, String path) {
        return match(method, path, New.hashMap(), New.hashMap());
    }

    @Override
    public Route match(String method, String path, Map<String,Object> inParameters,  Map<String, String> outVariables) {
		List<Route> matchedRoutes = new ArrayList<>();
		for(Route route : list){
			if(null == method || route.getMethod().equals("*") || route.getMethod().equals(method)){
				
				if(!matchRequiredParameters(route.getRequiredParameters(), inParameters)){
					continue;
				}
				
				if(route.getPathTemplate().match(path, outVariables)){
					matchedRoutes.add(route);
				}
			}
		}

		if(matchedRoutes.isEmpty()) {
            return null;
        }

        Route route = null;

		if(matchedRoutes.size() == 1) {
            route = matchedRoutes.get(0);
        }else{
            route = rematch(matchedRoutes);

            // get the right path template variables.
			outVariables.clear();
			route.getPathTemplate().match(path, outVariables);
        }

        if(route instanceof NestedRoute) {
            route = ((NestedRoute) route).match(method, path, inParameters, outVariables);
        }

		return route;
    }

	private Route rematch(List<Route> matchedRoutes) {
		// find the route of the highest priority
		return matchedRoutes.stream().min((r1, r2) -> {
			JerseyUriTemplate t1 = new JerseyUriTemplate(r1.getPathTemplate().getTemplate());
			JerseyUriTemplate t2 = new JerseyUriTemplate(r2.getPathTemplate().getTemplate());
			int re = JerseyUriTemplate.COMPARATOR.compare(t1, t2);
			if(0 == re){
				throw new IllegalStateException("Ambiguous handler methods mapped for path " +
						"'" + r1.getPathTemplate() + "' and '" + r2.getPathTemplate() + "'");
			}
			return re;
		}).get();
	}

	protected boolean matchRequiredParameters(Map<String, String> requiredParameters, Map<String, Object> inParameters) {
		if(requiredParameters.isEmpty()){
			return true;
		}
		
		for(Entry<String, String> entry : requiredParameters.entrySet()){
			
			Object v = inParameters.get(entry.getKey());
			
			if(null == v || !(v instanceof String)){
				return false;
			}
			
			if(!((String)v).equals(entry.getValue())){
				return false;
			}
		}
		
		return true;
	}
}