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

import leap.lang.Out;

public abstract class AbstractNumberConverter<T extends Number> extends AbstractConverter<T> {

    private static final Integer ZERO  = new Integer(0);
    private static final Integer ONE   = new Integer(1);
	private static final String  TRUE  = "true";
	private static final String  FALSE = "false";
    
	@Override
    public boolean convertFrom(Object value, Class<?> targetType, Type genericType, Out<Object> out, ConvertContext context) throws Throwable {
		if(value instanceof Number){
			out.set(toNumber(targetType, ((Number)value)));
		}else if(value instanceof Boolean){
			out.set(toNumber(targetType,((Boolean)value) ? ONE : ZERO));
		}else {
			out.set(toNumber(targetType, value));
		}
		return true;
    }
	
	protected T toNumber(Class<?> targetType,Object value) {
		String string = value.toString();
		
		if(string.equalsIgnoreCase(TRUE)){
			return toNumber(targetType, ONE);
		}else if(string.equalsIgnoreCase(FALSE)){
			return toNumber(targetType, ZERO);
		}else{
			return toNumber(targetType,string);	
		}
	}

	protected abstract T toNumber(Class<?> targetType,Number number);
	
	protected abstract T toNumber(Class<?> targetType,String stringValue);
}