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
package leap.core.value;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import leap.lang.convert.ConvertException;


/**
 * Represents a scalar value.
 */
public interface Scalar {
	
	/**
	 * Returns <code>true</code> if the scalar value is <code>null</code>
	 */
	boolean isNull();
	
	/**
	 * Returns the scalar value as {@link Object}.
	 */
	Object get();
	
	/**
	 * Returns the scalar value as the given type.
	 * 
	 * @throws ConvertException if cannot convert to value to the target type.
	 */
	<T> T get(Class<T> targetType) throws ConvertException;
	
	/**
	 * Returns the scalar value as {@link String}.
	 * 
	 * @throws ConvertException if cannot convert the value to {@link String}.
	 */
	default String getString() throws ConvertException {
		return get(String.class);
	}
	
	/**
	 * Returns the scalar value as {@link Integer}.
	 * 
	 * @throws ConvertException if cannot convert the value to {@link Integer}.
	 */
	default Integer getInteger() throws ConvertException {
		return get(Integer.class);
	}
	
	/**
	 * Returns the scalar value as {@link Long}.
	 * 
	 * @throws ConvertException if cannot convert the value to {@link Long}.
	 */
	default Long getLong() {
		return get(Long.class);
	}
	
	/**
	 * Returns the scalar value as {@link BigDecimal}.
	 * 
	 * @throws ConvertException if cannot convert the value to {@link BigDecimal}.
	 */
	default BigDecimal getDecimal() {
		return get(BigDecimal.class);
	}
	
	/**
	 * Returns the scalar value as {@link Date}.
	 * 
	 * @throws ConvertException if cannot convert the value to {@link Date}.
	 */
	default Date getDate() {
		return get(Date.class);
	}
	
	/**
	 * Returns the scalar value as {@link Timestamp}.
	 * 
	 * @throws ConvertException if cannot convert the value to {@link Timestamp}.
	 */
	default Timestamp getTimestamp() {
		return get(Timestamp.class);
	}
}