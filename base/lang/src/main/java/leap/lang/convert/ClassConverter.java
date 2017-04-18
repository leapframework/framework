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
package leap.lang.convert;

import java.lang.reflect.Type;

import leap.lang.Classes;
import leap.lang.Out;

public class ClassConverter extends AbstractConverter<Class<?>> implements Converter<Class<?>> {
	
	@Override
    public boolean convertFrom(Object value, Class<?> targetType, Type genericType, Out<Object> out, ConvertContext context) throws Throwable {
		out.set(Classes.forName(value.toString()));
		
		return true;
    }

	@Override
    public String convertToString(Class<?> value) throws Throwable {
		return value.getName();
    }

}