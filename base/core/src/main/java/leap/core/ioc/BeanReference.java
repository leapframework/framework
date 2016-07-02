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
package leap.core.ioc;

class BeanReference {
	
	protected String 		 				targetId;
	protected String		 				beanName;
	protected Class						beanType;
	protected BeanDefinition targetBeanDefinition;

	public BeanReference(Class<?> beanType, String beanName) {
		this.beanType = beanType;
		this.beanName = beanName;
	}

	public BeanReference(String targetId){
		this.targetId = targetId;
	}

	public String getBeanName(){
		return this.beanName;
	}
	public <T> Class<T> getBeanType(){
		return this.beanType;
	}

	public String getTargetId() {
		return targetId;
	}

	public BeanDefinition getTargetBeanDefinition() {
		return targetBeanDefinition;
	}

	protected void setTargetBeanDefinition(BeanDefinition targetBeanDefinition) {
		this.targetBeanDefinition = targetBeanDefinition;
	}
}