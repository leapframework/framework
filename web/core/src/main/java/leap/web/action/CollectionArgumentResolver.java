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
package leap.web.action;

import leap.lang.Beans;
import leap.lang.Strings;
import leap.lang.TypeInfo;
import leap.lang.beans.BeanType;
import leap.lang.convert.Converts;
import leap.web.App;
import leap.web.route.RouteBase;

import javax.servlet.http.Part;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;

/**
 * The {@link ArgumentResolver} for resolving collection type.
 */
public class CollectionArgumentResolver extends AbstractMapResolver {
	
	private final TypeInfo 						  			  elementType;
	private final BiFunction<ActionContext, Argument, Object> resolver;
	private final String									  arrayPrefix;
	private final RequestBodyArgumentResolver                 bodyResolver;
	
	public CollectionArgumentResolver(App app, RouteBase route, Argument argument, RequestBodyArgumentResolver bodyResolver) {
		super(app,route,argument);
		
		this.elementType = argument.getTypeInfo().getElementTypeInfo();
		this.arrayPrefix = argument.getName() + "[";
		
		if(this.elementType.isSimpleType()) {
			this.resolver = (ac,arg) -> resolveSimpleCollection(ac, arg);	
		}else{
			this.resolver = (ac,arg) -> resolveComplexCollection(ac, arg);
		}

		this.bodyResolver = bodyResolver;
	}

	@Override
	public Object resolveValue(ActionContext ac, Argument arg) throws Throwable {
		return resolver.apply(ac, arg);
	}
	
	protected Object resolveSimpleCollection(ActionContext ac, Argument arg) {
		try {
	        Object value = getParameter(ac, arg);
	        if(null == value){
	        	return resolveSimpleCollectionNoDirectMapping(ac,arg);
	        }else{
	        	if(value instanceof Part) {
	        		return convertFromPart((Part)value, arg);
	        	}
        		return Converts.convert(value, arg.getType(), arg.getGenericType());	
	        }
        } catch (Throwable e) {
        	if(e instanceof RuntimeException){
        		throw (RuntimeException)e;
        	}
        	throw new ArgumentException("Cannot resolve collection argument '" + arg.getName() + ", " + e.getMessage(), e);
        }
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
    protected Object resolveSimpleCollectionNoDirectMapping(ActionContext ac, Argument arg) throws Throwable {
	    //Only one argument for non-GET request
	    if(!ac.getRequest().isGet() && ac.getAction().getArguments().length == 1) {
	        Object value = bodyResolver.resolveValue(ac, arg);
	        if(null != value) {
	            return value;
            }
        }

		Map<String, Object> params = ac.getMergedParameters();
		
		List list = null;
		
		//items[0] items[1] items[]
		for(Entry<String, Object> entry : params.entrySet()){
			String key = entry.getKey();
			if(key.startsWith(arrayPrefix)) {
				int endArrayPos = key.indexOf(']',arrayPrefix.length());
				if(endArrayPos == key.length() - 1) {
					if(null == list){
						list = new ArrayList();
					}
					
					String indexString = key.substring(arrayPrefix.length(), endArrayPos).trim();
					if(Strings.isEmpty(indexString)) {
						Object value = entry.getValue();
						if(value instanceof String[]) {
							String[] a = (String[])value;
							for(String s : a) {
								list.add(s);
							}
						}else{
							list.add(value);
						}
					}else{
						int index = Integer.parseInt(indexString);
						tryIncreaseSize(list,index);
						list.set(index, entry.getValue());
					}
				}
			}
		}
		
		if(null != list){
			return Converts.convert(list, arg.getType(),arg.getGenericType());
		}else{
			return null;
		}
	}
	
	protected Object resolveComplexCollection(ActionContext ac, Argument arg) {
		try {
			//direct binding
			Object value = getParameter(ac, arg);
			if(null != value){
				return Converts.convert(value, arg.getType(),arg.getGenericType());
			}
			
			return bindingComplexCollectionFromParameters(ac, arg);
        } catch (Throwable e) {
        	if(e instanceof RuntimeException){
        		throw (RuntimeException)e;
        	}
        	throw new ArgumentException("Cannot resolve collection argument '" + arg.getName() + ", " + e.getMessage(), e);
        }
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
    protected Object bindingComplexCollectionFromParameters(ActionContext ac, Argument arg) throws Throwable  {
		Map<String, Object> params = ac.getMergedParameters();
		
		List<Map> list = new ArrayList<Map>();
		
		//TODO : optimize
		
		//parameters binding
		//items[0].name1,items[0].name2, items[0][name3] ...
		for(Entry<String, Object> entry : params.entrySet()){
			String key = entry.getKey();
			if(key.startsWith(arrayPrefix)){
				int endArrayPos = key.indexOf(']',arrayPrefix.length());
				if(endArrayPos > 0 && key.length() > endArrayPos + 2) {
					int index = Integer.parseInt(key.substring(arrayPrefix.length(), endArrayPos).trim());
					String name  = key.substring(endArrayPos + 1);
					Object value = entry.getValue();
					addToList(list, index, name, value);
					/*
					if(index > maxIndex) {
						maxIndex = index;
						increaseSize(list,maxIndex);
					}
					Map item = list.get(index);
					if(null == item){
						item = new HashMap();
						list.set(index, item);
					}
					

					
					char c0 = name.charAt(0);
					
					if(c0 == '.') {
						name = name.substring(1);
						putToMap(item,name,value);
					}else if(c0 == '[' && name.endsWith("]")) {
						name = name.substring(1,name.length() - 1).trim();
						putToMap(item,name,value);
					}
					*/
				}
			}
		}
		
		//return Converts.convert(list, arg.getType(),arg.getGenericType());
		
		//map
		if(elementType.getType().equals(Map.class)) {
			if(arg.getType().isArray()) {
				return list.toArray(new Map[list.size()]);
			}else{
				return list;
			}
		}
		
		BeanType beanType = BeanType.of(elementType.getType());
		
		//bean
		if(arg.getType().isArray()) {
			Object array = Array.newInstance(elementType.getType(), list.size());

			for(int i=0;i<list.size();i++) {
				Map item = list.get(i);
				if(null != item) {
					Array.set(array, i, mapToBean(ac, arg, beanType, item));	
				}
			}
			
			return array;
		}else{
			List beans = new ArrayList();
			
			for(int i=0;i<list.size();i++) {
				Map item = list.get(i);
				if(null != item) {
					beans.add(mapToBean(ac, arg, beanType, item));
				}else{
					beans.add(null);
				}
			}
			
			return beans;
		}
	}
	
    protected Object mapToBean(ActionContext context,Argument argument,BeanType beanType, Map<String, Object> map) {
    	Object bean = beanType.newInstance();
    	
    	Beans.setPropertiesNestable(beanType, bean, map);
    	
    	return bean;
    }

}