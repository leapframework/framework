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
import java.util.Calendar;
import java.util.Date;

import leap.lang.Dates;
import leap.lang.Out;
import leap.lang.Strings;
import leap.lang.time.DateFormats;

public abstract class AbstractDateConverter<T> extends AbstractConverter<T> {
	
	protected String[] patterns;
	
	public AbstractDateConverter(){
		
	}

	public AbstractDateConverter(String pattern){
		this.patterns = new String[]{pattern};
	}
	
	public AbstractDateConverter(String[] patterns){
		this.patterns = patterns;
	}

	@Override
    public boolean convertFrom(Object value, Class<?> targetType, Type genericType, Out<Object> out) throws Throwable {
		if(value instanceof Date){
			out.set(convertFrom(targetType,(Date)value));
			return true;
		}else if(value instanceof Calendar){
			out.set(convertFrom(targetType,(Calendar)value));
			return true;
		}else if(value instanceof Long){
			out.set(convertFrom(targetType,(Long)value));
			return true;
		}else if(value instanceof CharSequence){
			String stringValue = value.toString();
			
			if(Strings.isDigits(stringValue)){
				out.set((convertFrom(targetType, Long.parseLong(stringValue))));
			}else{
				out.set(convertFrom(targetType,stringValue));
			}
			
			return true;
		}
		return false;
    }

	@Override
    public String convertToString(T value) throws Throwable {
	    return DateFormats.getFormat(value.getClass()).format((Date)value);
    }
	
	public void setPattern(String pattern){
		this.patterns = new String[]{pattern};
	}
	
	public void setPatterns(String[] patterns){
		this.patterns = patterns;
	}
	
	protected abstract T convertFrom(Class<?> targetType, Date date);
	
	protected abstract T convertFrom(Class<?> targetType, Calendar calendar);
	
	protected abstract T convertFrom(Class<?> targetType, Long time);
	
	protected T convertFrom(Class<?> targetType, String stringValue) {
		if(null == patterns || patterns.length == 0){
			return convertFrom(targetType, Dates.parse(stringValue,DateFormats.getPattern(targetType)));
		}else{
			Date date;
			
			for(String pattern : patterns){
				date = Dates.tryParse(stringValue,pattern);
				
				if(null != date){
					return convertFrom(targetType,date);
				}
			}
			
			throw new ConvertException("cannot convert string '" + stringValue + "' to '" + targetType.getName() +  "'");
		}
	}
}
