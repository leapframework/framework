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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * null safe utils for {@link String}
 */
public class Strings {
	
	public static final String	EMPTY = "";
	public static final String  NULL  = "null";
	public static final String	COMMA = ",";
	public static final char[]  DEFAULT_SPLIT_CHARS = new char[]{','};
	
	/**
	 * Returns empty "" if the given string is <code>null</code> 
	 */
	public static String nullToEmpty(String string) {
		return null == string ? EMPTY : string;
	}
	
	public static boolean isEmpty(String string) {
		return string == null || string.length() == 0;
	}
	
	public static boolean isAllEmpty(String... strings) {
		if(null == strings || strings.length == 0){
			return true;
		}
		
		for(int i=0;i<strings.length;i++){
			if(isNotBlank(strings[i])){
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean isNotEmpty(String string){
		return null != string && string.length() > 0;
	}
	
	public static boolean isBlank(String string) {
		int strLen;
		if (string == null || (strLen = string.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if (Character.isWhitespace(string.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean isNotBlank(String string) {
		int strLen;
		if (string == null || (strLen = string.length()) == 0) {
			return false;
		}
		for (int i = 0; i < strLen; i++) {
			if (Character.isWhitespace(string.charAt(i)) == false) {
				return true;
			}
		}
		return false;
	}	
	
	public static boolean isNullOrEmpty(Object value){
		if(null == value){
			return true;
		}
		if(value instanceof String){
			return isEmpty((String)value);
		}
		return false;
	}
	
	public static boolean isNullOrBlank(Object value){
		if(null == value){
			return true;
		}
		if(value instanceof String){
			return isBlank((String)value);
		}
		return false;
	}
	
	public static boolean equals(String string1, String string2) {
		return string1 == null ? string2 == null : string1.equals(string2);
	}
	
	public static boolean equals(String string1, String string2, boolean ignoreCase) {
		return string1 == null ? string2 == null : (ignoreCase ? string1.equalsIgnoreCase(string2) : string1.equals(string2));
	}
	
	public static boolean equalsIgnoreCase(String string1, String string2) {
		if (string1 == null || string2 == null) {
			return string1 == string2;
		} else {
			return string1.equalsIgnoreCase(string2);
		}
	}	
	
	//length
	public static int length(String s){
		return null == s ? 0 : s.length();
	}
	
	//count
	
    public static int count(String str, char c) {
    	if(null == str){
    		return 0;
    	}
    	int count = 0;
    	for(int i=0;i<str.length();i++){
    		if(str.charAt(i) == c){
    			count++;
    		}
    	}
    	return count;
    }	
	
	/**
	 * returns the fisrt not empty string item in the given string array.
	 * 
	 * <p/>
	 * 
	 * returns empty string if all string item is empty.
	 */
	public static String firstNotEmpty(String... strings){
		for(String string : strings){
			if(!isEmpty(string)){
				return string;
			}
		}
		return EMPTY;
	}
	
	/**
	 * <pre class="code">
	 * 
	 * format("hello {0}","world") -> "hello world"
	 * 
	 * format("the argument[name = '{0}', index = {1}] must not be null","name",1) -> "the argument[name = 'name', index = 1] must not be null"
	 * 
	 * format("the argument[name = '{1}', index = {0}] must not be null",1,"name") -> "the argument[name = 'name', index = 1] must not be null"
	 * 
	 * </pre>
	 */
	public static String format(String template, Object... args) {
		if (isEmpty(template)) {
			return EMPTY;
		}
		
		if(null == args || args.length == 0){
			return template;
		}

		char[] templateChars = template.toCharArray();

		int templateLength = templateChars.length;
		int length = 0;
		int tokenCount = args.length;
		for (int i = 0; i < tokenCount; i++) {
			Object sourceString = args[i];
			if (sourceString != null) {
				length += sourceString.toString().length();
			}
		}

		// The following buffer size is just an initial estimate. It is legal for
		// any given pattern, such as {0}, to occur more than once, in which case
		// the buffer size will expand automatically if need be.
		StringBuilder buffer = new StringBuilder(length + templateLength);

		int lastStart = 0;
		for (int i = 0; i < templateLength; i++) {
			char ch = templateChars[i];
			if (ch == '{') {
				// Only check for single digit patterns that have an associated token.
				if (i + 2 < templateLength && templateChars[i + 2] == '}') {
					int tokenIndex = templateChars[i + 1] - '0';
					if (tokenIndex >= 0 && tokenIndex < tokenCount) {
						buffer.append(templateChars, lastStart, i - lastStart);
						Object sourceString = args[tokenIndex];
						if (sourceString != null){
							buffer.append(sourceString.toString());
						}
						i += 2;
						lastStart = i + 1;
					}
				}
			}
			// ELSE: Do nothing. The character will be added in later.
		}

		buffer.append(templateChars, lastStart, templateLength - lastStart);

		return new String(buffer);
	}
	
	//trim
	
	public static String trim(String string) {
		return string == null ? EMPTY : string.trim();
	}
	
	public static String trimStart(String s) {
		int strLen;

		if (s == null || (strLen = s.length()) == 0) {
			return EMPTY;
		}

		int start = 0;
		while (start != strLen && Character.isWhitespace(s.charAt(start))) {
			start++;
		}
		
		return s.substring(start);
	}
	
	/**
	 * <p>
	 * Trims any of a set of characters from the start of a String.
	 * </p>
	 * 
	 * <p>
	 * A {@code null} input String returns "". An empty string ("") input returns the empty string.
	 * </p>
	 * 
	 * <p>
	 * If the trimChars String is {@code null}, whitespace is trimmed as defined by {@link Character#isWhitespace(char)}
	 * .
	 * </p>
	 * 
	 * <pre>
	 * Strings.trimStart(null, *)          = ""
	 * Strings.trimStart("", *)            = ""
	 * Strings.trimStart("abc", "")        = "abc"
	 * Strings.trimStart("abc", null)      = "abc"
	 * Strings.trimStart("  abc", null)    = "abc"
	 * Strings.trimStart("abc  ", null)    = "abc  "
	 * Strings.trimStart(" abc ", null)    = "abc "
	 * Strings.trimStart("yxabc  ", "xyz") = "abc  "
	 * </pre>
	 * 
	 * @param str the String to remove characters from, may be null
	 * @param trimChars the characters to remove, null treated as whitespace
	 * @return the trimmed String, "" if null String input
	 */
	public static String trimStart(String str, char... trimChars) {
		int strLen;

		if (str == null || (strLen = str.length()) == 0) {
			return EMPTY;
		}

		int start = 0;
		if (trimChars == null) {
			while (start != strLen && Character.isWhitespace(str.charAt(start))) {
				start++;
			}
		} else if (trimChars.length == 0) {
			return str;
		} else {
			while (start != strLen && Arrays2.indexOf(trimChars, str.charAt(start)) != Arrays2.INDEX_NOT_FOUND) {
				start++;
			}
		}
		return str.substring(start);
	}

	public static String trimEnd(String s){
		int end;
		if (s == null || (end = s.length()) == 0) {
			return EMPTY;
		}

		while (end != 0 && Character.isWhitespace(s.charAt(end - 1))) {
			end--;
		}
		return s.substring(0, end);
	}
	
	/**
	 * <p>
	 * Removes any of a set of characters from the end of a String.
	 * </p>
	 * 
	 * <p>
	 * A {@code null} input String returns "". An empty string ("") input returns the empty string.
	 * </p>
	 * 
	 * <p>
	 * If the trimChars String is {@code null}, whitespace is trimmed as defined by {@link Character#isWhitespace(char)}
	 * .
	 * </p>
	 * 
	 * <pre>
	 * Strings.trimEnd(null, *)          = ""
	 * Strings.trimEnd("", *)            = ""
	 * Strings.trimEnd("abc", "")        = "abc"
	 * Strings.trimEnd("abc", null)      = "abc"
	 * Strings.trimEnd("  abc", null)    = "  abc"
	 * Strings.trimEnd("abc  ", null)    = "abc"
	 * Strings.trimEnd(" abc ", null)    = " abc"
	 * Strings.trimEnd("  abcyx", "xyz") = "  abc"
	 * Strings.trimEnd("120.00", ".0")   = "12"
	 * </pre>
	 * 
	 * @param str the String to remove characters from, may be null
	 * @param trimChars the set of characters to remove, null treated as whitespace
	 * @return the trimmed String, "" if null String input
	 */
	public static String trimEnd(String str, char... trimChars) {
		int end;
		if (str == null || (end = str.length()) == 0) {
			return EMPTY;
		}

		if (trimChars == null) {
			while (end != 0 && Character.isWhitespace(str.charAt(end - 1))) {
				end--;
			}
		} else if (trimChars.length == 0) {
			return str;
		} else {
			while (end != 0 && Arrays2.indexOf(trimChars, str.charAt(end - 1)) != Arrays2.INDEX_NOT_FOUND) {
				end--;
			}
		}
		return str.substring(0, end);
	}	

	/**
	 * Trims the string, if the trimmed string is empty than returns <code>null</coce>
	 */
	public static String trimToNull(String string) {
		if(null == string){
			return null;
		}
		String ts = string.trim();
		return ts.length() == 0 ? null : ts;
	}
	
	//split
	
	public static String[] split(String string) {
		return splitWorker(string, Chars.COMMA, false, true, true);
	}
	
	public static String[] splitWhitespaces(String s) {
		if(null == s || s.length() == 0) {
			return Arrays2.EMPTY_STRING_ARRAY;
		}
		
		List<String> list = new ArrayList<String>();
		int i = 0, start = 0, len = s.length();
		boolean match = false;

		while (i < len) {
			if (Character.isWhitespace(s.charAt(i))) {
				if (match) {
					String token = s.substring(start, i).trim();

					if (token.length() > 0) {
						list.add(token);
					}
					
					match = false;
				}
				start = ++i;
				continue;
			}
			match = true;
			i++;
		}
		
		if(match) {
			String token = s.substring(start, i).trim();
			if(token.length() > 0) {
				list.add(token);
			}
		}

		return list.toArray(new String[list.size()]);
	}
	
	public static String[] splitMultiLines(String string){
		return splitWorker(string, -1, false, true, true, '\r','\n');
	}
	
	public static String[] splitMultiLines(String string,char separator){
		return splitWorker(string, -1, false, true, true, '\r', '\n', separator);
	}

	public static String[] split(String string, char separator) {
		return splitWorker(string, -1, false, true, true, separator);
	}
	
	public static String[] split(String string, char... separators) {
		return splitWorker(string,-1,false, true, true, separators);
	}
	
	public static String[] split(String string, String separator) {
		return splitByWholeSeparatorWorker(string, separator, -1, false, true, true);
	}	

	public static String[] split(String string, char separator, boolean trim) {
		return splitWorker(string, -1, false, trim, true, separator);
	}
	
	public static String[] split(String string, char[] separators, boolean trim) {
		return splitWorker(string, -1, false, trim, true, separators);
	}	

	public static String[] split(String string, String separator, boolean trim) {
		return splitByWholeSeparatorWorker(string, separator, -1, false, trim, true);
	}
	
	public static String[] split(String string, String separator, boolean trim,boolean ignoreEmpty) {
		return splitByWholeSeparatorWorker(string, separator, -1, !ignoreEmpty, trim, ignoreEmpty);
	}
	
	public static String[] split(String string, char[] separators, boolean trim,boolean ignoreEmpty) {
		return splitWorker(string, -1, !ignoreEmpty, trim, ignoreEmpty,separators);
	}	
	
	//replace
	
	public static String replaceOnce(String text, String oldString, String newString) {
		return replace(text, oldString, newString, 1);
	}

	public static String replace(String text, String oldString, String newString) {
		return replace(text, oldString, newString, -1);
	}
	
	public static String replaceIgnoreCase(String text, String oldString, String newString) {
		return replaceIgnoreCase(text, oldString, newString, -1);
	}

	public static String replace(String text, char oldChar, char newChar) {
		if (text == null) {
			return EMPTY;
		}
		return text.replace(oldChar, newChar);
	}	
	
	//remove
	// Remove
	//-----------------------------------------------------------------------

	/**
	 * <p>
	 * Removes all occurrences of a substring from within the source string.
	 * </p>
	 * 
	 * <p>
	 * A {@code null} search string will return the source string. A {@code null} source string will return the empty
	 * string. string.
	 * </p>
	 * 
	 * <pre>
	 * Strings.remove(null, *)        = ""
	 * Strings.remove("", *)          = ""
	 * Strings.remove(*, null)        = *
	 * Strings.remove(*, "")          = *
	 * Strings.remove("queued", "ue") = "qd"
	 * Strings.remove("queued", "zz") = "queued"
	 * </pre>
	 * 
	 * @param string the source String to search, may be null
	 * 
	 * @param remove the String to search for and remove, may be null
	 * 
	 * @return the substring with the string removed if found, "" if null String input
	 */
	public static String remove(String string, String remove) {
		if (isEmpty(string)) {
			return EMPTY;
		}

		if (isEmpty(remove)) {
			return string;
		}

		return replace(string, remove, EMPTY, -1);
	}

	/**
	 * <p>
	 * Removes all occurrences of a character from within the source string.
	 * </p>
	 * 
	 * <p>
	 * A {@code null} source string will return the empty string.
	 * </p>
	 * 
	 * <pre>
	 * Strings.remove(null, *)       = null
	 * Strings.remove("", *)         = ""
	 * Strings.remove("queued", 'u') = "qeed"
	 * Strings.remove("queued", 'z') = "queued"
	 * </pre>
	 * 
	 * @param string the source String to search, may be null
	 * 
	 * @param remove the char to search for and remove, may be null
	 * 
	 * @return the substring with the char removed if found, "" if null String input
	 */
	public static String remove(String string, char remove) {
		if (isEmpty(string)) {
			return EMPTY;
		}

		if (string.indexOf(remove) == Arrays2.INDEX_NOT_FOUND) {
			return string;
		}

		char[] chars = string.toCharArray();
		int pos = 0;
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] != remove) {
				chars[pos++] = chars[i];
			}
		}
		return new String(chars, 0, pos);
	}

	/**
	 * <p>
	 * Removes all blank string from a String as defined by {@link Character#isWhitespace(char)}.
	 * </p>
	 * 
	 * <pre>
	 * Strings.removeBlank(null)         = ""
	 * Strings.removeBlank("")           = ""
	 * Strings.removeBlank("abc")        = "abc"
	 * Strings.removeBlank("   ab  c  ") = "abc"
	 * </pre>
	 * 
	 * @param str the String to delete whitespace from, may be null
	 * 
	 * @return the String without whitespaces, "" if null String input
	 */
	public static String removeBlank(String str) {
		if (isEmpty(str)) {
			return EMPTY;
		}
		int sz = str.length();
		char[] chs = new char[sz];
		int count = 0;
		for (int i = 0; i < sz; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				chs[count++] = str.charAt(i);
			}
		}
		if (count == sz) {
			return str;
		}
		return new String(chs, 0, count);
	}

	/**
	 * <p>
	 * Removes a substring only if it is at the beginning of a source string, otherwise returns the source string.
	 * </p>
	 * 
	 * <p>
	 * A {@code null} search string will return the source string. A {@code null} source string will return the empty
	 * string.
	 * </p>
	 * 
	 * <pre>
	 * Strings.removeStart(null, *)      			   = ""
	 * Strings.removeStart("", *)        			   = ""
	 * Strings.removeStart(*, null)      			   = *
	 * Strings.removeStart("www.domain.com", "www.")   = "domain.com"
	 * Strings.removeStart("domain.com", "www.")       = "domain.com"
	 * Strings.removeStart("www.domain.com", "domain") = "www.domain.com"
	 * Strings.removeStart("abc", "")    			   = "abc"
	 * </pre>
	 * 
	 * @param string the source String to search, may be null
	 * 
	 * @param remove the String to search for and remove, may be null
	 * 
	 * @return the substring with the string removed if found, "" if null String input
	 */
	public static String removeStart(String string, String remove) {
		if (isEmpty(string)) {
			return EMPTY;
		}

		if (isEmpty(remove)) {
			return string;
		}

		if (string.startsWith(remove)) {
			return string.substring(remove.length());
		}

		return string;
	}

	/**
	 * <p>
	 * Case insensitive removal of a substring if it is at the beginning of a source string, otherwise returns the
	 * source string.
	 * </p>
	 * 
	 * <p>
	 * A {@code null} search string will return the source string. A {@code null} source string will return the empty
	 * string.
	 * </p>
	 * 
	 * <pre>
	 * Strings.removeStartIgnoreCase(null, *)      				 = ""
	 * Strings.removeStartIgnoreCase("", *)        				 = ""
	 * Strings.removeStartIgnoreCase(*, null)      				 = *
	 * Strings.removeStartIgnoreCase("www.domain.com", "www.")   = "domain.com"
	 * Strings.removeStartIgnoreCase("www.domain.com", "WWW.")   = "domain.com"
	 * Strings.removeStartIgnoreCase("domain.com", "www.")       = "domain.com"
	 * Strings.removeStartIgnoreCase("www.domain.com", "domain") = "www.domain.com"
	 * Strings.removeStartIgnoreCase("abc", "")    				 = "abc"
	 * </pre>
	 * 
	 * @param string the source String to search, may be null
	 * 
	 * @param remove the String to search for (case insensitive) and remove, may be null
	 * 
	 * @return the substring with the string removed if found, "" if null String input
	 */
	public static String removeStartIgnoreCase(String string, String remove) {
		if (isEmpty(string)) {
			return EMPTY;
		}
		if (isEmpty(remove)) {
			return string;
		}
		if (startsWithIgnoreCase(string, remove)) {
			return string.substring(remove.length());
		}
		return string;
	}

	/**
	 * <p>
	 * Removes a substring only if it is at the end of a source string, otherwise returns the source string.
	 * </p>
	 * 
	 * <p>
	 * A {@code null} search string will return the source string. A {@code null} source string will return the empty
	 * string.
	 * </p>
	 * 
	 * <pre>
	 * Strings.removeEnd(null, *)      = ""
	 * Strings.removeEnd("", *)        = ""
	 * Strings.removeEnd(*, null)      = *
	 * Strings.removeEnd("www.domain.com", ".com.")  = "www.domain.com"
	 * Strings.removeEnd("www.domain.com", ".com")   = "www.domain"
	 * Strings.removeEnd("www.domain.com", "domain") = "www.domain.com"
	 * Strings.removeEnd("abc", "")    = "abc"
	 * </pre>
	 * 
	 * @param string the source String to search, may be null
	 * 
	 * @param remove the String to search for and remove, may be null
	 * 
	 * @return the substring with the string removed if found, "" if null String input
	 */
	public static String removeEnd(String string, String remove) {
		if (isEmpty(string)) {
			return EMPTY;
		}

		if (isEmpty(remove)) {
			return string;
		}

		if (string.endsWith(remove)) {
			return string.substring(0, string.length() - remove.length());
		}

		return string;
	}

	/**
	 * <p>
	 * Case insensitive removal of a substring if it is at the end of a source string, otherwise returns the source
	 * string.
	 * </p>
	 * 
	 * <p>
	 * A {@code null} search string will return the source string. A {@code null} source string will return the empty
	 * string.
	 * </p>
	 * 
	 * <pre>
	 * Strings.removeEndIgnoreCase(null, *)      = ""
	 * Strings.removeEndIgnoreCase("", *)        = ""
	 * Strings.removeEndIgnoreCase(*, null)      = *
	 * Strings.removeEndIgnoreCase("www.domain.com", ".com.")  = "www.domain.com"
	 * Strings.removeEndIgnoreCase("www.domain.com", ".com")   = "www.domain"
	 * Strings.removeEndIgnoreCase("www.domain.com", "domain") = "www.domain.com"
	 * Strings.removeEndIgnoreCase("abc", "")    = "abc"
	 * Strings.removeEndIgnoreCase("www.domain.com", ".COM") = "www.domain")
	 * Strings.removeEndIgnoreCase("www.domain.COM", ".com") = "www.domain")
	 * </pre>
	 * 
	 * @param str the source String to search, may be null
	 * 
	 * @param remove the String to search for (case insensitive) and remove, may be null
	 * 
	 * @return the substring with the string removed if found, "" if null String input
	 */
	public static String removeEndIgnoreCase(String str, String remove) {
		if (isEmpty(str)) {
			return EMPTY;
		}

		if (isEmpty(remove)) {
			return str;
		}

		if (endsWithIgnoreCase(str, remove)) {
			return str.substring(0, str.length() - remove.length());
		}

		return str;
	}	
	
	//concat
	public static String concat(String... strs){
		if(null == strs || strs.length == 0){
			return EMPTY;
		}
		
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<strs.length;i++){
			String s = strs[i];
			if(null != s && s.length() > 0){
				sb.append(s);
			}
		}
		return sb.toString();
	}
	
    //join
    
	public static String join(Object[] array, char separator) {
		if (array == null) {
			return EMPTY;
		}

		int len = array.length;

		StringBuilder buf = new StringBuilder(len * 16);

		for (int i = 0; i < len; i++) {
			if (i > 0) {
				buf.append(separator);
			}

			if (array[i] != null) {
				buf.append(array[i]);
			}
		}

		return buf.toString();
	}
	
	public static String join(Object[] array, String separator) {
		if (array == null) {
			return EMPTY;
		}

		int len = array.length;

		StringBuilder buf = new StringBuilder(len * 16);

		for (int i = 0; i < len; i++) {
			if (i > 0 && separator != null) {
				buf.append(separator);
			}

			if (array[i] != null) {
				buf.append(array[i]);
			}
		}

		return buf.toString();
	}
	
	public static String join(Object[] array,String separator,boolean ignoreEmpty){
		if(!ignoreEmpty){
			return join(array,separator);
		}
		
		if (array == null) {
			return EMPTY;
		}

		int len = array.length;

		StringBuilder buf = new StringBuilder(len * 16);

		int j=0;
		for (int i = 0; i < len; i++) {
			Object value = array[i];
			
			if(!Objects2.isEmpty(value)){
				if (j > 0 && separator != null) {
					buf.append(separator);
				}

				buf.append(value);
				j++;
			}
		}

		return buf.toString();
	}	
	
	public static String join(Iterator<?> iterator, char separator) {

		// handle null, zero and one elements before building a buffer
		if (iterator == null) {
			return EMPTY;
		}

		if (!iterator.hasNext()) {
			return EMPTY;
		}

		Object first = iterator.next();
		if (!iterator.hasNext()) {
			return Objects.toString(first,EMPTY);
		}

		// two or more elements
		StringBuilder buf = new StringBuilder(256); // Java default is 16, probably too small
		if (first != null) {
			buf.append(first);
		}

		while (iterator.hasNext()) {
			buf.append(separator);
			Object obj = iterator.next();
			if (obj != null) {
				buf.append(obj);
			}
		}

		return buf.toString();
	}

	public static String join(Iterator<?> iterator, String separator) {
		// handle null, zero and one elements before building a buffer
		if (iterator == null) {
			return EMPTY;
		}
		if (!iterator.hasNext()) {
			return EMPTY;
		}
		Object first = iterator.next();
		if (!iterator.hasNext()) {
			return Objects.toString(first,EMPTY);
		}

		// two or more elements
		StringBuilder buf = new StringBuilder(256); // Java default is 16, probably too small
		if (first != null) {
			buf.append(first);
		}

		while (iterator.hasNext()) {
			if (separator != null) {
				buf.append(separator);
			}
			Object obj = iterator.next();
			if (obj != null) {
				buf.append(obj);
			}
		}
		return buf.toString();
	}

	public static String join(Iterable<?> iterable, char separator) {
		if (iterable == null) {
			return EMPTY;
		}
		return join(iterable.iterator(), separator);
	}

	public static String join(Iterable<?> iterable, String separator) {
		if (iterable == null) {
			return EMPTY;
		}
		return join(iterable.iterator(), separator);
	}	
	
	public static String upperFirst(String string){
		if(null == string){
			return EMPTY;
		}
		
		if(string.length() > 1){
			return Character.toUpperCase(string.charAt(0)) + string.substring(1);
		}else{
			return string;
		}
	}
	
	public static String lowerFirst(String string){
		if(null == string){
			return EMPTY;
		}
		
		if(string.length() > 1){
			return Character.toLowerCase(string.charAt(0)) + string.substring(1);
		}else{
			return string;
		}
	}
	
	public static String upperCase(String string) {
		if (string == null) {
			return EMPTY;
		}
		return string.toUpperCase();
	}

	public static String lowerCase(String string) {
		if (string == null) {
			return EMPTY;
		}
		return string.toLowerCase();
	}
	
	/**
	 * <pre>
	 * Strings.lowerCamel(null) 	      = ""
	 * Strings.lowerCamel("")   	      = ""
	 * Strings.lowerCamel("hello_world",'_') = helloWorld
	 * </pre>
	 */
	public static String lowerCamel(String string, char seperator) {
		if(null == string){
			return EMPTY;
		}
		
		String[]	  parts = split(string,seperator);
		StringBuilder out   = new StringBuilder(string.length());
		for (String part : parts) {
			if (out.length() == 0) {
				out.append(part.toLowerCase());
			}else{
				out.append(part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase());
			}
		}
		return out.toString();
	}
	
	public static String lowerCamel(String... parts){
		StringBuilder out = new StringBuilder();
		for (String part : parts) {
			if(null == part || part.length() == 0){
				continue;
			}
			if (out.length() == 0) {
				out.append(part.toLowerCase());
			}else {
				out.append(part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase());
			}
		}
		return out.toString();
	}
	
	/**
	 * <pre>
	 * Strings.upperCamel(null) 	      = ""
	 * Strings.upperCamel("")   	      = ""
	 * Strings.upperCamel("hello_world",'_') = HelloWorld
	 * </pre>
	 */
	public static String upperCamel(String string, char seperator) {
		if(null == string){
			return EMPTY;
		}
		
		String[] parts 	  = split(string,seperator);
		StringBuilder out = new StringBuilder();
		for (String part : parts) {
			out.append(part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase());
		}
		return out.toString();
	}

	public static String upperCamel(String... parts) {
		StringBuilder out = new StringBuilder();
		for (String part : parts) {
			if(null == part || part.length() == 0){
				continue;
			}
			out.append(part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase());
		}
		return out.toString();
	}
	
	public static String lowerUnderscore(String name) {
		return lowerSplit(name, '_');
	}
	
	public static String lowerHyphen(String name) {
		return lowerSplit(name, '-');
	}
	
	protected static String lowerSplit(String name, char c) {
		StringBuilder buf = new StringBuilder(name);
		for (int i=1; i<buf.length()-1; i++) {
			char p = buf.charAt(i-1);
			if (
				(Character.isLowerCase( p ) || Character.isDigit(p) ) &&
				Character.isUpperCase( buf.charAt(i) ) &&
				Character.isLowerCase( buf.charAt(i+1) )
			) {
				buf.insert(i++, c);
			}
		}
		
		return buf.toString().toLowerCase();
	}
	
	//stars & ends with
	
	public static boolean startsWith(String string, String startsWith) {
		return startsWith(string, startsWith, false);
	}	
	
	public static boolean startsWithIgnoreCase(String string, String startsWith) {
		return startsWith(string, startsWith, true);
	}
	
	public static boolean startsWith(String str, String prefix, boolean ignoreCase) {
		if (str == null || prefix == null) {
			return str == null && prefix == null;
		}
		if (prefix.length() > str.length()) {
			return false;
		}
		return str.regionMatches(ignoreCase, 0, prefix, 0, prefix.length());
	}	
	
	public static boolean endsWith(String string, String endsWith) {
		return endsWith(string, endsWith, false);
	}

	public static boolean endsWithIgnoreCase(String string, String endsWith) {
		return endsWith(string, endsWith, true);
	}
	
	public static boolean endsWith(String str, String suffix, boolean ignoreCase) {
		if (str == null || suffix == null) {
			return str == null && suffix == null;
		}
		if (suffix.length() > str.length()) {
			return false;
		}
		int strOffset = str.length() - suffix.length();

		return str.regionMatches(ignoreCase, strOffset, suffix, 0,suffix.length());
	}	
	
	//contains and indexOf
	
	public static boolean contains(String string, char c){
		if(null == string){
			return false;
		}
		return string.indexOf(c) >= 0;
	}
	
	public static boolean contains(String string, String contains){
		return indexOf(string, contains) >= 0;
	}
	
	public static boolean containsWhitespaces(CharSequence cs) {
		if(null == cs) {
			return false;
		}
		for(int i=0;i<cs.length();i++) {
			if(Character.isWhitespace(cs.charAt(i))) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean containsIgnoreCase(String string,String contains){
		if(null == string || null == contains){
			return false;
		}
		int len = contains.length();
		int max = string.length() - len;
		for (int i = 0; i <= max; i++) {
			if (string.regionMatches(true, i, contains, 0, len)) {
				return true;
			}
		}		
		return false;
	}
	
	public static int indexOf(String string,String indexOf){
		if(null == string || null == indexOf){
			return Arrays2.INDEX_NOT_FOUND;
		}
		return string.indexOf(indexOf);
	}
	
	public static int indexOf(String string,String indexOf,int fromIndex){
		if(null == string || null == indexOf){
			return Arrays2.INDEX_NOT_FOUND;
		}
		return string.indexOf(indexOf,fromIndex);
	}
	
	public static int indexOfIgnoreCase(String string,String indexOf){
		return indexOfIgnoreCase(string, indexOf, 0);
	}
	
	public static int indexOfIgnoreCase(String string,String indexOf,int fromIndex){
		if (string == null || indexOf == null) {
			return Arrays2.INDEX_NOT_FOUND;
		}
		if (fromIndex < 0) {
			fromIndex = 0;
		}
		int endLimit = string.length() - indexOf.length() + 1;
		if (fromIndex > endLimit) {
			return Arrays2.INDEX_NOT_FOUND;
		}
		if (indexOf.length() == 0) {
			return fromIndex;
		}
		for (int i = fromIndex; i < endLimit; i++) {
			if (string.regionMatches(true, i, indexOf, 0, indexOf.length())){
				return i;
			}
		}
		return Arrays2.INDEX_NOT_FOUND;
	}
	
	public static int lastIndexOf(String string,String indexOf){
		if(null == string || null == indexOf){
			return Arrays2.INDEX_NOT_FOUND;
		}
		return string.lastIndexOf(indexOf);
	}
	
	public static int lastIndexOf(String string,String indexOf,int fromIndex){
		if(null == string || null == indexOf){
			return Arrays2.INDEX_NOT_FOUND;
		}
		return string.lastIndexOf(indexOf,fromIndex);
	}	
	
	public static int lastIndexOfIgnoreCase(String string, String indexOf) {
		return lastIndexOfIgnoreCase(string,indexOf,-1);
	}
	
    public static int lastIndexOfIgnoreCase(String string, String indexOf, int fromIndex) {
        if (string == null || indexOf == null) {
            return Arrays2.INDEX_NOT_FOUND;
        }
        if (fromIndex == -1 || fromIndex > string.length() - indexOf.length()) {
            fromIndex = string.length() - indexOf.length();
        }
        if (fromIndex < 0) {
            return Arrays2.INDEX_NOT_FOUND;
        }
        if (indexOf.length() == 0) {
            return fromIndex;
        }
        for (int i = fromIndex; i >= 0; i--) {
            if (string.regionMatches(true, i, indexOf, 0, indexOf.length())) {
                return i;
            }
        }
        return Arrays2.INDEX_NOT_FOUND;
    }
    
    //substring
    public static String substringBefore(String string,String indexOf) {
    	if(null == string || null == indexOf){
    		return EMPTY;
    	}
    	
    	int index = string.indexOf(indexOf);
    	if(index < 0){
    		return EMPTY;
    	}
    	
    	return string.substring(0,index); 
    }
    
    public static String substringAfter(String string,String indexOf) {
    	if(null == string || null == indexOf){
    		return EMPTY;
    	}
    	
    	int index = string.indexOf(indexOf);
    	if(index < 0){
    		return EMPTY;
    	}
    	
    	return string.substring(index+1); 
    }
	
	/**
	 * <p>
	 * Gets the leftmost {@code len} characters of a String.
	 * </p>
	 * 
	 * <p>
	 * If {@code len} characters are not available, or the String is {@code null}, the String will be returned without
	 * an exception. An empty String is returned if len is negative.
	 * </p>
	 * 
	 * <pre>
	 * Strings.left(null, *)    = ""
	 * Strings.left(*, -ve)     = ""
	 * Strings.left("", *)      = ""
	 * Strings.left("abc", 0)   = ""
	 * Strings.left("abc", 2)   = "ab"
	 * Strings.left("abc", 4)   = "abc"
	 * </pre>
	 * 
	 * @param string the String to get the leftmost characters from, may be null
	 * @param len the length of the required String
	 * @return the leftmost characters, "" if null String input
	 */
	public static String left(String string, int len) {
		if (string == null) {
			return EMPTY;
		}
		if (len < 0) {
			return EMPTY;
		}
		if (string.length() <= len) {
			return string;
		}
		return string.substring(0, len);
	}
	
    // Abbreviating
    //-----------------------------------------------------------------------
    /**
     * <p>Abbreviates a String using ellipses. This will turn
     * "Now is the time for all good men" into "Now is the time for..."</p>
     *
     * <p>Specifically:
     * <ul>
     *   <li>If {@code str} is less than {@code maxWidth} characters
     *       long, return it.</li>
     *   <li>Else abbreviate it to {@code (substring(str, 0, max-3) + "...")}.</li>
     *   <li>If {@code maxWidth} is less than {@code 4}, throw an
     *       {@code IllegalArgumentException}.</li>
     *   <li>In no case will it return a String of length greater than
     *       {@code maxWidth}.</li>
     * </ul>
     * </p>
     *
     * <pre>
     * Strings.abbreviate(null, *)      = null
     * Strings.abbreviate("", 4)        = ""
     * Strings.abbreviate("abcdefg", 6) = "abc..."
     * Strings.abbreviate("abcdefg", 7) = "abcdefg"
     * Strings.abbreviate("abcdefg", 8) = "abcdefg"
     * Strings.abbreviate("abcdefg", 4) = "a..."
     * Strings.abbreviate("abcdefg", 3) = IllegalArgumentException
     * </pre>
     *
     * @param str  the String to check, may be null
     * @param maxWidth  maximum length of result String, must be at least 4
     * @return abbreviated String, {@code null} if null String input
     * @throws IllegalArgumentException if the width is too small
     */
    public static String abbreviate(String str, int maxWidth) {
        return abbreviate(str, 0, maxWidth);
    }
    
    /**
     * <pre>
     * Strings.abbreviateMiddle(null,  0)      = ""
     * Strings.abbreviateMiddle("abc", 0)      = "abc"
     * Strings.abbreviateMiddle("abc", 0)      = "abc"
     * Strings.abbreviateMiddle("abc", ".", 3) = "abc"
     * Strings.abbreviateMiddle("abcdefghij", "...", 6)  = "ab...g"
     * </pre>
     */
    public static String abbreviateMiddle(String str, int maxWidth) {
        return abbreviateMiddle(str, "...", maxWidth);
    }

    /**
     * <p>Abbreviates a String using ellipses. This will turn
     * "Now is the time for all good men" into "...is the time for..."</p>
     *
     * <p>Works like {@code abbreviate(String, int)}, but allows you to specify
     * a "left edge" offset.  Note that this left edge is not necessarily going to
     * be the leftmost character in the result, or the first character following the
     * ellipses, but it will appear somewhere in the result.
     *
     * <p>In no case will it return a String of length greater than
     * {@code maxWidth}.</p>
     *
     * <pre>
     * Strings.abbreviate(null, *, *)                = null
     * Strings.abbreviate("", 0, 4)                  = ""
     * Strings.abbreviate("abcdefghijklmno", -1, 10) = "abcdefg..."
     * Strings.abbreviate("abcdefghijklmno", 0, 10)  = "abcdefg..."
     * Strings.abbreviate("abcdefghijklmno", 1, 10)  = "abcdefg..."
     * Strings.abbreviate("abcdefghijklmno", 4, 10)  = "abcdefg..."
     * Strings.abbreviate("abcdefghijklmno", 5, 10)  = "...fghi..."
     * Strings.abbreviate("abcdefghijklmno", 6, 10)  = "...ghij..."
     * Strings.abbreviate("abcdefghijklmno", 8, 10)  = "...ijklmno"
     * Strings.abbreviate("abcdefghijklmno", 10, 10) = "...ijklmno"
     * Strings.abbreviate("abcdefghijklmno", 12, 10) = "...ijklmno"
     * Strings.abbreviate("abcdefghij", 0, 3)        = IllegalArgumentException
     * Strings.abbreviate("abcdefghij", 5, 6)        = IllegalArgumentException
     * </pre>
     *
     * @param str  the String to check, may be null
     * @param offset  left edge of source String
     * @param maxWidth  maximum length of result String, must be at least 4
     * @return abbreviated String, {@code null} if null String input
     * @throws IllegalArgumentException if the width is too small
     */
    static String abbreviate(String str, int offset, int maxWidth) {
        if (str == null) {
            return null;
        }
        if (maxWidth < 4) {
            throw new IllegalArgumentException("Minimum abbreviation width is 4");
        }
        if (str.length() <= maxWidth) {
            return str;
        }
        if (offset > str.length()) {
            offset = str.length();
        }
        if (str.length() - offset < maxWidth - 3) {
            offset = str.length() - (maxWidth - 3);
        }
        final String abrevMarker = "...";
        if (offset <= 4) {
            return str.substring(0, maxWidth - 3) + abrevMarker;
        }
        if (maxWidth < 7) {
            throw new IllegalArgumentException("Minimum abbreviation width with offset is 7");
        }
        if (offset + maxWidth - 3 < str.length()) {
            return abrevMarker + abbreviate(str.substring(offset), maxWidth - 3);
        }
        return abrevMarker + str.substring(str.length() - (maxWidth - 3));
    }

    /**
     * <p>Abbreviates a String to the length passed, replacing the middle characters with the supplied
     * replacement String.</p>
     *
     * <p>This abbreviation only occurs if the following criteria is met:
     * <ul>
     * <li>Neither the String for abbreviation nor the replacement String are null or empty </li>
     * <li>The length to truncate to is less than the length of the supplied String</li>
     * <li>The length to truncate to is greater than 0</li>
     * <li>The abbreviated String will have enough room for the length supplied replacement String
     * and the first and last characters of the supplied String for abbreviation</li>
     * </ul>
     * Otherwise, the returned String will be the same as the supplied String for abbreviation.
     * </p>
     *
     * <pre>
     * Strings.abbreviateMiddle(null, null, 0)      = null
     * Strings.abbreviateMiddle("abc", null, 0)      = "abc"
     * Strings.abbreviateMiddle("abc", ".", 0)      = "abc"
     * Strings.abbreviateMiddle("abc", ".", 3)      = "abc"
     * Strings.abbreviateMiddle("abcdef", ".", 4)     = "ab.f"
     * </pre>
     *
     * @param str  the String to abbreviate, may be null
     * @param middle the String to replace the middle characters with, may be null
     * @param length the length to abbreviate {@code str} to.
     * @return the abbreviated String if the above criteria is met, or the original String supplied for abbreviation.
     */
    static String abbreviateMiddle(String str, String middle, int length) {
        if (isEmpty(str) || isEmpty(middle)) {
            return str;
        }

        if (length >= str.length() || length < middle.length()+2) {
            return str;
        }

        int targetSting = length-middle.length();
        int startOffset = targetSting/2+targetSting%2;
        int endOffset = str.length()-targetSting/2;

        StringBuilder builder = new StringBuilder(length);
        builder.append(str.substring(0,startOffset));
        builder.append(middle);
        builder.append(str.substring(endOffset));

        return builder.toString();
    }   	
	
    /**
     * <p>Returns padding using the specified delimiter repeated
     * to a given length.</p>
     *
     * <pre>
     * Strings.repeat(0, 'e')  = ""
     * Strings.repeat(3, 'e')  = "eee"
     * Strings.repeat(-2, 'e') = ""
     * </pre>
     *
     * <p>Note: this method doesn't not support padding with
     * <a href="http://www.unicode.org/glossary/#supplementary_character">Unicode Supplementary Characters</a>
     * as they require a pair of {@code char}s to be represented.
     * If you are needing to support full I18N of your applications
     * consider using {@link #repeat(String, int)} instead.
     * </p>
     *
     * @param ch  character to repeat
     * @param repeat  number of times to repeat char, negative treated as zero
     * @return String with repeated character
     * @see #repeat(String, int)
     */
    public static String repeat(char ch, int repeat) {
        char[] buf = new char[repeat];
        for (int i = repeat - 1; i >= 0; i--) {
            buf[i] = ch;
        }
        return new String(buf);
    }
	
	/**
	 * Returns a string consisting of a specific number of concatenated copies of an input string. For example,
	 * {@code repeat("hey", 3)} returns the string {@code "heyheyhey"}.
	 * 
	 * @param string any string, a <code>null</code> string return ""
	 * @param count the number of times to repeat it; a nonnegative integer
	 * @return a string containing {@code string} repeated {@code count} times (the empty string if {@code count} is
	 *         zero)
	 * @throws IllegalArgumentException if {@code count} is negative
	 */
	//from google guava Strings.java
	public static String repeat(String string, int count) {
		if (null == string) {
			return EMPTY;
		}

		if (count <= 1) {
			Args.assertTrue(count >= 0, "invalid count: " + count);
			return (count == 0) ? "" : string;
		}

		// IF YOU MODIFY THE CODE HERE, you must update StringsRepeatBenchmark
		final int len = string.length();
		final long longSize = (long) len * (long) count;
		final int size = (int) longSize;
		if (size != longSize) {
			throw new ArrayIndexOutOfBoundsException("Required array size too large: " + String.valueOf(longSize));
		}

		final char[] array = new char[size];
		string.getChars(0, len, array, 0);
		int n;
		for (n = len; n < size - n; n <<= 1) {
			System.arraycopy(array, 0, array, n, n);
		}
		System.arraycopy(array, 0, array, n, size - n);
		return new String(array);
	}
	
	//is number
	/**
	 * <p>
	 * Checks if the string contains only Unicode digits. 
	 * A decimal point is not a Unicode digit and returns false.
	 * </p>
	 * 
	 * <p>
	 * {@code null} will return {@code false}. An empty string will return {@code false}.
	 * </p>
	 * 
	 * <pre>
	 * Strings.isDigits(null)   = false
	 * Strings.isDigits("")     = false
	 * Strings.isDigits("  ")   = false
	 * Strings.isDigits("123")  = true
	 * Strings.isDigits("12 3") = false
	 * Strings.isDigits("ab2c") = false
	 * Strings.isDigits("12-3") = false
	 * Strings.isDigits("12.3") = false
	 * </pre>
	 * 
	 * @param cs the CharSequence to check, may be null
	 * 
	 * @return {@code true} if only contains digits, and is non-null
	 * 
	 * @see Character#isDigit(char)
	 */
	public static boolean isDigits(String cs) {
		if (cs == null || cs.length() == 0) {
			return false;
		}
		int sz = cs.length();
		for (int i = 0; i < sz; i++) {
			if (Character.isDigit(cs.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}	
	
	public static boolean isLetters(String s){
		if(s == null || s.length() == 0){
			return false;
		}
		
		int len = s.length();
		for(int i=0;i<len;i++){
			if(!Character.isLetter(s.charAt(i))){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * <p>
	 * Checks whether the String a valid Java number.
	 * </p>
	 * 
	 * <p>
	 * Valid numbers include hexadecimal marked with the <code>0x</code> qualifier, scientific notation and numbers marked with a
	 * type qualifier (e.g. 123L).
	 * </p>
	 * 
	 * <p>
	 * <code>Null</code> and empty String will return <code>false</code>.
	 * </p>
	 * 
	 * @param str the <code>String</code> to check
	 * @return <code>true</code> if the string is a correctly formatted number
	 */
    public static boolean isNumber(String str) {
        if (isEmpty(str)) {
            return false;
        }
        char[] chars = str.toCharArray();
        int sz = chars.length;
        boolean hasExp = false;
        boolean hasDecPoint = false;
        boolean allowSigns = false;
        boolean foundDigit = false;
        // deal with any possible sign up front
        int start = (chars[0] == '-') ? 1 : 0;
        if (sz > start + 1 && chars[start] == '0' && chars[start + 1] == 'x') {
            int i = start + 2;
            if (i == sz) {
                return false; // str == "0x"
            }
            // checking hex (it can't be anything else)
            for (; i < chars.length; i++) {
                if ((chars[i] < '0' || chars[i] > '9')
                    && (chars[i] < 'a' || chars[i] > 'f')
                    && (chars[i] < 'A' || chars[i] > 'F')) {
                    return false;
                }
            }
            return true;
        }
        sz--; // don't want to loop to the last char, check it afterwords
              // for type qualifiers
        int i = start;
        // loop to the next to last char or to the last char if we need another digit to
        // make a valid number (e.g. chars[0..5] = "1234E")
        while (i < sz || (i < sz + 1 && allowSigns && !foundDigit)) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                foundDigit = true;
                allowSigns = false;

            } else if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent   
                    return false;
                }
                hasDecPoint = true;
            } else if (chars[i] == 'e' || chars[i] == 'E') {
                // we've already taken care of hex.
                if (hasExp) {
                    // two E's
                    return false;
                }
                if (!foundDigit) {
                    return false;
                }
                hasExp = true;
                allowSigns = true;
            } else if (chars[i] == '+' || chars[i] == '-') {
                if (!allowSigns) {
                    return false;
                }
                allowSigns = false;
                foundDigit = false; // we need a digit after the E
            } else {
                return false;
            }
            i++;
        }
        if (i < chars.length) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                // no type qualifier, OK
                return true;
            }
            if (chars[i] == 'e' || chars[i] == 'E') {
                // can't have an E at the last byte
                return false;
            }
            if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                // single trailing decimal point after non-exponent is ok
                return foundDigit;
            }
            if (!allowSigns
                && (chars[i] == 'd'
                    || chars[i] == 'D'
                    || chars[i] == 'f'
                    || chars[i] == 'F')) {
                return foundDigit;
            }
            if (chars[i] == 'l'
                || chars[i] == 'L') {
                // not allowing L with an exponent or decimal point
                return foundDigit && !hasExp && !hasDecPoint;
            }
            // last character is illegal
            return false;
        }
        // allowSigns is true iff the val ends in 'E'
        // found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
        return !allowSigns && foundDigit;
    }	
	
	//new string
	
	public static String newString(byte[] bytes, String charsetName) {
		if (bytes == null) {
			return Strings.EMPTY;
		}
		try {
			return new String(bytes, charsetName);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}	

	public static String newStringIso8859_1(byte[] bytes) {
		return newString(bytes, Charsets.ISO_8859_1.name());
	}

	public static String newStringUsAscii(byte[] bytes) {
		return newString(bytes, Charsets.US_ASCII.name());
	}	

	public static String newStringUtf8(byte[] bytes) {
		return newString(bytes, Charsets.UTF_8.name());
	}
	
	//get bytes
	public static byte[] getBytes(String str, String charset) {
		if (isEmpty(str)) {
			return Arrays2.EMPTY_BYTE_ARRAY;
		}

		try {
			return str.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	public static byte[] getBytesIso8859_1(String string) {
		return getBytes(string, Charsets.ISO_8859_1.name());
	}

	public static byte[] getBytesUsAscii(String string) {
		return getBytes(string, Charsets.US_ASCII.name());
	}

	public static byte[] getBytesUtf8(String string) {
		return getBytes(string, Charsets.UTF_8.name());
	}	
	
	//-----------------private methods-------------------------------------------------------------------------------------------------------------
	
	private static String[] splitWorker(String str,int max, boolean preserveAllTokens,boolean trimTokens,boolean ignoreEmptyTokens,char... chars) {
		// Performance tuned for 2.0 (JDK1.4)
		// Direct code is quicker than StringTokenizer.
		// Also, StringTokenizer uses isSpace() not isWhitespace()

		if (str == null) {
			return Arrays2.EMPTY_STRING_ARRAY;
		}

		if (null == chars || chars.length == 0) {
			chars = DEFAULT_SPLIT_CHARS;
		}

		int len = str.length();
		if (len == 0) {
			return Arrays2.EMPTY_STRING_ARRAY;
		}

		List<String> list = new ArrayList<String>();
		int sizePlus1 = 1;
		int i = 0, start = 0;
		boolean match = false;
		boolean lastMatch = false;

		//		if (separatorChars == null) {
		//			// Null separator means use whitespace
		//			while (i < len) {
		//				if (Character.isWhitespace(str.charAt(i))) {
		//					if (match || preserveAllTokens) {
		//						lastMatch = true;
		//						if (sizePlus1++ == max) {
		//							i = len;
		//							lastMatch = false;
		//						}
		//						
		//						String token = str.substring(start, i);
		//						
		//						if(trimTokens){
		//							token = trim(token);
		//						}
		//						
		//						if(!ignoreEmptyTokens || token.length() > 0){
		//							list.add(token);
		//						}
		//						
		//						match = false;
		//					}
		//					start = ++i;
		//					continue;
		//				}
		//				lastMatch = false;
		//				match = true;
		//				i++;
		//			}
		//		} else
		if (chars.length == 1) {
			// Optimise 1 character case
			char sep = chars[0];
			while (i < len) {
				if (str.charAt(i) == sep) {
					if (match || preserveAllTokens) {
						lastMatch = true;
						if (sizePlus1++ == max) {
							i = len;
							lastMatch = false;
						}
						String token = str.substring(start, i);

						if (trimTokens) {
							token = trim(token);
						}

						if (!ignoreEmptyTokens || token.length() > 0) {
							list.add(token);
						}
						match = false;
					}
					start = ++i;
					continue;
				}
				lastMatch = false;
				match = true;
				i++;
			}
		} else {
			// standard case
			while (i < len) {
				if (Arrays2.indexOf(chars, str.charAt(i)) >= 0) {
					if (match || preserveAllTokens) {
						lastMatch = true;
						if (sizePlus1++ == max) {
							i = len;
							lastMatch = false;
						}
						String token = str.substring(start, i);

						if (trimTokens) {
							token = trim(token);
						}

						if (!ignoreEmptyTokens || token.length() > 0) {
							list.add(token);
						}
						match = false;
					}
					start = ++i;
					continue;
				}
				lastMatch = false;
				match = true;
				i++;
			}
		}
		if (match || preserveAllTokens && lastMatch) {
			String token = str.substring(start, i);

			if (trimTokens) {
				token = trim(token);
			}

			if (!ignoreEmptyTokens || token.length() > 0) {
				list.add(token);
			}
		}
		return list.toArray(new String[list.size()]);
	}	
	
	private static String[] splitByWholeSeparatorWorker(String str, String separator, int max, boolean preserveAllTokens, boolean trimTokens,
	        boolean ignoreEmptyTokens) {
		if (str == null) {
			return null;
		}

		int len = str.length();

		if (len == 0) {
			return Arrays2.EMPTY_STRING_ARRAY;
		}

		if (separator == null || EMPTY.equals(separator)) {
			// Split on whitespace.
			return splitWorker(str, max, preserveAllTokens, trimTokens, ignoreEmptyTokens);
		}

		int separatorLength = separator.length();

		ArrayList<String> substrings = new ArrayList<String>();
		int numberOfSubstrings = 0;
		int beg = 0;
		int end = 0;

		while (end < len) {
			end = str.indexOf(separator, beg);

			String token = null;

			if (end > -1) {
				if (end > beg) {
					numberOfSubstrings += 1;

					if (numberOfSubstrings == max) {
						end = len;

						token = str.substring(beg);
					} else {
						// The following is OK, because String.substring( beg, end ) excludes
						// the character at the position 'end'.
						token = str.substring(beg, end);

						// Set the starting point for the next search.
						// The following is equivalent to beg = end + (separatorLength - 1) + 1,
						// which is the right calculation:
						beg = end + separatorLength;
					}
				} else {
					// We found a consecutive occurrence of the separator, so skip it.
					if (preserveAllTokens) {
						numberOfSubstrings += 1;
						if (numberOfSubstrings == max) {
							end = len;
							token = str.substring(beg);
						} else {
							token = EMPTY;
						}
					}
					beg = end + separatorLength;
				}
			} else {
				// String.substring( beg ) goes from 'beg' to the end of the String.
				token = str.substring(beg);
				end = len;
			}

			if (null != token) {
				if (trimTokens) {
					token = trim(token);
				}

				if (!ignoreEmptyTokens || token.length() > 0) {
					substrings.add(token);
				}
			}
		}

		return substrings.toArray(new String[substrings.size()]);
	}	
	
	private static String replace(String text, String oldString, String newString, int max) {
		if (isEmpty(text) || isEmpty(oldString) || newString == null || max == 0) {
			return nullToEmpty(text);
		}
		int start = 0;
		int end = text.indexOf(oldString, start);
		if (end == -1) {
			return text;
		}
		int replLength = oldString.length();
		int increase = newString.length() - replLength;
		increase = increase < 0 ? 0 : increase;
		increase *= max < 0 ? 16 : max > 64 ? 64 : max;
		StringBuilder buf = new StringBuilder(text.length() + increase);
		while (end != -1) {
			buf.append(text.substring(start, end)).append(newString);
			start = end + replLength;
			if (--max == 0) {
				break;
			}
			end = text.indexOf(oldString, start);
		}
		buf.append(text.substring(start));
		return buf.toString();
	}
	
	private static String replaceIgnoreCase(String text, String oldString, String newString, int max) {
		if (isEmpty(text) || isEmpty(oldString) || newString == null || max == 0) {
			return nullToEmpty(text);
		}
		
		String lcText      = text.toLowerCase();
		String lcOldString = oldString.toLowerCase();
		
		int start = 0;
		int end = lcText.indexOf(lcOldString, start);
		if (end == -1) {
			return lcText;
		}
		int replLength = lcOldString.length();
		int increase = newString.length() - replLength;
		increase = increase < 0 ? 0 : increase;
		increase *= max < 0 ? 16 : max > 64 ? 64 : max;
		StringBuilder buf = new StringBuilder(lcText.length() + increase);
		while (end != -1) {
			buf.append(text.substring(start, end)).append(newString);
			start = end + replLength;
			if (--max == 0) {
				break;
			}
			end = lcText.indexOf(lcOldString, start);
		}
		buf.append(text.substring(start));
		return buf.toString();
	}	
	
	protected Strings(){
		
	}
}
