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
package leap.lang.el.spel.ast;

import static leap.lang.el.ElTypes.*;

import java.math.BigDecimal;
import java.math.BigInteger;

import leap.lang.el.ElEvalContext;
import leap.lang.el.ElException;
import leap.lang.el.ElTypes;


class Ops {
	
	/**
	 * + operator : jsr341 spec
	 */
	static Object add(ElEvalContext ctx, int ltype,Object lval,int rtype,Object rval) {
		//■ If A and B are null, return (Long)0
		if(null == lval && null == rval){
			return Coerce.LG_ZERO;
		}
		
		if(ltype == -1){
			ltype = resolveTypeByVal(lval);
		}
		if(rtype == -1){
			rtype = resolveTypeByVal(rval);
		}
		
		//numbers
		if(ltype > 29 || rtype > 29){
			//If A or B is a BigDecimal, coerce both to BigDecimal and then return A.add(B)
			if(ltype == BIG_DECIMAL || rtype == BIG_DECIMAL){
				return Coerce.toBigDecimal(ctx, ltype, lval).add(Coerce.toBigDecimal(ctx, rtype, rval));
			}
			
			//If A or B is a Float, Double, or String containing ., e, or E:
			//If A or B is BigInteger, coerce both A and B to BigDecimal and apply operator.
			//Otherwise, coerce both A and B to Double and apply operator
			if(isFloatOrDoubleOrDotEe(ltype,lval) || isFloatOrDoubleOrDotEe(rtype, rval)){
				if(ltype == BIG_INTEGER || rtype == BIG_INTEGER){
					return Coerce.toBigDecimal(ctx, ltype, lval).add(Coerce.toBigDecimal(ctx, rtype, rval));
				}else{
					return Coerce.toDouble(ctx, ltype, lval) + (Coerce.toDouble(ctx, rtype, rval));
				}
			}
			
			//If A or B is BigInteger, coerce both to BigInteger and then return A.add(B)
			if(ltype == BIG_INTEGER || rtype == BIG_INTEGER){
				return Coerce.toBigInteger(ctx, ltype, lval).add(Coerce.toBigInteger(ctx, rtype, rval));
			}
			
			return Coerce.toLong(ctx, ltype, lval) + Coerce.toLong(ctx, rtype, rval);
		}
		
		//String add (not supported by jsr341)
		return ctx.toString(lval) + ctx.toString(rval); 
	}
	
	/**
	 * - operator : jsr341 spec
	 */
	static Object sub(ElEvalContext ctx, int ltype,Object lval,int rtype,Object rval) {
		//■ If A and B are null, return (Long)0
		if(null == lval && null == rval){
			return Coerce.LG_ZERO;
		}
		
		if(ltype == -1){
			ltype = resolveTypeByVal(lval);
		}
		if(rtype == -1){
			rtype = resolveTypeByVal(rval);
		}
		
		//If A or B is a BigDecimal, coerce both to BigDecimal and then return A.subtract(B)
		if(ltype == BIG_DECIMAL || rtype == BIG_DECIMAL){
			return Coerce.toBigDecimal(ctx, ltype, lval).subtract(Coerce.toBigDecimal(ctx, rtype, rval));
		}
		
		//If A or B is a Float, Double, or String containing ., e, or E:
		//If A or B is BigInteger, coerce both A and B to BigDecimal and apply operator.
		//Otherwise, coerce both A and B to Double and apply operator
		if(isFloatOrDoubleOrDotEe(ltype,lval) || isFloatOrDoubleOrDotEe(rtype, rval)){
			if(ltype == BIG_INTEGER || rtype == BIG_INTEGER){
				return Coerce.toBigDecimal(ctx, ltype, lval).subtract(Coerce.toBigDecimal(ctx, rtype, rval));
			}else{
				return Coerce.toDouble(ctx, ltype, lval) - (Coerce.toDouble(ctx, rtype, rval));
			}
		}
		
		//If A or B is BigInteger, coerce both to BigInteger and then return A.subtract(B)
		if(ltype == BIG_INTEGER || rtype == BIG_INTEGER){
			return Coerce.toBigInteger(ctx, ltype, lval).subtract(Coerce.toBigInteger(ctx, rtype, rval));
		}
		
		//Otherwise coerce both A and B to Long and apply operator
		return Coerce.toLong(ctx, ltype, lval) - Coerce.toLong(ctx, rtype, rval);
	}
	
	/**
	 * * operator : jsr341 spec
	 */
	static Object mul(ElEvalContext ctx, int ltype,Object lval,int rtype,Object rval) {
		//■ If A and B are null, return (Long)0
		if(null == lval && null == rval){
			return Coerce.LG_ZERO;
		}
		
		if(ltype == -1){
			ltype = resolveTypeByVal(lval);
		}
		if(rtype == -1){
			rtype = resolveTypeByVal(rval);
		}
		
		//If A or B is a BigDecimal, coerce both to BigDecimal and then return A.multiply(B)
		if(ltype == BIG_DECIMAL || rtype == BIG_DECIMAL){
			return Coerce.toBigDecimal(ctx, ltype, lval).multiply(Coerce.toBigDecimal(ctx, rtype, rval));
		}
		
		//If A or B is a Float, Double, or String containing ., e, or E:
		//If A or B is BigInteger, coerce both A and B to BigDecimal and apply operator.
		//Otherwise, coerce both A and B to Double and apply operator
		if(isFloatOrDoubleOrDotEe(ltype,lval) || isFloatOrDoubleOrDotEe(rtype, rval)){
			if(ltype == BIG_INTEGER || rtype == BIG_INTEGER){
				return Coerce.toBigDecimal(ctx, ltype, lval).multiply(Coerce.toBigDecimal(ctx, rtype, rval));
			}else{
				return Coerce.toDouble(ctx, ltype, lval) * (Coerce.toDouble(ctx, rtype, rval));
			}
		}
		
		//If A or B is BigInteger, coerce both to BigInteger and then return A.multiply(B)
		if(ltype == BIG_INTEGER || rtype == BIG_INTEGER){
			return Coerce.toBigInteger(ctx, ltype, lval).multiply(Coerce.toBigInteger(ctx, rtype, rval));
		}
		
		//Otherwise coerce both A and B to Long and apply operator
		return Coerce.toLong(ctx, ltype, lval) * Coerce.toLong(ctx, rtype, rval);
	}
	
	/**
	 * / operator : jsr341 spec
	 */
	static Object div(ElEvalContext ctx, int ltype,Object lval,int rtype,Object rval) {
		//■ If A and B are null, return (Long)0
		if(null == lval && null == rval){
			return Coerce.LG_ZERO;
		}
		
		if(ltype == -1){
			ltype = resolveTypeByVal(lval);
		}
		if(rtype == -1){
			rtype = resolveTypeByVal(rval);
		}
		
		//If A or B is a BigDecimal or a BigInteger, coerce both to BigDecimal and return A.divide(B, BigDecimal.ROUND_HALF_UP)
		if(ltype > 69 || rtype > 69 ){
			return Coerce.toBigDecimal(ctx, ltype, lval).divide(Coerce.toBigDecimal(ctx, rtype, rval),BigDecimal.ROUND_HALF_UP);
		}
		
		//Otherwise, coerce both A and B to Double and apply operator
		return Coerce.toDouble(ctx, ltype, lval) / Coerce.toDouble(ctx, rtype, rval);
	}
	
	/**
	 * % operator : jsr341 spec
	 */
	static Object mod(ElEvalContext ctx, int ltype,Object lval,int rtype,Object rval) {
		//■ If A and B are null, return (Long)0
		if(null == lval && null == rval){
			return Coerce.LG_ZERO;
		}
		
		if(ltype == -1){
			ltype = resolveTypeByVal(lval);
		}
		if(rtype == -1){
			rtype = resolveTypeByVal(rval);
		}
		
		//If A or B is a BigDecimal, Float, Double, or String containing ., e, or E, 
		//coerce both A and B to Double and apply operator
		if((ltype == BIG_DECIMAL || isFloatOrDoubleOrDotEe(ltype, lval)) ||  
		   (rtype == BIG_DECIMAL || isFloatOrDoubleOrDotEe(rtype, rval))) {
			return Coerce.toDouble(ctx, ltype, lval) % Coerce.toDouble(ctx, rtype, rval);
		}
		
		//If A or B is a BigInteger, coerce both to BigInteger and return A.remainder(B).
		if(ltype == BIG_INTEGER || rtype == BIG_INTEGER){
			return Coerce.toBigInteger(ctx, ltype, lval).remainder(Coerce.toBigInteger(ctx, rtype, rval));
		}
		
		//Otherwise coerce both A and B to Long and apply operator
		return Coerce.toLong(ctx, ltype, lval) % Coerce.toLong(ctx, rtype, rval);
	}
	
	/**
	 * == operator : jsr341 spec
	 */
	static Object eq(ElEvalContext ctx, int ltype,Object lval,int rtype,Object rval) {
		if(ltype == -1){
			ltype = resolveTypeByVal(lval);
		}
		if(rtype == -1){
			rtype = resolveTypeByVal(rval);
		}
		
		//If A or B is Boolean coerce both A and B to Boolean, apply operator
		if(isBoolean(ltype) || isBoolean(rtype)){
			return Coerce.toBoolean(ctx, ltype, lval) == Coerce.toBoolean(ctx, rtype, rval);
		}
		
		//If A or B is an enum, coerce both A and B to enum, apply operator
		if(ltype == ENUM){
			if(rtype == ENUM){
				return lval.equals(rval);
			}else{
				return lval.equals(ctx.convert(rval, lval.getClass()));
			}
		}else if(rtype == ENUM){
			if(ltype == ENUM){
				return rval.equals(lval);
			}else{
				return rval.equals(ctx.convert(lval, rval.getClass()));
			}
		}
		
		//If A or B is String coerce both A and B to String, compare lexically
		if(ltype == STRING || rtype == STRING){
			return Coerce.toString(ctx, ltype, lval).equals(Coerce.toString(ctx, rtype, rval));
		}

		//If A or B is Byte, Short, Character, Integer, or Long coerce both A and B to Long, apply operator
		if(ltype > 29 || rtype > 29){
			//if A or B is BigDecimal, coerce both A and B to BigDecimal and then: return A.equals(B)
			if(ltype == BIG_DECIMAL || rtype == BIG_DECIMAL){
				return Coerce.toBigDecimal(ctx, ltype, lval).equals(Coerce.toBigDecimal(ctx, rtype, rval));
			}
			
			//If A or B is Float or Double coerce both A and B to Double, apply operator
			if(ElTypes.isFloatOrDouble(ltype) || ElTypes.isFloatOrDouble(rtype)){
				return Coerce.toDouble(ctx, ltype, lval) == Coerce.toDouble(ctx, rtype, rval);
			}
			
			//If A or B is BigInteger, coerce both A and B to BigInteger and then: return A.equals(B)
			if(ltype == BIG_INTEGER || rtype == BIG_INTEGER){
				return Coerce.toBigInteger(ctx, ltype, lval).equals(Coerce.toBigInteger(ctx, rtype, rval));
			}
			
			return Coerce.toLong(ctx, ltype, lval) == Coerce.toLong(ctx, rtype, rval);
		}
		
		//Otherwise, apply operator to result of A.equals(B)	
		return lval.equals(rval);
	}
	
	/**
	 * >= operator : jsr341 spec
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
    static Object ge(ElEvalContext ctx, int ltype,Object lval,int rtype,Object rval) {
		if(ltype == -1){
			ltype = resolveTypeByVal(lval);
		}
		if(rtype == -1){
			rtype = resolveTypeByVal(rval);
		}

		//If A or B is BigDecimal, coerce both A and B to BigDecimal and use the return value of A.compareTo(B)
		if(ltype == BIG_DECIMAL || rtype == BIG_DECIMAL){
			return Coerce.toBigDecimal(ctx, ltype, lval).compareTo(Coerce.toBigDecimal(ctx, rtype, rval)) >= 0;
		}
		
		//If A or B is Float or Double coerce both A and B to Double, apply operator
		if(ElTypes.isFloatOrDouble(ltype) || ElTypes.isFloatOrDouble(rtype)){
			return Coerce.toDouble(ctx, ltype, lval) >= Coerce.toDouble(ctx, rtype, rval);
		}
		
		//If A or B is BigInteger, coerce both A and B to BigInteger and use the return value of A.compareTo(B).
		if(ltype == BIG_INTEGER || rtype == BIG_INTEGER){
			return Coerce.toBigInteger(ctx, ltype, lval).compareTo(Coerce.toBigInteger(ctx, rtype, rval)) >= 0;
		}
		
		//If A or B is Byte, Short, Character, Integer, or Long coerce both A and B to Long, apply operator
		if(ltype > 29 || rtype > 29){
			return Coerce.toLong(ctx, ltype, lval) >= Coerce.toLong(ctx, rtype, rval);
		}
		
		//If A or B is String coerce both A and B to String, compare lexically
		if(ltype == STRING || rtype == STRING){
			return Coerce.toString(ctx, ltype, lval).compareTo(Coerce.toString(ctx, rtype, rval)) >= 0;
		}
		
		//If A is Comparable, then: use result of A.compareTo(B)
		if(lval instanceof Comparable){
			return ((Comparable) lval).compareTo(rval) >= 0;
		}
		
		//If B is Comparable, then: use result of B.compareTo(A)
		if(rval instanceof Comparable){
			return ((Comparable) rval).compareTo(lval) < 0;
		}
		
		throw new ElException(ctx.getMessage("el.errors.valuesNotComparable", lval, rval));
	}
	
	/**
	 * > operator : jsr341 spec
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
    static Object gt(ElEvalContext ctx, int ltype,Object lval,int rtype,Object rval) {
		if(ltype == -1){
			ltype = resolveTypeByVal(lval);
		}
		if(rtype == -1){
			rtype = resolveTypeByVal(rval);
		}

		//If A or B is BigDecimal, coerce both A and B to BigDecimal and use the return value of A.compareTo(B)
		if(ltype == BIG_DECIMAL || rtype == BIG_DECIMAL){
			return Coerce.toBigDecimal(ctx, ltype, lval).compareTo(Coerce.toBigDecimal(ctx, rtype, rval)) > 0;
		}
		
		//If A or B is Float or Double coerce both A and B to Double, apply operator
		if(ElTypes.isFloatOrDouble(ltype) || ElTypes.isFloatOrDouble(rtype)){
			return Coerce.toDouble(ctx, ltype, lval) > Coerce.toDouble(ctx, rtype, rval);
		}
		
		//If A or B is BigInteger, coerce both A and B to BigInteger and use the return value of A.compareTo(B).
		if(ltype == BIG_INTEGER || rtype == BIG_INTEGER){
			return Coerce.toBigInteger(ctx, ltype, lval).compareTo(Coerce.toBigInteger(ctx, rtype, rval)) > 0;
		}
		
		//If A or B is Byte, Short, Character, Integer, or Long coerce both A and B to Long, apply operator
		if(ltype > 29 || rtype > 29){
			return Coerce.toLong(ctx, ltype, lval) > Coerce.toLong(ctx, rtype, rval);
		}
		
		//If A or B is String coerce both A and B to String, compare lexically
		if(ltype == STRING || rtype == STRING){
			return Coerce.toString(ctx, ltype, lval).compareTo(Coerce.toString(ctx, rtype, rval)) > 0;
		}
		
		//If A is Comparable, then: use result of A.compareTo(B)
		if(lval instanceof Comparable){
			return ((Comparable) lval).compareTo(rval) > 0;
		}
		
		//If B is Comparable, then: use result of B.compareTo(A)
		if(rval instanceof Comparable){
			return ((Comparable) rval).compareTo(lval) <= 0;
		}
		
		throw new ElException(ctx.getMessage("el.errors.valuesNotComparable", lval, rval));
	}
	
	/**
	 * < operator : jsr341 spec
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
    static Object lt(ElEvalContext ctx, int ltype,Object lval,int rtype,Object rval) {
		if(ltype == -1){
			ltype = resolveTypeByVal(lval);
		}
		if(rtype == -1){
			rtype = resolveTypeByVal(rval);
		}

		//If A or B is BigDecimal, coerce both A and B to BigDecimal and use the return value of A.compareTo(B)
		if(ltype == BIG_DECIMAL || rtype == BIG_DECIMAL){
			return Coerce.toBigDecimal(ctx, ltype, lval).compareTo(Coerce.toBigDecimal(ctx, rtype, rval)) < 0;
		}
		
		//If A or B is Float or Double coerce both A and B to Double, apply operator
		if(ElTypes.isFloatOrDouble(ltype) || ElTypes.isFloatOrDouble(rtype)){
			return Coerce.toDouble(ctx, ltype, lval) < Coerce.toDouble(ctx, rtype, rval);
		}
		
		//If A or B is BigInteger, coerce both A and B to BigInteger and use the return value of A.compareTo(B).
		if(ltype == BIG_INTEGER || rtype == BIG_INTEGER){
			return Coerce.toBigInteger(ctx, ltype, lval).compareTo(Coerce.toBigInteger(ctx, rtype, rval)) < 0;
		}
		
		//If A or B is Byte, Short, Character, Integer, or Long coerce both A and B to Long, apply operator
		if(ltype > 29 || rtype > 29){
			return Coerce.toLong(ctx, ltype, lval) < Coerce.toLong(ctx, rtype, rval);
		}
		
		//If A or B is String coerce both A and B to String, compare lexically
		if(ltype == STRING || rtype == STRING){
			return Coerce.toString(ctx, ltype, lval).compareTo(Coerce.toString(ctx, rtype, rval)) < 0;
		}
		
		//If A is Comparable, then: use result of A.compareTo(B)
		if(lval instanceof Comparable){
			return ((Comparable) lval).compareTo(rval) < 0;
		}
		
		//If B is Comparable, then: use result of B.compareTo(A)
		if(rval instanceof Comparable){
			return ((Comparable) rval).compareTo(lval) >= 0;
		}
		
		throw new ElException(ctx.getMessage("el.errors.valuesNotComparable", lval, rval));
	}
	
	/**
	 * <= operator : jsr341 spec
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
    static Object le(ElEvalContext ctx, int ltype,Object lval,int rtype,Object rval) {
		if(ltype == -1){
			ltype = resolveTypeByVal(lval);
		}
		if(rtype == -1){
			rtype = resolveTypeByVal(rval);
		}

		//If A or B is BigDecimal, coerce both A and B to BigDecimal and use the return value of A.compareTo(B)
		if(ltype == BIG_DECIMAL || rtype == BIG_DECIMAL){
			return Coerce.toBigDecimal(ctx, ltype, lval).compareTo(Coerce.toBigDecimal(ctx, rtype, rval)) <= 0;
		}
		
		//If A or B is Float or Double coerce both A and B to Double, apply operator
		if(ElTypes.isFloatOrDouble(ltype) || ElTypes.isFloatOrDouble(rtype)){
			return Coerce.toDouble(ctx, ltype, lval) <= Coerce.toDouble(ctx, rtype, rval);
		}
		
		//If A or B is BigInteger, coerce both A and B to BigInteger and use the return value of A.compareTo(B).
		if(ltype == BIG_INTEGER || rtype == BIG_INTEGER){
			return Coerce.toBigInteger(ctx, ltype, lval).compareTo(Coerce.toBigInteger(ctx, rtype, rval)) <= 0;
		}
		
		//If A or B is Byte, Short, Character, Integer, or Long coerce both A and B to Long, apply operator
		if(ltype > 29 || rtype > 29){
			return Coerce.toLong(ctx, ltype, lval) <= Coerce.toLong(ctx, rtype, rval);
		}
		
		//If A or B is String coerce both A and B to String, compare lexically
		if(ltype == STRING || rtype == STRING){
			return Coerce.toString(ctx, ltype, lval).compareTo(Coerce.toString(ctx, rtype, rval)) < 0;
		}
		
		//If A is Comparable, then: use result of A.compareTo(B)
		if(lval instanceof Comparable){
			return ((Comparable) lval).compareTo(rval) <= 0;
		}
		
		//If B is Comparable, then: use result of B.compareTo(A)
		if(rval instanceof Comparable){
			return ((Comparable) rval).compareTo(lval) > 0;
		}
		
		throw new ElException(ctx.getMessage("el.errors.valuesNotComparable", lval, rval));
	}
	
	/**
	 *  - operator , jsr341 spec
	 */
	static Object minus(ElEvalContext ctx, int type,Object val) {
		//If A is null, return (Long)0
		if(val == null){
			return Coerce.LG_ZERO;
		}
		
		if(type == -1){
			type = resolveTypeByVal(val);
		}
		
		//If A is a BigDecimal or BigInteger, return A.negate().
		if(type == BIG_DECIMAL){
			return ((BigDecimal)val).negate();
		}
		if(type == BIG_INTEGER){
			return ((BigInteger)val).negate();
		}
		
		/*
			If A is a String:
			■ If A contains ., e, or E, coerce to a Double and apply operator
			■ Otherwise, coerce to a Long and apply operator
			■ If operator results in exception, error
		 */
		if(type == STRING){
			if(isDotEe((String)val)){
				return - Coerce.toDouble(ctx, type, val);
			}else{
				return - Coerce.toLong(ctx, type, val);
			}
		}
		
		/*
			If A is Byte, Short, Integer, Long, Float, Double
			■ Retain type, apply operator
			■ If operator results in exception, error
		 */
		if(type > 39){
			if(type == W_INTEGER || type == INTEGER) {
				return - (Integer)val;
			}
			
			if(type == W_LONG || type == LONG){
				return - (Long)val;
			}
			
			if(type == W_DOUBLE || type == DOUBLE){
				return - (Double)val; 
			}
			
			if(type == W_FLOAT || type == FLOAT){
				return - (Float)val;
			}
			
			if(type == W_SHORT || type == SHORT){
				return - (Short)val;
			}
			
			if(type == W_BYTE || type == BYTE){
				return -(Byte)val;
			}
		}
		
		//Otherwise, error
		throw new ElException(ctx.getMessage("el.errors.cannotApplyMinusOperatorTo", val));
	}
	
	private static final boolean isFloatOrDoubleOrDotEe(int type,Object val) {
		return ElTypes.isFloatOrDouble(type) || (type == STRING && isDotEe((String)val));
	}
	
	private static final boolean isDotEe(String value) {
		if(!(value instanceof String)){
			return false;
		}
		
		String s = (String)value;
		
		int length = s.length();
		for (int i = 0; i < length; i++) {
			switch (s.charAt(i)) {
				case '.':
				case 'E':
				case 'e': return true;
			}
		}
		return false;
	}
	
	protected Ops() {
		
	}
	
}