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

import java.lang.reflect.UndeclaredThrowableException;

import leap.junit.concurrent.ConcurrentTestCase;
import leap.lang.Out;

abstract class ConverterTestBase extends ConcurrentTestCase {
	
	protected Object convertFrom(Converter<?> converter, Object value,Class<?> targetType) {
		Out<Object> out = new Out<Object>();
		
		try {
	        if(converter.convertFrom(value, targetType, null, out, null)){
	        	return out.getValue();
	        }
        } catch (Throwable e) {
        	throw new UndeclaredThrowableException(e);
        }
		
		throw new ConvertUnsupportedException();
	}
	
	protected Object convertFrom(Converter<?> converter, Class<?> targetType, Object value) {
		return convertFrom(converter,value,targetType);
	}	
	
	protected abstract Converter<?> getConverter();
	
}
