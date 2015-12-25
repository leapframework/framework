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
package leap.db.model;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbColumnTypes {

    public static final int BOOLEAN       = Types.BOOLEAN;
    public static final int BIT           = Types.BIT;
    public static final int SMALLINT      = Types.SMALLINT;
    public static final int INTEGER       = Types.INTEGER;
    public static final int BITINT        = Types.BIGINT;
    public static final int FLOAT         = Types.FLOAT;
    public static final int DOUBLE        = Types.DOUBLE;
    public static final int DECIMAL       = Types.DECIMAL;
    public static final int NUMERIC       = Types.NUMERIC;
    public static final int CHAR          = Types.CHAR;
    public static final int VARCHAR       = Types.VARCHAR;
    public static final int LONGVARCHAR   = Types.LONGVARCHAR;
    public static final int BINARY        = Types.BINARY;
    public static final int VARBINARY     = Types.VARBINARY;
    public static final int LONGVARBINARY = Types.LONGVARBINARY;
    public static final int DATE          = Types.DATE;
    public static final int TIME          = Types.TIME;
    public static final int TIMESTAMP     = Types.TIMESTAMP;
    public static final int BLOB          = Types.BLOB;
    public static final int CLOB          = Types.CLOB;
    
    private static final int[] SUPPORTED_TYPES = new int[]{
        BOOLEAN,BIT,SMALLINT,INTEGER,BITINT,FLOAT,DOUBLE,DECIMAL,NUMERIC,CHAR,VARCHAR,LONGVARCHAR,BINARY,VARBINARY,LONGVARBINARY,DATE,TIME,TIMESTAMP,BLOB,CLOB
    };
    
    public static int[] supportedTypes(){
        return SUPPORTED_TYPES;
    }
    
    public static boolean isSupportedType(int typeCode){
        for(int supported : SUPPORTED_TYPES){
            if(supported == typeCode){
                return true;
            }
        }
        return false;
    }
    
    protected Map<Integer, DbColumnType>       defaultTypes  = new HashMap<Integer, DbColumnType>();
    protected Map<Integer, List<DbColumnType>> lengthedTypes = new HashMap<>();
    
    public DbColumnType get(int typeCode,int length){
        List<DbColumnType> types = lengthedTypes.get(typeCode);
        
        if (types != null && types.size() > 0) {
            for (DbColumnType ct : types) {
                if(ct.matchesLength(length)) {
                    return ct;
                }
            }
        }        
        
        return defaultTypes.get(typeCode);
    }
    
    public void add(int typeCode,String typeDef){
        defaultTypes.put(typeCode, new DbColumnType(typeCode, typeDef));
    }
    
    public void add(int typeCode,String typeDef,Integer minLength){
        add(typeCode,typeDef,minLength,null,typeCode);
    }
	
    public void add(int typeCode,String typeDef,Integer minLength,Integer maxLength){
        add(typeCode,typeDef,minLength,maxLength,typeCode);
    }
    
    public void add(int typeCode,String typeDef,Integer minLength,Integer maxLength,int nativeTypeCode){
        List<DbColumnType> types = lengthedTypes.get(typeCode);
        
        if (types == null) {
            types = new ArrayList<DbColumnType>();
            lengthedTypes.put(typeCode,types);
        }
        
        DbColumnType type = new DbColumnType(typeCode,typeDef,minLength,maxLength,nativeTypeCode);
        
        types.add(type);
        
        if(!defaultTypes.containsKey(typeCode)){
            defaultTypes.put(typeCode, type);
        }
    }
}
