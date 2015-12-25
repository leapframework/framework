/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except val compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to val writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.lang.el.spel.ast;

import static leap.lang.el.ElTypes.BIG_DECIMAL;
import static leap.lang.el.ElTypes.BIG_INTEGER;
import static leap.lang.el.ElTypes.BOOLEAN;
import static leap.lang.el.ElTypes.BYTE;
import static leap.lang.el.ElTypes.CHAR;
import static leap.lang.el.ElTypes.DOUBLE;
import static leap.lang.el.ElTypes.FLOAT;
import static leap.lang.el.ElTypes.INTEGER;
import static leap.lang.el.ElTypes.LONG;
import static leap.lang.el.ElTypes.SHORT;
import static leap.lang.el.ElTypes.STRING;
import static leap.lang.el.ElTypes.W_BOOLEAN;
import static leap.lang.el.ElTypes.W_BYTE;
import static leap.lang.el.ElTypes.W_CHAR;
import static leap.lang.el.ElTypes.W_DOUBLE;
import static leap.lang.el.ElTypes.W_FLOAT;
import static leap.lang.el.ElTypes.W_INTEGER;
import static leap.lang.el.ElTypes.W_LONG;
import static leap.lang.el.ElTypes.W_SHORT;

import java.math.BigDecimal;
import java.math.BigInteger;

import leap.lang.el.ElEvalContext;

abstract class Coerce {
	public static final Long       LG_ONE  = new Long(1);
	public static final Long       LG_ZERO = new Long(0);
	public static final Double     DB_ONE  = new Double(1);
	public static final Double     DB_ZERO = new Double(0);
	public static final BigInteger BI_ONE  = new BigInteger("1");
	public static final BigInteger BI_ZERO = new BigInteger("0");
	public static final BigDecimal BD_ZERO = new BigDecimal("0");
	
	public static BigDecimal toBigDecimal(ElEvalContext ctx, int type,Object val){
		if(type == BIG_DECIMAL){
			return (BigDecimal)val;
		}
		
        if (val == null || "".equals(val)) {
            return BD_ZERO;
        }
        
		switch (type) {
			case BIG_DECIMAL:
				return new BigDecimal(((BigDecimal) val).doubleValue());
			case BIG_INTEGER:
				return new BigDecimal((BigInteger) val);
			case INTEGER:
			case W_INTEGER:
				return new BigDecimal((Integer) val);
			case LONG:
			case W_LONG:
				return new BigDecimal((Long) val);
			case STRING:
				return new BigDecimal((String) val);
			case FLOAT:
			case W_FLOAT:
				return new BigDecimal((Float) val);
			case DOUBLE:
			case W_DOUBLE:
				return new BigDecimal((Double) val);
			case SHORT:
			case W_SHORT:
				return new BigDecimal((Short) val);
			case CHAR:
			case W_CHAR:
				return new BigDecimal((Character) val);
			case BOOLEAN:
			case W_BOOLEAN:
				return new BigDecimal(((Boolean) val) ? 1 : 0);
			case W_BYTE:
			case BYTE:
				return new BigDecimal(((Byte) val).intValue());
		}
        
        return ctx.convert(val, BigDecimal.class);
	}
	
	public static BigInteger toBigInteger(ElEvalContext ctx, int type,Object val){
		if(type == BIG_INTEGER){
			return (BigInteger)val;
		}
		
        if (val == null || "".equals(val)) {
            return BI_ZERO;
        }
        
        if(type > 49){
        	return BigInteger.valueOf(((Number)val).longValue());
        }
        
		switch (type) {
			case CHAR:
			case W_CHAR:
				return BigInteger.valueOf((Character)val);
			case BOOLEAN:
			case W_BOOLEAN:
				return (Boolean)val ? BI_ONE : BI_ZERO;
			case W_BYTE:
			case BYTE:
				return BigInteger.valueOf((Byte)val);
		}
        
        return ctx.convert(val, BigInteger.class);
	}
	
	public static Double toDouble(ElEvalContext ctx, int type,Object val){
		if(type == W_DOUBLE || type == DOUBLE){
			return (Double)val;
		}
		
        if (val == null || "".equals(val)) {
            return DB_ZERO;
        }
        
		switch (type) {
			case BIG_DECIMAL:
				return ((Number) val).doubleValue();
			case BIG_INTEGER:
				return ((Number) val).doubleValue();
			case INTEGER:
			case W_INTEGER:
				return ((Number) val).doubleValue();
			case LONG:
			case W_LONG:
				return ((Number) val).doubleValue();
			case STRING:
				return Double.parseDouble((String) val);
			case FLOAT:
			case W_FLOAT:
				return ((Number) val).doubleValue();
			case DOUBLE:
			case W_DOUBLE:
				return (Double) val;
			case SHORT:
			case W_SHORT:
				return ((Number) val).doubleValue();
			case CHAR:
			case W_CHAR:
				return Double.parseDouble(String.valueOf((Character) val));
			case BOOLEAN:
			case W_BOOLEAN:
				return ((Boolean) val) ? DB_ONE : DB_ZERO;
			case W_BYTE:
			case BYTE:
				return ((Byte) val).doubleValue();
		}
        
        return ctx.convert(val, Double.class);
	}
	
	public static Long toLong(ElEvalContext ctx, int type,Object val){
		if(type == W_LONG || type == LONG){
			return (Long)val;
		}
        
		if (val == null || "".equals(val)) {
            return LG_ZERO;
        }
        
		if(type > 49){
			return ((Number)val).longValue();
		}
		
		switch (type) {
			case CHAR:
			case W_CHAR:
				return Long.valueOf((Character)val);
			case BOOLEAN:
			case W_BOOLEAN:
				return (Boolean)val ? LG_ONE : LG_ZERO;
			case W_BYTE:
			case BYTE:
				return Long.valueOf((Byte)val);
		}
        
        return ctx.convert(val, Long.class);
	}
	
	public static Boolean toBoolean(ElEvalContext ctx, int type,Object val){
		if(type == W_BOOLEAN || type == BOOLEAN){
			return (Boolean)val;
		}
		
		if(type > 49){
			return ((Number)val).doubleValue() != 0.0d;
		}
		
		return ctx.convert(val, Boolean.class);
	}
	
	public static String toString(ElEvalContext ctx, int type, Object val){
		if(type == STRING){
			return (String)val;
		}
		return ctx.toString(val);
	}
}
