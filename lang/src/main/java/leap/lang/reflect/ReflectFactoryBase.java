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
package leap.lang.reflect;

import leap.lang.Factory;

public abstract class ReflectFactoryBase implements ReflectFactory {
	
	protected static ReflectStrategy strategy = Factory.newInstance(ReflectStrategy.class,DefaultReflectionStrategy.class);

	@Override
    public final ReflectAccessor createAccessor(Class<?> clazz) {
	    if(strategy.canCreateAccessor(clazz)){
	    	return doCreateAccessor(clazz);
	    }
	    return null;
    }
	
	protected abstract ReflectAccessor doCreateAccessor(Class<?> clazz);
	
	public static class DefaultReflectionStrategy implements ReflectStrategy {
		@Override
        public boolean canCreateAccessor(Class<?> clazz) {
	        if(clazz.isAnonymousClass()){
	        	return false;
	        }
	        
	        if(clazz.isSynthetic()){
	        	return false;
	        }
	        
	        String className = clazz.getName();
	        if(className.startsWith("java.") || className.startsWith("sun.")){
	        	return false;
	        }
	        
	        return true;
        }
	}
}
