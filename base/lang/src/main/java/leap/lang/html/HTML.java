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
package leap.lang.html;

import java.io.IOException;


//some codes from spring framework.
public class HTML {
	
	/**
	 * Shared instance of pre-parsed HTML character entity references.
	 */
	private static final HtmlCharacterEntityReferences characterEntityReferences =
			new HtmlCharacterEntityReferences();
	
	/**
	 * Turn special characters into HTML character references.
	 * Handles complete character set defined in HTML 4.01 recommendation.
	 * <p>Escapes all special characters to their corresponding
	 * entity reference (e.g. {@code &lt;}).
	 * <p>Reference:
	 * <a href="http://www.w3.org/TR/html4/sgml/entities.html">
	 * http://www.w3.org/TR/html4/sgml/entities.html
	 * </a>
	 * @param input the (unescaped) input string
	 * @return the escaped string
	 */
	public static String escape(CharSequence input) {
		if (input == null) {
			return null;
		}
		StringBuilder escaped = new StringBuilder(input.length() * 2);
		for (int i = 0; i < input.length(); i++) {
			char character = input.charAt(i);
			String reference = characterEntityReferences.convertToReference(character);
			if (reference != null) {
				escaped.append(reference);
			}
			else {
				escaped.append(character);
			}
		}
		return escaped.toString();
	}
	
	/**
	 * Return the escaped string mapped to the given character or {@code null}.
	 */
	public static String escape(char c){
		return characterEntityReferences.convertToReference(c);
	}
	
	/**
	 * Appends the original char or escaped string to the given {@link Appendable}.
	 */
	public static void escapeAndAppend(char c, Appendable out) throws IOException {
		String s = characterEntityReferences.convertToReference(c);
		if(null == s){
			out.append(c);
		}else{
			out.append(s);
		}
	}
	
	/**
	 * Appends the original {@link CharSequence} or escaped string to the given {@link Appendable}.
	 */
	public static void escapeAndAppend(CharSequence cs, Appendable out) throws IOException {
		if(null == cs){
			return;
		}
        for(int i=0;i<cs.length();i++){
        	char c = cs.charAt(i);
        	String s = characterEntityReferences.convertToReference(c);
        	if(null == s){
        		out.append(c);
        	}else{
        		out.append(s);
        	}
        }
	}
	
	/**
	 * Turn HTML character references into their plain text UNICODE equivalent.
	 * <p>Handles complete character set defined in HTML 4.01 recommendation
	 * and all reference types (decimal, hex, and entity).
	 * <p>Correctly converts the following formats:
	 * <blockquote>
	 * &amp;#<i>Entity</i>; - <i>(Example: &amp;amp;) case sensitive</i>
	 * &amp;#<i>Decimal</i>; - <i>(Example: &amp;#68;)</i><br>
	 * &amp;#x<i>Hex</i>; - <i>(Example: &amp;#xE5;) case insensitive</i><br>
	 * </blockquote>
	 * Gracefully handles malformed character references by copying original
	 * characters as is when encountered.<p>
	 * <p>Reference:
	 * <a href="http://www.w3.org/TR/html4/sgml/entities.html">
	 * http://www.w3.org/TR/html4/sgml/entities.html
	 * </a>
	 * @param input the (escaped) input string
	 * @return the unescaped string
	 */
	public static String unescape(String input) {
		if (input == null) {
			return null;
		}
		return new HtmlCharacterEntityDecoder(characterEntityReferences, input).decode();
	}
	
	protected HTML(){
		
	}
}
