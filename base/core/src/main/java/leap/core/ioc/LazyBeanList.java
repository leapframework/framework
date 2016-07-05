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

import java.util.List;

import leap.core.BeanFactory;
import leap.lang.Lazy;
import leap.lang.Strings;
import leap.lang.beans.BeanException;

public class LazyBeanList<T> implements Lazy<List<T>> {
	
	private final BeanFactory factory;
	private final Class<T>    type;
	private final String	  qualifier;
	private final boolean     notEmpty;
	private List<T> beans;
	
	LazyBeanList(BeanFactory factory,Class<T> type) {
		this(factory,type,null, false);
	}
	
	LazyBeanList(BeanFactory factory,Class<T> type,String qualifier, boolean notEmpty) {
		this.factory   = factory;
		this.type      = type;
		this.qualifier = Strings.trimToNull(qualifier);
		this.notEmpty  = notEmpty;
	}

	@Override
    public List<T> get() {
		if(null == beans){
			beans = null == qualifier ? factory.getBeans(type) : factory.getBeans(type,qualifier);
			
			if(notEmpty & beans.isEmpty()) {
			    StringBuilder s = new StringBuilder();
			    
                s.append("The list of bean [");

                if (null != type) {
                    s.append(" type=").append(type.getName());
                }

                s.append(" ] cannot be empty");

                throw new BeanException(s.toString());
			}
		}
	    return beans;
    }

}
