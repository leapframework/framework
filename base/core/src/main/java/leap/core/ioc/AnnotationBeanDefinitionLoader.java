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

import leap.core.annotation.Bean;
import leap.lang.Classes;
import leap.lang.Strings;
import leap.lang.beans.BeanType;


class AnnotationBeanDefinitionLoader {

	public void load(final BeanContainer container, Class<?>[] classes) {
		if(null == container.getAppConfig()){
			return;
		}
		
		String basePackage = container.getAppConfig().getBasePackage() + ".";

		//TODO : currently only supports classes in base-package
		for(Class<?> cls : classes){
			if(cls.isAnnotationPresent(Bean.class) && cls.getName().startsWith(basePackage)){
				readBean(container, cls);
			}
		}
	}
	
	protected void readBean(BeanContainer container,Class<?> cls){
		Bean a = cls.getAnnotation(Bean.class);

		BeanDefinitionBase bd = new BeanDefinitionBase(Classes.getClassResourcePath(cls));
		
		if(!Strings.isEmpty(a.value())) {
			bd.setId(a.value());
		}else{
			
			if(!void.class.equals(a.type())){
				bd.setType(a.type());
			}else{
				bd.setType(resolveBeanType(cls));
			}
			
			if(!Strings.isEmpty(a.id())) {
				bd.setId(a.id());
			}

			if(!Strings.isEmpty(a.name())){
				bd.setName(a.name());
			}
			
			bd.setBeanClass(cls);
			bd.setBeanClassType(BeanType.of(cls));
			bd.setPrimary(a.primary());
			bd.setSingleton(a.singleton());
			bd.setLazyInit(a.lazyInit());
			bd.setSortOrder(a.sortOrder());
			container.addBeanDefinition(bd);
		}
	}
	
	protected Class<?> resolveBeanType(Class<?> cls) {
		if(cls.getInterfaces().length == 1){
			return cls.getInterfaces()[0];
		}else{
			return cls;
		}
	}
}
