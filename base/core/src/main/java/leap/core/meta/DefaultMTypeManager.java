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
package leap.core.meta;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ioc.PostCreateBean;
import leap.lang.meta.MType;
import leap.lang.meta.MTypeFactory;

import java.lang.reflect.Type;

public class DefaultMTypeManager implements MTypeManager, PostCreateBean {

	protected @Inject MTypeFactory[] typeFactories;
	
	private MTypeContainer defaultContainer;

    @Override
    public MType getMType(Class<?> type) {
        return defaultContainer.getMType(type, null);
    }

    @Override
    public MType getMType(Class<?> type, Type genericType) {
		return defaultContainer.getMType(type, genericType);
    }

	@Override
    public MTypeContainerCreator factory() {
	    return new DefaultMTypeContainer(typeFactories);
    }

	@Override
    public void postCreate(BeanFactory factory) throws Throwable {
		defaultContainer = factory().create();
    }

}