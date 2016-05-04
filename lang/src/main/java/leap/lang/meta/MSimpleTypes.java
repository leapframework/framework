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

import leap.lang.Strings;
import leap.lang.exception.ObjectNotFoundException;
import leap.lang.jdbc.JdbcType;
import leap.lang.jdbc.JdbcTypes;
import leap.lang.reflect.Reflection;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class MSimpleTypes {
	
	private static final Map<String, MSimpleType> types = new LinkedHashMap<String, MSimpleType>();
	
	public static final MSimpleType BINARY   = define(MSimpleTypeKind.BINARY, JdbcTypes.BINARY_TYPE_NAME, byte[].class, Byte[].class);
	
	public static final MSimpleType BOOLEAN  = define(MSimpleTypeKind.BOOLEAN, JdbcTypes.BOOLEAN_TYPE_NAME, Boolean.class, boolean.class);
	
	public static final MSimpleType BYTE     = define(MSimpleTypeKind.BYTE, JdbcTypes.TINYINT_TYPE_NAME,Byte.class, byte.class);
	
	public static final MSimpleType DATETIME = define(MSimpleTypeKind.DATETIME, JdbcTypes.TIMESTAMP_TYPE_NAME, 
													  Date.class, Timestamp.class,java.sql.Date.class,Calendar.class);
	
	public static final MSimpleType TIME	 = define(MSimpleTypeKind.TIME,JdbcTypes.TIME_TYPE_NAME,Time.class,Date.class);
	
	public static final MSimpleType DECIMAL  = define(MSimpleTypeKind.DECIMAL,JdbcTypes.DECIMAL_TYPE_NAME,19,4,BigDecimal.class);
	
	public static final MSimpleType SINGLE	 = define(MSimpleTypeKind.SINGLE,JdbcTypes.REAL_TYPE_NAME,Float.class,float.class);
	
	public static final MSimpleType DOUBLE   = define(MSimpleTypeKind.DOUBLE,JdbcTypes.DOUBLE_TYPE_NAME,Double.class,double.class);
	
	public static final MSimpleType SMALLINT = define(MSimpleTypeKind.SMALLINT,JdbcTypes.SMALLINT_TYPE_NAME,Short.class,short.class);
	
	public static final MSimpleType INTEGER  = define(MSimpleTypeKind.INTEGER,JdbcTypes.INTEGER_TYPE_NAME,Integer.class,int.class);
	
	public static final MSimpleType BIGINT	 = define(MSimpleTypeKind.BIGINT,JdbcTypes.BIGINT_TYPE_NAME,
													  Long.class,long.class,BigInteger.class);
	
	public static final MSimpleType STRING   = define(MSimpleTypeKind.STRING,JdbcTypes.VARCHAR_TYPE_NAME,255,
													  String.class,StringBuilder.class,char.class,Character.class);
	
	/**
	 * Returns all the {@link MSimpleType}.
	 */
	public static Iterable<MSimpleType> all(){
		return types.values();
	}
	
	public static MSimpleType forName(String name) throws ObjectNotFoundException {
		MSimpleType t = tryForName(name);
		if(null == t){
			throw new ObjectNotFoundException("Simple Type '" + name + "' not found");
		}
		return t;
	}
	
	public static MSimpleType tryForName(String name){
		return types.get(name.toLowerCase());
	}
	
	public static MSimpleType forClass(Class<?> cls) throws ObjectNotFoundException {
		MSimpleType t = tryForClass(cls);
		if(null == t){
			throw new ObjectNotFoundException("No simple type mapping to java class '" + cls + "'");
		}
		return t;
	}
	
	public static MSimpleType tryForClass(Class<?> cls) {
		if(cls.isEnum()){
            Field field = Reflection.findField(cls, "value");
            if(null != field) {
                return tryForClass(field.getType());
            }else{
				return tryForClass(String.class);
			}
		}
		
		for(MSimpleType t : all()){
			if(t.getJavaType().equals(cls)){
				return t;
			}
		}
		
		for(MSimpleType t : all()){
			for(Class<?> javaType : t.getJavaTypes()){
				if(javaType.equals(cls)){
					return t;
				}
			}
		}
		
		return null;
	}
	
	private static MSimpleType define(MSimpleTypeKind kind,String jdbcTypeName,Class<?>... javaTypes){
		return define(kind,jdbcTypeName,0,0,0,javaTypes);
	}
	
	private static MSimpleType define(MSimpleTypeKind kind,String jdbcTypeName, 
										 int defaultLength, Class<?>... javaTypes){
		return define(kind,jdbcTypeName,defaultLength,0,0,javaTypes);
	}
	
	private static MSimpleType define(MSimpleTypeKind kind,String jdbcTypeName,
										 int defaultPrecision,int defaultScale,Class<?>... javaTypes){
		
		return define(kind,jdbcTypeName,0,defaultPrecision,defaultScale,javaTypes);
	}
	
	private static MSimpleType define(MSimpleTypeKind kind,String jdbcTypeName, 
										 int defaultLength, int defautlPrecision, int defaultScale, Class<?>... javaTypes){
		String   name     = kind.name();
		String   title    = Strings.upperCamel(name);
		JdbcType jdbcType = JdbcTypes.forTypeName(jdbcTypeName);
		
		MSimpleType t = new MSimpleType(name, title, null, null, kind, jdbcType, javaTypes[0], javaTypes, 
											  defaultLength, defautlPrecision, defaultScale);
		
		types.put(name.toLowerCase(), t);
		
		return t;
	}
	
	protected MSimpleTypes() {
		
	}
}
