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
import java.sql.Clob;

import leap.lang.Out;
import leap.lang.jdbc.SimpleClob;

public class ClobConverter extends AbstractConverter<Clob> {

	@Override
    public boolean convertFrom(Object value, Class<?> targetType, Type genericType, Out<Object> out, ConvertContext context) throws Throwable {
	    String string = Converts.toString(value);
	    out.set(new SimpleClob(string));
	    
	    return true;
    }

	@Override
    public boolean convertTo(Clob value, Class<?> targetType, Type genericType, Out<Object> out, ConvertContext context) throws Throwable {
	    out.set(Converts.convert(convertToString(value), targetType,genericType));
	    return true;
    }

	@Override
    public String convertToString(Clob value) throws Throwable {
		return value.getSubString(1,(int)value.length());
    }
}