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

public class Chars {
	
	public static final char COMMA      = ',';
	public static final char WHITESPACE = ' ';
	
	public static String substring(char[] chars, int start, int end) throws IndexOutOfBoundsException {
		if(null == chars){
			return Strings.EMPTY;
		}
		
        if (start < 0) {
            throw new IndexOutOfBoundsException("Negative start index : " + start);
        }
        
        if (end > chars.length) {
            throw new IndexOutOfBoundsException("end index : " + end + " > length : " + chars.length);
        }
        
        if (start > end) { 
            throw new IndexOutOfBoundsException("start index : " + start + " > end index : " + end);
        }
        
        return new String(chars, start, end - start);
	}

	protected Chars(){
		
	}
}