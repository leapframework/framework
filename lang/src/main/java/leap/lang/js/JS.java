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
package leap.lang.js;

import java.io.IOException;

import leap.lang.Exceptions;

public class JS {

	/**
	 * Turn JavaScript special characters into escaped characters.
	 *
	 * @param input the input string
	 * @return the string with escaped characters
	 */
	//from spring framework : JavaScriptUtils
	public static String escape(CharSequence input) {
		if (input == null) {
			return null;
		}

		StringBuilder filtered = new StringBuilder(input.length());

		try {
	        escapeAndAppend(input, filtered);
        } catch (IOException e) {
        	throw Exceptions.wrap(e);
        }
		
		return filtered.toString();
	}
	
	public static void escapeAndAppend(CharSequence input,Appendable out) throws IOException {
		if (input == null) {
			return;
		}

		char prevChar = '\u0000';
		char c;
		
        for (int i = 0; i < input.length(); i++) {
        	c = input.charAt(i);
        	escape(prevChar, c, out);
        	prevChar = c;
        }
	}
	
	public static void escapeAndAppend(char c,Appendable out) throws IOException {
		escape('\u0000',c,out);
	}
	
	private static void escape(char prevChar, char c,Appendable out) throws IOException {
		if (c == '"') {
			out.append("\\\"");
		}
		else if (c == '\'') {
			out.append("\\'");
		}
		else if (c == '\\') {
			out.append("\\\\");
		}
		else if (c == '/') {
			out.append("\\/");
		}
		else if (c == '\t') {
			out.append("\\t");
		}
		else if (c == '\n') {
			if (prevChar != '\r') {
				out.append("\\n");
			}
		}
		else if (c == '\r') {
			out.append("\\n");
		}
		else if (c == '\f') {
			out.append("\\f");
		}
		else if (c == '\b') {
			out.append("\\b");
		}
		// No '\v' in Java, use octal value for VT ascii char
		else if (c == '\013') {
			out.append("\\v");
		}
		else if (c == '<') {
			out.append("\\u003C");
		}
		else if (c == '>') {
			out.append("\\u003E");
		}
		// Unicode for PS (line terminator in ECMA-262)
		else if (c == '\u2028') {
			out.append("\\u2028");
		}
		// Unicode for LS (line terminator in ECMA-262)
		else if (c == '\u2029') {
			out.append("\\u2029");
		}
		else {
			out.append(c);
		}
	}
	
	public static boolean isValidJavascriptFunction(String f) {
		for(int i=0;i<f.length();i++){
			if(!Character.isJavaIdentifierPart(f.charAt(i))){
				return false;
			}
		}
		return true;
	}
	
	protected JS(){
		
	}
}
