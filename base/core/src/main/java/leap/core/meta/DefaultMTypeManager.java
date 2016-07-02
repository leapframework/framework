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

import java.lang.reflect.Type;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ioc.PostCreateBean;
import leap.lang.meta.MType;
import leap.lang.meta.MTypeFactory;
import leap.lang.meta.MTypeFactoryCreator;
import leap.lang.meta.SimpleMTypeFactoryCreator;

public class DefaultMTypeManager implements MTypeManager, PostCreateBean {

	protected @Inject MTypeFactory[] extendedMTypeFactories;
	
	private MTypeFactory rootMTypeFactory;
	
	@Override
    public MType getMType(Class<?> type, Type genericType) {
		return rootMTypeFactory.getMType(type, genericType);
    }

	@Override
    public MTypeFactoryCreator factory() {
	    return new ManagedMTypeFactoryCreator();
    }

	@Override
    public void postCreate(BeanFactory factory) throws Throwable {
		rootMTypeFactory = factory().create();
    }
	
	protected class ManagedMTypeFactoryCreator extends SimpleMTypeFactoryCreator implements MTypeFactory { 
		
		protected MTypeFactory root;

		@Override
        public MTypeFactory create() {
			root = super.create();
			return this;
        }

		@Override
        public MType getMType(Class<?> type, Type genericType, MTypeFactory root) {
			for(MTypeFactory f : extendedMTypeFactories) {
				MType mtype = f.getMType(type, genericType, root);
				
				if(null != mtype) {
					return mtype;
				}
			}
			
			return this.root.getMType(type, genericType);
        }
	}
	
}
