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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

import leap.lang.Strings;

public class NumberConverters {
	
	public static class ByteConverter extends AbstractNumberConverter<Byte> {

		@Override
	    protected Byte toNumber(Class<?> targetType, Number number) {
			long longValue = number.longValue();
			
	        if (longValue > Byte.MAX_VALUE) {
	        	throw new ConvertException(Strings.format("value '{0}' is too large for type '{1}'",longValue,targetType.getName()));
	        }
	        
	        if (longValue < Byte.MIN_VALUE) {
	        	throw new ConvertException(Strings.format("value '{0}' is too small for type '{1}'",longValue,targetType.getName()));
	        }
	        
	        return Byte.valueOf(number.byteValue());
	    }

		@Override
	    protected Byte toNumber(Class<?> targetType, String stringValue) {
		    return Byte.valueOf(stringValue);
	    }
	}	
	
	public static class FloatConverter extends AbstractNumberConverter<Float> {

		@Override
	    protected Float toNumber(Class<?> targetType, Number number) {
			double doubleValue = number.doubleValue();
			
	        if (doubleValue > Float.MAX_VALUE) {
	        	throw new ConvertException(Strings.format("value '{0}' is too large for type '{1}'",doubleValue,targetType.getName()));
	        }
	        
	        return Float.valueOf(number.floatValue());
	    }

		@Override
	    protected Float toNumber(Class<?> targetType, String stringValue) {
		    return Float.valueOf(stringValue);
	    }
	}
	
	public static class DoubleConverter extends AbstractNumberConverter<Double> {

		@Override
	    protected Double toNumber(Class<?> targetType, Number number) {
	        return Double.valueOf(number.doubleValue());
	    }

		@Override
	    protected Double toNumber(Class<?> targetType, String stringValue) {
		    return Double.valueOf(stringValue);
	    }
	}

	public static class NumberConverter extends AbstractNumberConverter<Number> {

		@Override
		protected Number toNumber(Class<?> targetType, Number number) {
			return number;
		}

		@Override
		protected Number toNumber(Class<?> targetType, String stringValue) {
		    if(stringValue.contains(".")) {
		        return new BigDecimal(stringValue).doubleValue();
            }else {
		        return Long.parseLong(stringValue);
            }
		}
	}
	
	public static class BigDecimalConverter extends AbstractNumberConverter<BigDecimal> {

		@Override
	    protected BigDecimal toNumber(Class<?> targetType, Number number) {
	        if (number instanceof Float || number instanceof Double) {
	        	return new BigDecimal(number.toString());
	        } else if (number instanceof BigInteger) {
	            return new BigDecimal((BigInteger)number);
	        } else {
	            return BigDecimal.valueOf(number.longValue());
	        }
	    }

		@Override
	    protected BigDecimal toNumber(Class<?> targetType, String stringValue) {
		    return new BigDecimal(stringValue);
	    }
	}
	
	public static class BigIntegerConverter extends AbstractNumberConverter<BigInteger> {

		@Override
	    protected BigInteger toNumber(Class<?> targetType, Number number) {
            if (number instanceof BigDecimal) {
                return ((BigDecimal)number).toBigInteger();
            } else {
                return BigInteger.valueOf(number.longValue());
            }
	    }

		@Override
	    protected BigInteger toNumber(Class<?> targetType, String stringValue) {
		    return new BigInteger(stringValue);
	    }
	}	
	
	public static class IntegerConverter extends AbstractNumberConverter<Integer> {

		@Override
	    protected Integer toNumber(Class<?> targetType, Number number) {
			long longValue = number.longValue();
			
	        if (longValue > Integer.MAX_VALUE) {
	        	throw new ConvertException(Strings.format("value '{0}' is too large for type '{1}'",longValue,targetType.getName()));
	        }
	        
	        if (longValue < Integer.MIN_VALUE) {
	        	throw new ConvertException(Strings.format("value '{0}' is too small for type '{1}'",longValue,targetType.getName()));
	        }
	        
	        return Integer.valueOf(number.intValue());
	    }

		@Override
	    protected Integer toNumber(Class<?> targetType, String stringValue) {
		    return Integer.valueOf(stringValue);
	    }
	}	
	
	public static class ShortConverter extends AbstractNumberConverter<Short> {

		@Override
	    protected Short toNumber(Class<?> targetType, Number number) {
			long longValue = number.longValue();
			
	        if (longValue > Short.MAX_VALUE) {
	        	throw new ConvertException(Strings.format("value '{0}' is too large for type '{1}'",longValue,targetType.getName()));
	        }
	        
	        if (longValue < Short.MIN_VALUE) {
	        	throw new ConvertException(Strings.format("value '{0}' is too small for type '{1}'",longValue,targetType.getName()));
	        }
	        
	        return Short.valueOf(number.shortValue());
	    }

		@Override
	    protected Short toNumber(Class<?> targetType, String stringValue) {
		    return Short.valueOf(stringValue);
	    }
	}	

	public static class LongConverter extends AbstractNumberConverter<Long> {

		@Override
	    protected Long toNumber(Class<?> targetType, Number number) {
	        return Long.valueOf(number.longValue());
	    }
		
		@Override
	    protected Long toNumber(Class<?> targetType, Object value) {
			if(value instanceof Date){
				return toNumber(targetType, ((Date)value).getTime());
			}else if(value instanceof Calendar){
				return toNumber(targetType, ((Calendar)value).getTime());
			}
			return super.toNumber(targetType, value);
	    }

		@Override
	    protected Long toNumber(Class<?> targetType, String stringValue) {
		    return Long.valueOf(stringValue);
	    }
	}
}
