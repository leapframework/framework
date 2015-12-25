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
package leap.lang.convert;

import java.lang.reflect.Type;
import java.time.Instant;

import leap.lang.Out;

public abstract class AbstractDateTimeConverter<T> implements Converter<T> {
	
	@Override
    public boolean convertTo(T value, Class<?> targetType, Type genericType, Out<Object> out) throws Throwable {
		if(targetType.equals(Instant.class)) {
			out.set(convertToInstant(value));
			return true;
		}
	    return false;
    }
	
	@Override
    public boolean convertFrom(Object value, Class<?> targetType, Type genericType, Out<Object> out) throws Throwable {
		if(value instanceof CharSequence) {
			out.set(convertFromString((CharSequence)value));
			return true;
		}
		
		if(value instanceof Long) {
			out.set(convertFromLong((Long)value));
			return true;
		}
		
		Instant instant = Converts.tryConvert(value, Instant.class, genericType);
		if(null == instant) {
			return false;
		}else{
			out.set(convertFromInstant(instant));
			return true;
		}
    }
	
	protected T convertFromLong(long l) {
		return convertFromInstant(Instant.ofEpochMilli(l));
	}
	
	protected abstract T convertFromString(CharSequence cs);
	
	protected abstract T convertFromInstant(Instant instant);

	protected abstract Instant convertToInstant(T value);
}