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
package leap.core.el;

import leap.core.BeanFactory;
import leap.lang.el.ElException;
import leap.lang.el.ElPropertyResolver;

public class BeansPropertyResolver implements ElPropertyResolver {
	
	protected BeanFactory factory;
	
	public BeansPropertyResolver(BeanFactory factory) {
		this.factory = factory;
	}

	@Override
    public Object resovleProperty(String name) {
		Object bean = factory.tryGetBean(name);
		
		if(null == bean){
			throw new ElException("Bean with id '" + name + "' cannot be resolved");
		}
		
	    return bean;
    }
	
}
