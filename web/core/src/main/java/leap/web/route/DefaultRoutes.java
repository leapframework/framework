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
import leap.lang.collection.ArrayIterator;
import leap.lang.http.HTTP.Method;
import leap.web.Handler;
import leap.web.action.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;

public class DefaultRoutes implements Routes {
	
	protected @Inject PathTemplateFactory pathTemplateFactory;
    protected @Inject ActionManager		actionManager;

    protected Route[]    array = new Route[]{};
    protected Set<Route> set   = new TreeSet<Route>(Route.COMPARATOR);
	
	@Override
    public int size() {
	    return array.length;
    }

	@Override
    public boolean isEmpty() {
	    return array.length == 0;
    }

	@Override
    public Iterator<Route> iterator() {
	    return new ArrayIterator<>(array);
    }
	
	@Override
    public RouteConfigurator create() {
		return new DefaultRouteConfigurator((c) -> {
			RouteBuilder rb = createRoute(c.getMethod(), c.getPath(), c.getHandler());
			
			rb.setSupportsMultipart(c.isSupportsMultipart());
			rb.setCorsEnabled(c.getCorsEnabled());
			rb.setCsrfEnabled(c.getCsrfEnabled());
			rb.setHttpsOnly(c.getHttpsOnly());
            rb.setAllowAnonymous(c.getAllowAnonymous());
            rb.setAllowClientOnly(c.getAllowClientOnly());

			Route r = rb.build();
			
			add(r);
			
			return r;
		});
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
		set.add(route);
		this.setNewArray();
	    return this;
    }

    @Override
    public boolean remove(Route route) {
        boolean r = set.remove(route);
        if(r) {
            this.setNewArray();
        }
        return r;
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
		Route[] routes = this.array;

		List<Route> matchedRoutes = new ArrayList<>();
		for(int i=0;i<routes.length;i++){
			Route route = routes[i];
			
			if(null == method || route.getMethod().equals("*") || route.getMethod().equals(method)){
				
				if(!matchRequiredParameters(route.getRequiredParameters(), inParameters)){
					continue;
				}
				
				if(route.getPathTemplate().match(path, outVariables)){
					matchedRoutes.add(route);
				}
			}
		}

		if(matchedRoutes.isEmpty()) return null;

		if(matchedRoutes.size() == 1) return matchedRoutes.get(0);

		Route target = rematch(matchedRoutes);

		return target;
    }

	private Route rematch(List<Route> matchedRoutes) {
		Route route = matchedRoutes.get(0);
		JerseyUriTemplate template = new JerseyUriTemplate(route.getPathTemplate().getTemplate());

		for (int i = 1; i < matchedRoutes.size(); i++) {
			Route other = matchedRoutes.get(i);
			JerseyUriTemplate otherTemplate = new JerseyUriTemplate(other.getPathTemplate().getTemplate());

			int re = JerseyUriTemplate.COMPARATOR.compare(template, otherTemplate);
			if(0 == re) {
				throw new IllegalStateException("Ambiguous handler methods mapped for path " +
						"'" + route.getPathTemplate() + "' and '" + other.getPathTemplate() + "'");
			} else if(re > 0) {
				route = other;
				template = otherTemplate;
			}
		}

		return route;
	}

	@Override
	public Route[] getRoutesByController(Object controller) {
		List<Route> routes = new LinkedList<>();
		for(Route route : this){
			if(route.getController() == controller){
				routes.add(route);
			}
		}
		return routes.toArray(new Route[]{});
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

	protected void setNewArray(){
		this.array = set.toArray(new Route[set.size()]);
	}
}