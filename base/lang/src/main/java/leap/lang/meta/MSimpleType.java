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
package leap.lang.meta;

import java.lang.reflect.Type;

import leap.lang.Args;
import leap.lang.Out;
import leap.lang.Strings;
import leap.lang.convert.AbstractConverter;
import leap.lang.convert.Converts;
import leap.lang.jdbc.JdbcType;

public class MSimpleType extends MType implements MNamed {
	
	protected final String			name;
	protected final String			title;
	protected final MSimpleTypeKind simpleTypeKind;
	protected final JdbcType        jdbcType;
	protected final Class<?>        javaType;
	protected final Class<?>[]      javaTypes;
	protected final int			    defaultLength;
	protected final int			    defaultPrecision;
	protected final int			    defaultScale;
	
	protected MSimpleType(String name, 
					   	  String title, 
					   	  String summary, 
					   	  String description, 
					   	  MSimpleTypeKind simpleTypeKind,
					   	  JdbcType jdbcType,
					   	  Class<?> javaType,
					   	  Class<?>[] javaTypes,
					   	  int defaultLength,
					   	  int defaultPrecision,
					   	  int defaultScale) {
	    super(summary, description);
	    
	    Args.notEmpty(name, "name");
	    Args.notNull(jdbcType,"jdbcType");
	    Args.notNull(javaType,"javaType");
	    Args.notEmpty(javaTypes,"javaTypes");
	    
	    this.name = name;
	    this.title = Strings.isEmpty(title) ? name : title;
	    this.simpleTypeKind = simpleTypeKind;
	    this.jdbcType = jdbcType;
	    this.javaType = javaType;
	    this.javaTypes = javaTypes;
	    this.defaultLength = defaultLength;
	    this.defaultPrecision = defaultPrecision;
	    this.defaultScale = defaultScale;
    }
	
	@Override
    public String getName() {
	    return name;
    }

	@Override
    public String getTitle() {
	    return title;
    }

	@Override
    public MTypeKind getTypeKind() {
	    return MTypeKind.SIMPLE;
    }

	public MSimpleTypeKind getSimpleTypeKind() {
		return simpleTypeKind;
	}

	/**
	 * Returns the default jdbc type mapping to.
	 */
	public JdbcType getJdbcType() {
		return jdbcType;
	}

	/**
	 * Returns the default java type mapping to.
	 */
	public Class<?> getJavaType() {
		return javaType;
	}

	/**
	 * Returns all the java types mapping to.
	 */
	public Class<?>[] getJavaTypes() {
		return javaTypes;
	}

	public int getDefaultLength() {
		return defaultLength;
	}

	public int getDefaultPrecision() {
		return defaultPrecision;
	}

	public int getDefaultScale() {
		return defaultScale;
	}
	
	/**
	 * Converts the input value to the value for this type.
	 */
	public Object valueOf(Object value) {
		if(null == value){
			return null;
		}
		
		for(Class<?> cls : javaTypes){
			if(cls.isAssignableFrom(value.getClass())){
				return value;
			}
		}
		
		return Converts.convert(value, javaType);
	}
	
	public static class MSimpleTypeConverter extends AbstractConverter<MSimpleType> {

		@Override
        public boolean convertFrom(Object value, Class<?> targetType, Type genericType, Out<Object> out) throws Throwable {
			if(value instanceof CharSequence){
				String name = value.toString();
				out.set(MSimpleTypes.forName(name));
				return true;
			}
			return false;
		}

		@Override
        public String convertToString(MSimpleType value) throws Throwable {
			return value.getName();
		}
	}
}