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
package leap.web.action;

import leap.lang.Beans;
import leap.lang.beans.BeanType;
import leap.web.App;
import leap.web.route.RouteBase;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * The {@link ArgumentResolver} for resolving complex type.
 */
public class ComplexArgumentResolver extends AbstractMapResolver {

	protected final BeanType beanType;
	protected final Class<?> beanClass;
	protected final String   prefix;
	protected final boolean  bindable;
	protected final boolean  map;

	public ComplexArgumentResolver(App app, RouteBase route, Argument argument){
		super(app,route,argument);
		this.beanClass = argument.getType();
		this.beanType  = BeanType.of(beanClass);
		this.prefix    = argument.getName() + ".";
		this.bindable  = Bindable.class.isAssignableFrom(beanClass);
		this.map       = Map.class.equals(beanClass);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final Object resolveValue(ActionContext context, Argument argument) throws Throwable {
		Map<String, Object> params = context.getMergedParameters();

		//direct mapping
		Object value = params.get(argument.getName());
		if(null != value){
			if(beanClass.isAssignableFrom(value.getClass())){
				return value;
			}

			if(value instanceof Map){
				return mapBinding(context, argument, (Map)value);
			}
		}

		Map<String, Object> map = null;

		for(Entry<String, Object> entry : params.entrySet()){
			String key = entry.getKey();
			if(key.startsWith(prefix)){
				if(null == map){
					map = new HashMap<>(beanType.getProperties().length);
				}

				map.put(key.substring(prefix.length()), entry.getValue());
			}
		}

		if(null == map){
			map = params;
		}

		//TODO : optimize performance
		return mapBinding(context, argument, resolveMap(map));
	}

	@SuppressWarnings("rawtypes")
	protected Map resolveMap(Map<String,Object> params) {
		Map map = new HashMap();

		for(Entry<String,Object> entry : params.entrySet()) {
			putToMap(map, entry.getKey(), entry.getValue());
		}

		return map;
	}

	protected Object mapBinding(ActionContext context,Argument argument,Map<String, Object> map) throws Throwable {
		if(this.map) {
			return new LinkedHashMap<>(map);
		}

        if(argument.getBinder() != null) {
            Optional value = argument.getBinder().bind(context, argument, map);
            if(null != value) {
                return value.get();
            }
        }

		Object bean = beanType.newInstance();

		if(bindable){
			((Bindable)bean).preBinding(context.getRequest(), context.getResponse(), context, context.getRequest().getValidation());
		}

		Beans.setPropertiesNestable(beanType, bean, map);

		if(bindable){
			((Bindable)bean).postBinding(context.getRequest(), context.getResponse(), context, context.getRequest().getValidation());
		}

		return bean;
	}
}