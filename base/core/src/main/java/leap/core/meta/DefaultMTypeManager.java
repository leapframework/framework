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
import leap.lang.meta.*;

public class DefaultMTypeManager implements MTypeManager, PostCreateBean {

	protected @Inject MTypeFactory[] extendedMTypeFactories;
	
	private MTypeFactory rootMTypeFactory;

    @Override
    public MType getMType(Class<?> type) {
        return rootMTypeFactory.getMType(type, null);
    }

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

	protected class ManagedMTypeFactoryCreator extends SimpleMTypeFactoryCreator implements MTypeFactory,MTypeContext {
		
		protected MTypeFactory root;

		@Override
        public MTypeFactory create() {
			root = new SimpleMTypeFactory(this);

			return this;
        }

        @Override
        public MTypeFactory root() {
            return root;
        }

        @Override
        public MTypeStrategy strategy() {
            return strategy;
        }

        @Override
        public MTypeListener listener() {
            return listener;
        }

        @Override
        public MType getMType(Class<?> type) {
            return getMType(type, null, this);
        }

        @Override
        public MType getMType(Class<?> type, Type genericType) {
            return getMType(type, genericType, this);
        }

        @Override
        public MType getMType(Class<?> type, Type genericType, MTypeContext context) {
            MType mtype = null;

			for(MTypeFactory f : extendedMTypeFactories) {
				mtype = f.getMType(type, genericType, context);
				
				if(null != mtype) {
					break;
				}
			}

            if(mtype != null) {

            }
			
			return this.root.getMType(type, genericType, context);
        }
	}
	
}
