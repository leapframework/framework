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
package leap.lang;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * {@link Pattern} utils.
 */
public class Patterns {
	
	private static final Map<String,Integer>  flags    = new HashMap<>();

	static {
		flags.put("CANON_EQ", 		  		 Pattern.CANON_EQ);
		flags.put("CASE_INSENSITIVE", 		 Pattern.CASE_INSENSITIVE);
		flags.put("COMMENTS", 		  		 Pattern.COMMENTS);
		flags.put("DOTALL", 		  		 Pattern.DOTALL);
		flags.put("LITERAL", 		  		 Pattern.LITERAL);
		flags.put("MULTILINE", 		  		 Pattern.MULTILINE);
		flags.put("UNICODE_CASE", 	 		 Pattern.UNICODE_CASE);
		flags.put("UNICODE_CHARACTER_CLASS", Pattern.UNICODE_CHARACTER_CLASS);
		flags.put("UNIX_LINES", 			 Pattern.UNIX_LINES);
	}
	
	//from hibernate validator : EmailValidator
	private static String ATOM = "[a-z0-9!#$%&'*+/=?^_`{|}~-]";
	private static String DOMAIN = "(" + ATOM + "+(\\." + ATOM + "+)*";
	private static String IP_DOMAIN = "\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\]";

	public static final String  EMAIL_REGEX   = "^" + ATOM + "+(\\." + ATOM + "+)*@" + DOMAIN + "|" + IP_DOMAIN + ")$";
	public static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX,Pattern.CASE_INSENSITIVE);
	
	public static boolean matches(Pattern pattern,String string) {
		return pattern.matcher(string).matches();
	}
	
	public static int parseFlags(String flags){
		String[] flagNames = Strings.split(flags,"|");
		
		int flag = 0;
		
		for(String name : flagNames){
			Integer value = Patterns.flags.get(name);
			if(null == value){
				throw new IllegalStateException("Invalid pattern flag '" + name + "'");
			}
			flag = flag | value;
		}
		
		return flag;
	}
	
	protected Patterns(){
		
	}

}