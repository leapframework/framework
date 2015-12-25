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
package leap.lang.edm;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import leap.lang.Enumerable;
import leap.lang.Enumerables;
import leap.lang.Named;
import leap.lang.value.DateTimeOffset;
import leap.lang.value.Guid;
import leap.lang.value.UnsignedByte;

public class EdmSimpleType extends EdmType implements Named {
	
	private static final Map<EdmSimpleTypeKind, EdmSimpleType> map = new LinkedHashMap<EdmSimpleTypeKind, EdmSimpleType>();
	
	public static EdmSimpleType BINARY 				= add(EdmSimpleTypeKind.Binary,			byte[].class,Byte[].class);
	public static EdmSimpleType BOOLEAN 			= add(EdmSimpleTypeKind.Boolean,		Boolean.class,boolean.class);
	public static EdmSimpleType BYTE 				= add(EdmSimpleTypeKind.Byte,			UnsignedByte.class);
	public static EdmSimpleType SBYTE 				= add(EdmSimpleTypeKind.SByte,			Byte.class,byte.class);
	public static EdmSimpleType DATETIME 			= add(EdmSimpleTypeKind.DateTime,		Date.class,Timestamp.class,java.sql.Date.class,Calendar.class);
	public static EdmSimpleType DATETIME_OFFSET 	= add(EdmSimpleTypeKind.DateTimeOffset,	DateTimeOffset.class);
	public static EdmSimpleType TIME 				= add(EdmSimpleTypeKind.Time,			Time.class,Date.class);
	public static EdmSimpleType DECIMAL 			= add(EdmSimpleTypeKind.Decimal,		BigDecimal.class);
	public static EdmSimpleType DOUBLE 				= add(EdmSimpleTypeKind.Double,			Double.class,double.class);
	public static EdmSimpleType GUID 				= add(EdmSimpleTypeKind.Guid,			Guid.class,UUID.class);
	public static EdmSimpleType INT16				= add(EdmSimpleTypeKind.Int16,			Short.class,short.class);
	public static EdmSimpleType INT32 				= add(EdmSimpleTypeKind.Int32,			Integer.class,int.class);
	public static EdmSimpleType INT64 				= add(EdmSimpleTypeKind.Int64,			Long.class,long.class,BigInteger.class);
	public static EdmSimpleType SINGLE 				= add(EdmSimpleTypeKind.Single,			Float.class,float.class);
	public static EdmSimpleType STREAM 				= add(EdmSimpleTypeKind.Stream);
	public static EdmSimpleType STRING			 	= add(EdmSimpleTypeKind.String,			String.class,char.class,Character.class);
	
	public static final Enumerable<EdmSimpleType> ALL = Enumerables.of(map.values());
	
	public static EdmSimpleType of(String kindName) {
		for(EdmSimpleType type : ALL){
			if(type.getName().equals(kindName)){
				return type;
			}
		}
		return null;
	}
	
	public static EdmSimpleType of(EdmSimpleTypeKind kind) {
		return map.get(kind);
	}
	
	public static EdmSimpleType of(Class<?> javaType){
		for(EdmSimpleType edmType : ALL){
			if(javaType.equals(edmType.getDefaultJavaType())){
				return edmType;
			}
		}
		
		for(EdmSimpleType edmType : ALL){
			for(Class<?> mappingType : edmType.getMappingJavaTypes()){
				if(mappingType.equals(javaType)){
					return edmType;
				}
			}
		}
		
		return null;
	}

	public static boolean hasMaxLengthFacet(EdmType type){
		return hasFacet(type, EdmSimpleTypeFacet.MaxLength);
	}
	
	public static boolean hasFixedLengthFacet(EdmType type){
		return hasFacet(type, EdmSimpleTypeFacet.FixedLength);
	}
	
	public static boolean hasPrecisionFacet(EdmType type){
		return hasFacet(type, EdmSimpleTypeFacet.Precision);
	}
	
	public static boolean hasScaleFacet(EdmType type){
		return hasFacet(type, EdmSimpleTypeFacet.Scale);
	}
	
	public static boolean hasFacet(EdmType type,EdmSimpleTypeFacet facet){
		if(type instanceof EdmSimpleType){
			return ((EdmSimpleType) type).getValueKind().hasFacet(facet);
		}
		return false;
	}

	private final String	            name;
	private final String	            fullQualifiedName;
	private final EdmSimpleTypeKind	    valueKind;
	private final Class<?>	            defaultJavaType;
	private final Enumerable<Class<?>>	mappingJavaTypes;
	
	private EdmSimpleType(EdmSimpleTypeKind kind,Class<?>... javaTypes){
		this.valueKind         = kind;
		this.name              = kind.toString();
		this.fullQualifiedName = "Edm." + name;
		
		if(javaTypes.length > 0){
			this.defaultJavaType = javaTypes[0];	
		}else{
			this.defaultJavaType = null;
		}
		
		this.mappingJavaTypes  = Enumerables.of(javaTypes);
	}	
	
	public String getName() {
    	return name;
    }

	public String getFullQualifiedName() {
    	return fullQualifiedName;
    }

	@Override
    public EdmTypeKind getTypeKind() {
	    return EdmTypeKind.Simple;
    }
	
	public EdmSimpleTypeKind getValueKind() {
    	return valueKind;
    }
	
	public Class<?> getDefaultJavaType() {
    	return defaultJavaType;
    }

	public Iterable<Class<?>> getMappingJavaTypes() {
    	return mappingJavaTypes;
    }
	
	public boolean isMappingType(Class<?> javaType){
		if(null == javaType){
			return false;
		}
		
		for(Class<?> mappingType : mappingJavaTypes){
			if(mappingType.equals(javaType)){
				return true;
			}
		}
		
		return false;
	}

	private static EdmSimpleType add(EdmSimpleTypeKind kind,Class<?>... javaTypes){
		EdmSimpleType type = new EdmSimpleType(kind,javaTypes);
		map.put(type.getValueKind(), type);
		return type;
	}

	@Override
    public String toString() {
		return getFullQualifiedName();
    }
}