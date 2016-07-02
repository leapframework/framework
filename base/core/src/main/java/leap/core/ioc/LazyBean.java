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
package leap.core.ioc;

import leap.core.BeanFactory;
import leap.lang.Lazy;
import leap.lang.Objects2;
import leap.lang.Strings;
import leap.lang.beans.BeanException;

public class LazyBean<T> implements Lazy<T> {
	
	private final BeanFactory factory;
	private final Class<T>    type;
	private final String 	  name;
	private final boolean	  namedOrPrimary;
	private final boolean	  nullable;
	private final boolean     required;
	
	private T bean;
	
	LazyBean(BeanFactory factory,Class<T> type) {
		this(factory,type,null,true, true, false);
    }

	LazyBean(BeanFactory factory,Class<T> type, String name)  {
		this(factory,type,name,true, true, false);
	}
	
	LazyBean(BeanFactory factory,Class<T> type, boolean namedOrPrimary) {
		this(factory,type,null,namedOrPrimary, true, false);
    }

	LazyBean(BeanFactory factory,Class<T> type, String name, boolean namedOrPrimary, boolean nullable, boolean required)  {
		this.factory  = factory;
		this.type     = type;
		this.name     = Strings.trimToNull(name);
		this.namedOrPrimary = namedOrPrimary;
		this.nullable = nullable;
		this.required = required;
	}

	@Override
    public T get() {
		if(null == bean) {
			if(null == name){
				bean = factory.getBean(type);
			}else{
				if(!namedOrPrimary){
					bean = factory.getBean(type,name);
				}else{
					bean = factory.tryGetBean(type,name);
					
					if(null == bean){
						bean = factory.getBean(type);
					}
				}
			}
			
			if(required && Objects2.isEmpty(bean)) {
	             StringBuilder s = new StringBuilder();
	                
                s.append("Bean [");

                if (null != type) {
                    s.append(" type=").append(type.getName());
                }

                if (null != name) {
                    s.append(" name=").append(name);
                }

                s.append(" ] cannot be empty");

                throw new BeanException(s.toString());
			}
			
			if(null == bean && !nullable) {
				StringBuilder s = new StringBuilder();
				
				s.append("Bean [");
				
				if(null != type) {
					s.append(" type=").append(type.getName());
				}
				
				if(null != name) {
					s.append(" name=").append(name);
				}
				
				s.append(" ] cannot be null");
			
				throw new BeanException(s.toString());
			}
		}
		return bean;
	}
}