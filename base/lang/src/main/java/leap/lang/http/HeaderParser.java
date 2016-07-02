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
package leap.lang.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import leap.lang.Arrays2;
import leap.lang.Chars;
import leap.lang.http.Header.HeaderElement;
import leap.lang.value.ImmutableNamedValue;
import leap.lang.value.NamedValue;

final class HeaderParser {

	private final static char	PARAM_DELIMITER	= ';';
	private final static char	ELEM_DELIMITER	= ',';
	private final static char[]	ALL_DELIMITERS	= new char[] { PARAM_DELIMITER, ELEM_DELIMITER };
	
	static List<HeaderElement> parseElements(String value){
		return parseElements(value.toCharArray(),new Cursor(0,value.length()));
	}
	
	static List<HeaderElement> parseElements(final char[] buffer, final Cursor cursor) {

		if (buffer == null) {
			throw new IllegalArgumentException("Char array buffer may not be null");
		}
		if (cursor == null) {
			throw new IllegalArgumentException("Parser cursor may not be null");
		}

		List<HeaderElement> elements = new ArrayList<HeaderElement>();
		while (!cursor.atEnd()) {
			HeaderElement element = parseHeaderElement(buffer, cursor);
			if (!(element.getName().length() == 0 && element.getValue() == null)) {
				elements.add(element);
			}
		}
		return elements;
	}
	
	private static HeaderElement parseHeaderElement(final char[] buffer, final Cursor cursor) {

		if (buffer == null) {
			throw new IllegalArgumentException("Char array buffer may not be null");
		}
		if (cursor == null) {
			throw new IllegalArgumentException("Parser cursor may not be null");
		}

		NamedValue<String> nvp = parseNameValuePair(buffer, cursor,ALL_DELIMITERS);
		
		Map<String, String> params = null;
		
		if (!cursor.atEnd()) {
			char ch = buffer[cursor.pos - 1];
			if (ch != ELEM_DELIMITER) {
				params = parseParameters(buffer, cursor);
			}
		}
		return new HeaderElement(nvp.getName(), nvp.getValue(), params);
	}

	private static NamedValue<String> parseNameValuePair(final char[] buffer, final Cursor cursor, final char[] delimiters) {

		if (buffer == null) {
			throw new IllegalArgumentException("Char array buffer may not be null");
		}
		if (cursor == null) {
			throw new IllegalArgumentException("Parser cursor may not be null");
		}

		boolean terminated = false;

		int pos = cursor.pos;
		int indexFrom = cursor.pos;
		int indexTo = cursor.upperBound;

		// Find name
		String name = null;
		while (pos < indexTo) {
			char ch = buffer[pos];
			if (ch == '=') {
				break;
			}
			if (Arrays2.contains(delimiters, ch)) {
				terminated = true;
				break;
			}
			pos++;
		}

		if (pos == indexTo) {
			terminated = true;
			name = Chars.substring(buffer,indexFrom, indexTo).trim();
		} else {
			name = Chars.substring(buffer,indexFrom, pos).trim();
			pos++;
		}

		if (terminated) {
			cursor.updatePos(pos);
			return ImmutableNamedValue.of(name, null);
		}

		// Find value
		String value = null;
		int i1 = pos;

		boolean qouted = false;
		boolean escaped = false;
		while (pos < indexTo) {
			char ch = buffer[pos];
			if (ch == '"' && !escaped) {
				qouted = !qouted;
			}
			if (!qouted && !escaped && Arrays2.contains(delimiters,ch)) {
				terminated = true;
				break;
			}
			if (escaped) {
				escaped = false;
			} else {
				escaped = qouted && ch == '\\';
			}
			pos++;
		}

		int i2 = pos;
		// Trim leading white spaces
		while (i1 < i2 && (Character.isWhitespace(buffer[i1]))) {
			i1++;
		}
		// Trim trailing white spaces
		while ((i2 > i1) && (Character.isWhitespace(buffer[i2 - 1]))) {
			i2--;
		}
		// Strip away quotes if necessary
		if (((i2 - i1) >= 2) && (buffer[i1] == '"') && (buffer[i2 - 1] == '"')) {
			i1++;
			i2--;
		}
		value = Chars.substring(buffer,i1, i2);
		if (terminated) {
			pos++;
		}
		cursor.updatePos(pos);
		return ImmutableNamedValue.of(name, value);
	}
	
	private static Map<String, String> parseParameters(final char[] buffer, final Cursor cursor) {

		int pos = cursor.pos;
		int indexTo = cursor.upperBound;

		while (pos < indexTo) {
			char ch = buffer[pos];
			if (Character.isWhitespace(ch)) {
				pos++;
			} else {
				break;
			}
		}
		
		cursor.updatePos(pos);
		if (cursor.atEnd()) {
			return new HashMap<String, String>();
		}

		Map<String, String> params = HeaderElement.createParametersMap(null);
		
		while (!cursor.atEnd()) {
			NamedValue<String> param = parseNameValuePair(buffer, cursor,ALL_DELIMITERS);
			params.put(param.getName(), param.getValue());
			char ch = buffer[cursor.pos - 1];
			if (ch == ELEM_DELIMITER) {
				break;
			}
		}

		return params;
	}
	
	private static class Cursor {
	    private final int lowerBound;
	    private final int upperBound;
	    private int pos;

	    public Cursor(int lowerBound, int upperBound) {
	        if (lowerBound < 0) {
	            throw new IndexOutOfBoundsException("Lower bound cannot be negative");
	        }
	        if (lowerBound > upperBound) {
	            throw new IndexOutOfBoundsException("Lower bound cannot be greater then upper bound");
	        }
	        this.lowerBound = lowerBound;
	        this.upperBound = upperBound;
	        this.pos = lowerBound;
	    }

	    public void updatePos(int pos) {
	        if (pos < this.lowerBound) {
	            throw new IndexOutOfBoundsException("pos: "+pos+" < lowerBound: "+this.lowerBound);
	        }
	        if (pos > this.upperBound) {
	            throw new IndexOutOfBoundsException("pos: "+pos+" > upperBound: "+this.upperBound);
	        }
	        this.pos = pos;
	    }

	    public boolean atEnd() {
	        return this.pos >= this.upperBound;
	    }
	}	
}
