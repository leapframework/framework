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
package leap.lang.meta;

import java.lang.reflect.Type;

public class MTypes {
	
	private static final MTypeFactory DEFAULT_FACTORY = factory().create();
	
	public static MTypeFactoryCreator factory() {
		return new SimpleMTypeFactoryCreator();
	}
	
	public static MType getMType(Class<?> type) {
		return getMType(type, null);
	}
	
	public static MType getMType(Class<?> type, Type genericType) {
		return DEFAULT_FACTORY.getMType(type, genericType);
	}
	
	public static boolean isSimpleType(Class<?> type) {
		return null == type ? false : MSimpleTypes.tryForClass(type) != null;
	}
	
	public static boolean isCollectionType(Class<?> type) {
		return null == type ? false : type.isArray() || Iterable.class.isAssignableFrom(type);
	}
	
	protected MTypes() {
		
	}

}
