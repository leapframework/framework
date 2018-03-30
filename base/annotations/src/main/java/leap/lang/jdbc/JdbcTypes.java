/*
 * Copyright 2013 the original author or authors.
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
package leap.lang.jdbc;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class JdbcTypes {
	
	private static final Map<Integer,JdbcType> typeCodeToJdbcTypeMappings = new HashMap<Integer, JdbcType>();
	private static final Map<String, JdbcType> typeNameToJdbcTypeMappings = new HashMap<String, JdbcType>();
	
	public static final int UNKNOWN_TYPE_CODE = Integer.MIN_VALUE;
	
	public static final String ARRAY_TYPE_NAME	       = "array";
	public static final String BIGINT_TYPE_NAME	       = "bigint";
	public static final String BINARY_TYPE_NAME	       = "binary";
	public static final String BIT_TYPE_NAME	       = "bit";
	public static final String BLOB_TYPE_NAME	       = "blob";
	public static final String BOOLEAN_TYPE_NAME	   = "boolean";
	public static final String CHAR_TYPE_NAME	       = "char";
	public static final String CLOB_TYPE_NAME	       = "clob";
	public static final String DATALINK_TYPE_NAME	   = "datalink";
	public static final String DATE_TYPE_NAME	       = "date";
	public static final String DECIMAL_TYPE_NAME	   = "decimal";
	public static final String DISTINCT_TYPE_NAME	   = "distinct";
	public static final String DOUBLE_TYPE_NAME	       = "double";
	public static final String FLOAT_TYPE_NAME	       = "float";
	public static final String INTEGER_TYPE_NAME	   = "integer";
	public static final String JAVA_OBJECT_TYPE_NAME   = "java_object";
	public static final String LONGVARBINARY_TYPE_NAME = "longvarbinary";
	public static final String LONGVARCHAR_TYPE_NAME   = "longvarchar";
	public static final String NULL_TYPE_NAME	       = "null";
	public static final String OTHER_TYPE_NAME	       = "other";
	public static final String NUMERIC_TYPE_NAME	   = "numeric";
	public static final String REAL_TYPE_NAME	       = "real";
	public static final String REF_TYPE_NAME	       = "ref";
	public static final String SMALLINT_TYPE_NAME	   = "smallint";
	public static final String STRUCT_TYPE_NAME	       = "struct";
	public static final String TIME_TYPE_NAME	       = "time";
	public static final String TIMESTAMP_TYPE_NAME	   = "timestamp";
	public static final String TINYINT_TYPE_NAME	   = "tinyint";
	public static final String VARBINARY_TYPE_NAME	   = "varbinary";
	public static final String VARCHAR_TYPE_NAME	   = "varchar";
	public static final String ROWID_TYPE_NAME	       = "rowid";
	public static final String NCHAR_TYPE_NAME	       = "nchar";
	public static final String NVARCHAR_TYPE_NAME	   = "nvarchar";
	public static final String LONGNVARCHAR_TYPE_NAME  = "longnvarchar";
	public static final String NCLOB_TYPE_NAME	       = "nclob";
	public static final String SQLXML_TYPE_NAME	       = "sqlxml";	

	static {
		//http://docs.oracle.com/javase/1.5.0/docs/guide/jdbc/getstart/mapping.html
		//note : jdbc's float mapping to java's double 
		
        register(java.sql.Types.BIGINT,        BIGINT_TYPE_NAME,        JdbcTypeKind.Numeric,   long.class, false, false, "long");
        register(java.sql.Types.BIT,           BIT_TYPE_NAME,           JdbcTypeKind.Numeric,   boolean.class,short.class, false, false);
        register(java.sql.Types.BOOLEAN,       BOOLEAN_TYPE_NAME,       JdbcTypeKind.Numeric,   boolean.class, false, false, "bool");
        register(java.sql.Types.DECIMAL,       DECIMAL_TYPE_NAME,       JdbcTypeKind.Numeric,   BigDecimal.class, false, true);
        register(java.sql.Types.DOUBLE,        DOUBLE_TYPE_NAME,        JdbcTypeKind.Numeric,   double.class,Double.class, false, false);
        register(java.sql.Types.FLOAT,         FLOAT_TYPE_NAME,         JdbcTypeKind.Numeric,   double.class,Double.class, false, false); 
        register(java.sql.Types.INTEGER,       INTEGER_TYPE_NAME,       JdbcTypeKind.Numeric,   int.class,Integer.class, false, false, "int");
        register(java.sql.Types.NUMERIC,       NUMERIC_TYPE_NAME,       JdbcTypeKind.Numeric,   BigDecimal.class, false, true, "number");
        register(java.sql.Types.REAL,          REAL_TYPE_NAME,          JdbcTypeKind.Numeric,   float.class,Float.class, false, false);
        register(java.sql.Types.SMALLINT,      SMALLINT_TYPE_NAME,      JdbcTypeKind.Numeric,   short.class,Short.class, false, false, "short");
        register(java.sql.Types.TINYINT,       TINYINT_TYPE_NAME,       JdbcTypeKind.Numeric,   short.class,Short.class, false, false);
        
        register(java.sql.Types.CHAR,          CHAR_TYPE_NAME,          JdbcTypeKind.Text,      String.class, true, false);
        register(java.sql.Types.CLOB,          CLOB_TYPE_NAME,          JdbcTypeKind.Text,      String.class,Clob.class, false, false);
        register(java.sql.Types.LONGVARCHAR,   LONGVARCHAR_TYPE_NAME,   JdbcTypeKind.Text,      String.class, true, false, "text");
        register(java.sql.Types.VARCHAR,       VARCHAR_TYPE_NAME,       JdbcTypeKind.Text,      String.class, true, false, "string");
        register(java.sql.Types.NCHAR,		   NCHAR_TYPE_NAME,		    JdbcTypeKind.Text,	    String.class, true, false);
        register(java.sql.Types.NVARCHAR,	   NVARCHAR_TYPE_NAME,	    JdbcTypeKind.Text,	    String.class, true, false);
        register(java.sql.Types.LONGNVARCHAR,  LONGNVARCHAR_TYPE_NAME,  JdbcTypeKind.Text,	    String.class, true, false);
        register(java.sql.Types.NCLOB,		   NCLOB_TYPE_NAME,		    JdbcTypeKind.Text,	    String.class,Clob.class, false, false);
        register(java.sql.Types.SQLXML,		   SQLXML_TYPE_NAME,		JdbcTypeKind.Text,	    String.class, false, false);	
        
        register(java.sql.Types.DATE,          DATE_TYPE_NAME,          JdbcTypeKind.Temporal,  Date.class, false, false);
        register(java.sql.Types.TIME,          TIME_TYPE_NAME,          JdbcTypeKind.Temporal,  Time.class, false, false);
        register(java.sql.Types.TIMESTAMP,     TIMESTAMP_TYPE_NAME,     JdbcTypeKind.Temporal,  Timestamp.class, false, false, "datetime");
        
        register(java.sql.Types.BINARY,        BINARY_TYPE_NAME,        JdbcTypeKind.Binary,    byte[].class, true, false, "bytes");
        register(java.sql.Types.BLOB,          BLOB_TYPE_NAME,          JdbcTypeKind.Binary,    byte[].class,Blob.class, false, false);
        register(java.sql.Types.LONGVARBINARY, LONGVARBINARY_TYPE_NAME, JdbcTypeKind.Binary,    byte[].class, true, false);
        register(java.sql.Types.VARBINARY,     VARBINARY_TYPE_NAME,     JdbcTypeKind.Binary,    byte[].class, true, false);  

        register(java.sql.Types.ARRAY,         ARRAY_TYPE_NAME,         JdbcTypeKind.Special,   Array.class, false, false);
        register(java.sql.Types.DATALINK,      DATALINK_TYPE_NAME,      JdbcTypeKind.Special,   null, false, false);
        register(java.sql.Types.DISTINCT,      DISTINCT_TYPE_NAME,      JdbcTypeKind.Special,   null, false, false);
        register(java.sql.Types.JAVA_OBJECT,   JAVA_OBJECT_TYPE_NAME,   JdbcTypeKind.Special,   null, false, false);
        register(java.sql.Types.NULL,          NULL_TYPE_NAME,          JdbcTypeKind.Special,   null, false, false);
        register(java.sql.Types.OTHER,         OTHER_TYPE_NAME,         JdbcTypeKind.Special,   null, false, false);
        register(java.sql.Types.REF,           REF_TYPE_NAME,           JdbcTypeKind.Special,   Ref.class, false, false);
        register(java.sql.Types.STRUCT,        STRUCT_TYPE_NAME,        JdbcTypeKind.Special,   Struct.class, false, false);
	}
	
	private static void register(int typeCode,String typeName,JdbcTypeKind kind,Class<?> defaultJavaType, 
							     boolean supportsLength, boolean supportsPrecisionAndScale){
		register(typeCode, typeName, kind, defaultJavaType, defaultJavaType, supportsLength, supportsPrecisionAndScale, null);
	}

    private static void register(int typeCode,String typeName,JdbcTypeKind kind,Class<?> defaultJavaType,
                                 boolean supportsLength, boolean supportsPrecisionAndScale, String... aliases){
        register(typeCode, typeName, kind, defaultJavaType, defaultJavaType, supportsLength, supportsPrecisionAndScale, aliases);
    }
	
	private static void register(int typeCode,String typeName,JdbcTypeKind kind,Class<?> defaultReadType,Class<?> defaultSaveType,
							     boolean supportsLength,boolean supportsPrecisionAndScale){
        register(typeCode, typeName, kind, defaultReadType, defaultSaveType, supportsLength, supportsPrecisionAndScale, null);
	}

    private static void register(int typeCode,String typeName,JdbcTypeKind kind,Class<?> defaultReadType,Class<?> defaultSaveType,
                                 boolean supportsLength,boolean supportsPrecisionAndScale, String... aliases){
        JdbcType type = new JdbcType(typeCode, typeName, kind , defaultReadType, defaultSaveType,supportsLength,supportsPrecisionAndScale);

        if(typeCodeToJdbcTypeMappings.containsKey(typeCode)) {
            throw new IllegalStateException("Type code '" + typeCode + "' already registered!");
        }

        if(typeNameToJdbcTypeMappings.containsKey(typeName)) {
            throw new IllegalStateException("Type name '" + typeName + "' already registered!");
        }

        typeCodeToJdbcTypeMappings.put(typeCode, type);
        typeNameToJdbcTypeMappings.put(typeName, type);

        if(null != aliases) {
            for(String alias : aliases) {
                if(typeNameToJdbcTypeMappings.containsKey(alias)) {
                    throw new IllegalStateException("Type name '" + alias + "' already registered!");
                }
                typeNameToJdbcTypeMappings.put(alias, type);
            }
        }
    }
	
	public static Iterable<JdbcType> all(){
		return typeCodeToJdbcTypeMappings.values();
	}
	
	/**
	 * returns the {@link JdbcType} object which 'code' property equals to the given type code.
	 * 
	 * @throws NoSuchElementException if no {@link JdbcType} object found for the given type code.
	 */
	public static JdbcType forTypeCode(int typeCode) throws NoSuchElementException {
		JdbcType type = typeCodeToJdbcTypeMappings.get(typeCode);
		if(null == type){
			throw new NoSuchElementException("no jdbc type found for code '" + typeCode + "'");
		}
		return type;
	}
	
	/**
	 * returns the {@link JdbcType} object which 'code' property equals to the given type code.
	 * 
	 * <p>returns <code>null</code> if no {@link JdbcType} object found for the given type code.
	 */
	public static JdbcType tryForTypeCode(int typeCode){
		return typeCodeToJdbcTypeMappings.get(typeCode);
	}
	
	/**
	 * returns the {@link JdbcType} object which name property equals to the given type name (ignore case).
	 * 
	 * @throws IllegalArgumentException if the given type name is null or empty.
	 * @throws NoSuchElementException if no {@link JdbcType} object found for the given type name.
	 */
	public static JdbcType forTypeName(String typeName) throws IllegalArgumentException, NoSuchElementException {
		JdbcType type = typeNameToJdbcTypeMappings.get(typeName.toLowerCase());
		if(null == type){
			throw new NoSuchElementException("no jdbc type found for name '" + typeName + "'");
		}
		return type;
	}
	
	/**
	 * returns the {@link JdbcType} object which name property equals to the given type name (ignore case).
	 * 
	 * <p>
	 * returns <code>null</code> if not {@link JdbcType} object found for the given type name.
	 * 
	 * <p>
	 * returns <code>null</code> if the given type name is null or empty.
	 */
	public static JdbcType tryForTypeName(String typeName) {
		return null == typeName ? null : typeNameToJdbcTypeMappings.get(typeName.toLowerCase());
	}
	
	protected JdbcTypes(){
		
	}
}
