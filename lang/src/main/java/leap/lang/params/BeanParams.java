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
package leap.lang.params;

import leap.lang.Beans;
import leap.lang.beans.BeanType;

public class BeanParams extends NamedParamsBase {

	protected final Object     bean;
	protected final BeanType   beanType;
	
	public BeanParams(Object bean){
		super(BeanType.of(bean.getClass()).toMap(bean));
		this.bean       = bean;
		this.beanType   = BeanType.of(bean.getClass());
	}

	@Override
    protected void setRawValue(String name, Object value) {
		Beans.setProperty(beanType, bean, name, value, true);
	}

}