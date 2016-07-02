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
package leap.lang.el;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class ElTypes {
	
	public static final int	UNRESOLVED	= -1;

	public static final int	OBJECT	    = 0;
	public static final int	COLLECTION	= 1;
	public static final int ENUM        = 2;
	
	public static final int	STRING	    = 10;
	
	public static final int	BOOLEAN     = 20;
	public static final int	W_BOOLEAN	= 21;
	
	public static final int	CHAR	    = 30;
	public static final int	W_CHAR	    = 31;
	
	public static final int	BYTE	    = 40;
	public static final int	W_BYTE	    = 41;
	
	public static final int	SHORT	    = 50;
	public static final int	INTEGER	    = 51;
	public static final int	LONG	    = 52;
	public static final int	W_SHORT	    = 53;
	public static final int	W_INTEGER	= 54;
	public static final int	W_LONG	    = 55;

	public static final int	FLOAT	    = 60;
	public static final int	DOUBLE	    = 61;
	public static final int	W_FLOAT	    = 62;
	public static final int	W_DOUBLE	= 63;
	public static final int	BIG_DECIMAL	= 70;
	public static final int	BIG_INTEGER	= 71;
	
	private static final Map<Class<?>, Integer> typeCodes= new HashMap<>(30, 0.5f);

	static {
		typeCodes.put(Integer.class, W_INTEGER);
		typeCodes.put(Double.class, W_DOUBLE);
		typeCodes.put(Boolean.class, W_BOOLEAN);
		typeCodes.put(String.class, STRING);
		typeCodes.put(Long.class, W_LONG);
		typeCodes.put(Short.class, W_SHORT);
		typeCodes.put(Float.class, W_FLOAT);
		typeCodes.put(Byte.class, W_BYTE);
		typeCodes.put(Character.class, W_CHAR);

		typeCodes.put(BigDecimal.class, BIG_DECIMAL);
		typeCodes.put(BigInteger.class, BIG_INTEGER);

		typeCodes.put(int.class, INTEGER);
		typeCodes.put(double.class, DOUBLE);
		typeCodes.put(boolean.class, BOOLEAN);
		typeCodes.put(long.class, LONG);
		typeCodes.put(short.class, SHORT);
		typeCodes.put(float.class, FLOAT);
		typeCodes.put(byte.class, BYTE);
		typeCodes.put(char.class, CHAR);
	}
	
	public static boolean isFloatOrDouble(int type){
		return type > 59 & type < 70;
	}
	
	public static boolean isShortIntOrLong(int type){
		return type > 49 && type < 60;
	}
	
	public static boolean isBoolean(int type){
		return type == W_BOOLEAN || type == BOOLEAN;
	}
	
	public static int resolveTypeByVal(Object v){
		if(null == v){
			return ElTypes.OBJECT;
		}
		return resolveType(v.getClass());
	}

	public static int resolveType(Class<?> cls) {
		if(null == cls){
			return UNRESOLVED;
		}
		
		Integer code = typeCodes.get(cls);
		if (code == null) {
			if(null != cls){
				if(Collection.class.isAssignableFrom(cls)){
					return COLLECTION;
				}
				if(cls.isEnum()){
					return ENUM;
				}
			}
			return OBJECT;
		}
		return code;
	}

}