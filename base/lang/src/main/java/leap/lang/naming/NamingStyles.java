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
package leap.lang.naming;

import java.util.HashMap;
import java.util.Map;

import leap.lang.Strings;
import leap.lang.beans.BeanFactoryBase;

public class NamingStyles {

	protected static final Map<String, NamingStyle> styles = new HashMap<String, NamingStyle>();
	
	public static final NamingStyle RAW = (s) -> {return s;};
	
	/**
	 * "A" -> "a"
	 */
	public static final NamingStyle LOWER = (s) -> {
		return null == s ? null : s.toLowerCase();
	};
	
	/**
	 *  "a" -> "A"
	 */
	public static final NamingStyle UPPER = (s) -> {
		return null == s ? null : s.toUpperCase();
	};
	
	/**
	 * "hello_world" -> "helloWorld"
	 */
	public static final NamingStyle LOWER_CAMEL = (s) -> {
		return null == s ? null : Strings.lowerCamel(s,'_');
	};

	/**
	 * "hello_word" -> "HelloWorld"
	 */
	public static final NamingStyle UPPER_CAMEL = (s) -> {
		return null == s ? null : Strings.upperCamel(s,'_');
	};
	
	/**
	 * "helloWorld" -> "hello_world"
	 */
	public static final NamingStyle LOWER_UNDERSCORE = (s) -> {
		return null == s ? null : Strings.lowerUnderscore(s);
	};
	
	/**
	 * <pre>
	 * "helloWorld" -> "hello_word"
	 * "hello"      -> "hello_"
	 * </pre>
	 */
	public static final NamingStyle LOWER_UNDERSCORE_ = (s) -> {
		if(null == s){
			return null;
		}
		s = Strings.lowerUnderscore(s);
		return s.indexOf('_') >= 0 ? s : s + "_";
	};
	
	public static final String NAME_RAW               = "raw";
	public static final String NAME_LOWER             = "lower";
	public static final String NAME_UPPER             = "upper";
	public static final String NAME_LOWER_CAMEL       = "lower_camel";
	public static final String NAME_UPPER_CAMEL       = "upper_camel";
	public static final String NAME_LOWER_UNDERSCORE  = "lower_underscore";
	public static final String NAME_LOWER_UNDERSCORE_ = "lower_underscore_";
	
	static {
		styles.put(NAME_RAW,               RAW);
		styles.put(NAME_LOWER,             LOWER);
		styles.put(NAME_UPPER,             UPPER);
		styles.put(NAME_LOWER_CAMEL,       LOWER_CAMEL);
		styles.put(NAME_UPPER_CAMEL,       UPPER_CAMEL);
		styles.put(NAME_LOWER_UNDERSCORE,  LOWER_UNDERSCORE);
		styles.put(NAME_LOWER_UNDERSCORE_, LOWER_UNDERSCORE_);
	}
	
	public static NamingStyle get(String name) {
		return styles.get(name);
	}
	
	public static NamingStyle get(String name, BeanFactoryBase factory) {
		NamingStyle ns = factory.tryGetBean(NamingStyle.class, name);
		return null == ns ? get(name) : ns;
	}
	
	protected NamingStyles() {
		
	}
}
