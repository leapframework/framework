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
package leap.lang.text;

import leap.lang.Strings;
import leap.lang.accessor.MapPropertyAccessor;
import leap.lang.accessor.PropertyGetter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class DefaultPlaceholderResolver implements PlaceholderResolver {
	
	protected String  placeholderPrefix = "${";
	protected String  placeholderSuffix = "}";
	protected String  valueSeparator    = ":";
	protected boolean ignoreUnresolvablePlaceholders = false;
	protected boolean emptyUnresolvablePlaceholders  = true;
	
	protected PropertyGetter properties;
	
	public DefaultPlaceholderResolver(PropertyGetter properties){
		this.properties = properties;
	}
	
	public DefaultPlaceholderResolver(Map<String, String> properties){
		this(new MapPropertyAccessor(properties));
	}
	
	public String getPlaceholderPrefix() {
		return placeholderPrefix;
	}

	public void setPlaceholderPrefix(String placeholderPrefix) {
		this.placeholderPrefix = placeholderPrefix;
	}

	public String getPlaceholderSuffix() {
		return placeholderSuffix;
	}

	public void setPlaceholderSuffix(String placeholderSuffix) {
		this.placeholderSuffix = placeholderSuffix;
	}

	public String getValueSeparator() {
		return valueSeparator;
	}

	public void setValueSeparator(String valueSeparator) {
		this.valueSeparator = valueSeparator;
	}

	public boolean isIgnoreUnresolvablePlaceholders() {
		return ignoreUnresolvablePlaceholders;
	}

	public void setIgnoreUnresolvablePlaceholders(boolean ignoreUnresolvablePlaceholders) {
		this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
	}
	
	public boolean isEmptyUnresolvablePlaceholders() {
		return emptyUnresolvablePlaceholders;
	}

	public void setEmptyUnresolvablePlaceholders(boolean emptyUnresolvablePlaceholders) {
		this.emptyUnresolvablePlaceholders = emptyUnresolvablePlaceholders;
	}

    @Override
    public boolean hasPlaceholder(String value) {
        if(null == value) {
            return false;
        }

        int prefix = value.indexOf(placeholderPrefix);
        if(prefix < 0) {
            return false;
        }

        int suffix = value.indexOf(placeholderSuffix, prefix + placeholderPrefix.length());
        if(suffix < 0) {
            return false;
        }

        return true;
    }

    @Override
	public String resolveString(String value) {
		if(Strings.isEmpty(value)){
			return value;
		}
		
		int startIndex = value.indexOf(placeholderPrefix);
		if(startIndex < 0){
			return value;
		}
		
		return parseStringValue(value, new HashSet<>(), startIndex);
	}
	
	@Override
    public String resolveString(String value, String defaultValue) {
	    String resolved = resolveString(value);
	    
	    if(Strings.equals(resolved, value)) {
	        return resolveString(defaultValue);
	    }
	    
	    if(value.length() > 0 && emptyUnresolvablePlaceholders && resolved.isEmpty()) {
	        return resolveString(defaultValue);
	    }
	    
        return resolved;
    }

    protected String parseStringValue(String strVal, Set<String> visitedPlaceholders) {
		int startIndex = strVal.indexOf(placeholderPrefix);
		
		if(startIndex < 0){
			return strVal;
		}
		
		return parseStringValue(strVal, visitedPlaceholders, startIndex);
	}
	
	protected String parseStringValue(String strVal, Set<String> visitedPlaceholders, int startIndex) {
		StringBuilder buf = new StringBuilder(strVal);
		while (startIndex != -1) {
			int endIndex = findPlaceholderEndIndex(buf, startIndex);
			if (endIndex != -1) {
				String placeholder = buf.substring(startIndex + placeholderPrefix.length(), endIndex);
				String originalPlaceholder = placeholder;
				if (!visitedPlaceholders.add(originalPlaceholder)) {
					throw new IllegalArgumentException(
							"Circular placeholder reference '" + originalPlaceholder + "' in property definitions");
				}
				// Recursive invocation, parsing placeholders contained in the placeholder key.
				placeholder = parseStringValue(placeholder, visitedPlaceholders);
				// Now obtain the value for the fully resolved key...
				String propVal = properties.getProperty(placeholder);
				if (propVal == null && this.valueSeparator != null) {
					int separatorIndex = placeholder.indexOf(this.valueSeparator);
					if (separatorIndex != -1) {
						String actualPlaceholder = placeholder.substring(0, separatorIndex);
						String defaultValue = placeholder.substring(separatorIndex + this.valueSeparator.length());
						propVal = properties.getProperty(actualPlaceholder);
						if (propVal == null) {
							propVal = defaultValue;
						}
					}
				}
				
				if(null == propVal && emptyUnresolvablePlaceholders){
					propVal = Strings.EMPTY;
				}
				
				if (propVal != null) {
					// Recursive invocation, parsing placeholders contained in the
					// previously resolved placeholder value.
					if(!Strings.isEmpty(propVal)){
						propVal = parseStringValue(propVal, visitedPlaceholders);	
					}
					buf.replace(startIndex, endIndex + this.placeholderSuffix.length(), propVal);
					startIndex = buf.indexOf(this.placeholderPrefix, startIndex + propVal.length());
				}
				else if (this.ignoreUnresolvablePlaceholders) {
					// Proceed with unprocessed value.
					startIndex = buf.indexOf(this.placeholderPrefix, endIndex + this.placeholderSuffix.length());
				}
				else {
					throw new IllegalArgumentException("Could not resolve placeholder '" +
							placeholder + "'" + " in string value \"" + strVal + "\"");
				}
				visitedPlaceholders.remove(originalPlaceholder);
			}
			else {
				startIndex = -1;
			}
		}

		return buf.toString();
	}

	private int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
		int index = startIndex + this.placeholderPrefix.length();
		int withinNestedPlaceholder = 0;
		while (index < buf.length()) {
			if (substringMatch(buf, index, this.placeholderSuffix)) {
				if (withinNestedPlaceholder > 0) {
					withinNestedPlaceholder--;
					index = index + this.placeholderSuffix.length();
				}
				else {
					return index;
				}
			}
//			else if (substringMatch(buf, index, this.simplePrefix)) {
//				withinNestedPlaceholder++;
//				index = index + this.simplePrefix.length();
//			}
			else {
				index++;
			}
		}
		return -1;
	}	
	
	private static boolean substringMatch(CharSequence str, int index, CharSequence substring) {
		for (int j = 0; j < substring.length(); j++) {
			int i = index + j;
			if (i >= str.length() || str.charAt(i) != substring.charAt(j)) {
				return false;
			}
		}
		return true;
	}	
}
