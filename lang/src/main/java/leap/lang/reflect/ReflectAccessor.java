/*
 * Copyright 2012 the original author or authors.
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface ReflectAccessor {

	boolean canNewInstance();

	Object newInstance();

	Object newArray(int length);

	int getArrayLength(Object array);

	Object getArrayItem(Object array, int index);

	void setArrayItem(Object array, int index, Object value);

	Object invokeMethod(Object object, int methodIndex, Object... args);

	void setFieldValue(Object object, int fieldIndex, Object value);

	Object getFieldValue(Object object, int fieldIndex);
	
	int getMethodIndex(Method method);
	
	int getFieldIndex(Field field);
}