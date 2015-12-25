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
package leap.web.action;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import leap.lang.Args;
import leap.lang.Builders;
import leap.lang.reflect.ReflectMethod;

public class MethodActionBuilder implements ActionBuilder {
	
	protected Object                  controller;
	protected ReflectMethod           method;
	protected List<ArgumentBuilder>   arguments = new ArrayList<ArgumentBuilder>();
	protected List<ActionInterceptor> interceptors = new ArrayList<ActionInterceptor>();
	
	public MethodActionBuilder(Object controller, ReflectMethod method) {
		Args.notNull(controller);
		Args.notNull(method);
		
		this.controller = controller;
		this.method     = method;
	}

	@Override
    public String getName() {
	    return method.getName();
    }

	public Object getController() {
		return controller;
	}

	public ReflectMethod getMethod() {
		return method;
	}

	@Override
    public Annotation[] getAnnotations() {
	    return method.getAnnotations();
    }

	public List<ArgumentBuilder> getArguments() {
		return arguments;
	}
	
	public void addArgument(ArgumentBuilder arg) {
		arguments.add(arg);
	}

	public List<ActionInterceptor> getInterceptors() {
		return interceptors;
	}
	
	public void addInterceptor(ActionInterceptor i) {
		interceptors.add(i);
	}
	
	@Override
    public boolean isAnnotationPresent(Class<? extends Annotation> t) {
	    return method.isAnnotationPresent(t);
    }

	@Override
    public Action build() {
	    return new MethodAction(controller, method, 
	    						Builders.buildArray(arguments, new Argument[arguments.size()]), 
	    						interceptors.toArray(new ActionInterceptor[]{}));
    }

}
