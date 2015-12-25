/*
 * Copyright 2015 the original author or authors.
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
package leap.web.api.meta;

import java.util.Map;

/**
 * @see http://json-schema.org/latest/json-schema-validation.html
 */
public class ApiValidation extends ApiObject {
	
	protected final Number   maximum;
	protected final boolean  exclusiveMaximum;
	protected final Number   minimum;
	protected final boolean  exclusiveMinimum;
	protected final Integer  maxLength;
	protected final Integer  minLength;
	protected final String   pattern;
	protected final Integer  maxItems;
	protected final Integer  minItems;
	protected final boolean  uniqueItems;
	protected final String[] enumValues;
	protected final Number   multipleOf;
	
	public ApiValidation(Number maximum, boolean exclusiveMaximum, Number minimum, boolean exclusiveMinimum, Integer maxLength, Integer minLength,
            String pattern, Integer maxItems, Integer minItems, boolean uniqueItems, String[] enumValues, Number multipleOf,
            Map<String, Object> attrs) {
	    super(attrs);
	    this.maximum = maximum;
	    this.exclusiveMaximum = exclusiveMaximum;
	    this.minimum = minimum;
	    this.exclusiveMinimum = exclusiveMinimum;
	    this.maxLength = maxLength;
	    this.minLength = minLength;
	    this.pattern = pattern;
	    this.maxItems = maxItems;
	    this.minItems = minItems;
	    this.uniqueItems = uniqueItems;
	    this.enumValues = enumValues;
	    this.multipleOf = multipleOf;
    }

	public Number getMaximum() {
		return maximum;
	}

	public boolean isExclusiveMaximum() {
		return exclusiveMaximum;
	}

	public Number getMinimum() {
		return minimum;
	}

	public boolean isExclusiveMinimum() {
		return exclusiveMinimum;
	}

	public Integer getMaxLength() {
		return maxLength;
	}

	public Integer getMinLength() {
		return minLength;
	}

	public String getPattern() {
		return pattern;
	}

	public Integer getMaxItems() {
		return maxItems;
	}

	public Integer getMinItems() {
		return minItems;
	}

	public boolean isUniqueItems() {
		return uniqueItems;
	}

	public String[] getEnumValues() {
		return enumValues;
	}

	public Number getMultipleOf() {
		return multipleOf;
	}
}
