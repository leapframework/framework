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
package leap.orm.df;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import leap.lang.Classes;
import leap.lang.Randoms;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;

public class RandomDataGenerator implements DataGenerator {
	
	protected int maxNullableRandomValue = 3;
	protected int maxDecimalRandomLength = 10;
	
	public int getMaxNullableRandomValue() {
		return maxNullableRandomValue;
	}

	public void setMaxNullableRandomValue(int nullRandomValue) {
		this.maxNullableRandomValue = nullRandomValue;
	}
	
	public int getMaxDecimalRandomLength() {
		return maxDecimalRandomLength;
	}

	public void setMaxDecimalRandomLength(int maxDecimalRandomLength) {
		this.maxDecimalRandomLength = maxDecimalRandomLength;
	}

	@Override
    public Object generateValue(DataGeneratorContext context, EntityMapping em, FieldMapping fm) {
		if(fm.isNullable() && isRandomNullable()){
			return null;
		}

		Class<?> type = fm.getJavaType();
		
		Object value;
		
		if(Classes.isString(type)){
			value = Randoms.nextString(1,fm.getColumn().getLength());
		}else if(Classes.isInteger(type)){
			value = Randoms.nextInt();
		}else if(Classes.isLong(type)){
			value = Randoms.nextLong();
		}else if(Classes.isShort(type)){
			value = Randoms.nextShort();
		}else if(Classes.isFloat(type)){
			value = Randoms.nextFloat();
		}else if(Classes.isDouble(type)){
			value = Randoms.nextDouble();
		}else if(Classes.isCharacter(type)){
			value = Randoms.nextCharacter();
		}else if(Classes.isBoolean(type)){
			value = Randoms.nextBoolean();
		}else if(Classes.isBigDecimal(type)){
			value = new BigDecimal(Randoms.nextDouble());
		}else if(Classes.isBigInteger(type)){
			value = new BigInteger(Randoms.nextStringNumeric(Randoms.nextInt(maxDecimalRandomLength)));
		}else if(Date.class.isAssignableFrom(type)){
			value = new Date(Randoms.nextLong());
		}else{
			//TODO : supports other type
			value = null;
		}
		
		return value;
    }
	
	protected boolean isRandomNullable(){
		return maxNullableRandomValue > 0 ? Randoms.nextInt(0, maxNullableRandomValue) == 0 : false;
	}
}