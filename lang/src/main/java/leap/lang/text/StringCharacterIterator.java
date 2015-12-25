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

public final class StringCharacterIterator {
    private String s;
    private int    pos;
    private char   curr;
    private int    upperBound;

    public StringCharacterIterator(String s) {
        this.s          = s;
        this.pos        = -1;
        this.upperBound = s.length() - 1;
    }

    public String getInput() {
        return s;
    }
    
    public boolean isEnd(){
    	return pos == upperBound;
    }
    
    public boolean hasNext() {
        return pos < upperBound;
    }

    public boolean next() {
        if (!hasNext()) {
            return false;
        }
        curr = s.charAt(++pos);
        return true;
    }
    
    public char nextChar(){
    	if(!hasNext()){
    		throw new IllegalStateException("No next char");
    	}
    	curr = s.charAt(++pos);
    	return curr;
    }
    
    public char charAt(int index) throws IndexOutOfBoundsException {
    	return s.charAt(index);
    }
    
    public char peek() throws IllegalStateException {
        if (pos == upperBound) {
            throw new IllegalStateException("cannot peek next character,current position is at the end of input string");
        }
        return s.charAt(pos + 1);
    }

    public int pos() {
        return pos;
    }
    
    public int upperBound(){
    	return upperBound;
    }
    
    public String substring(int indexFrom,int indexTo,boolean trim) throws IndexOutOfBoundsException {
    	return trim ? s.substring(indexFrom, indexTo).trim() : s.substring(indexFrom, indexTo);
    }
    
    public char nextNonWhitespaceChar() throws IllegalStateException{
    	do{
    		if(!next()){
    			throw new IllegalStateException("cannot next to non white space character, position is end");
    		}
    	}while(Character.isWhitespace(curr));
    	
    	return curr;
    }

    public void move(int newPosition) throws IndexOutOfBoundsException{
        if (newPosition > upperBound || newPosition < 0) {
            throw new IndexOutOfBoundsException("The new position " + newPosition + " is out of string's range.");
        }
        this.pos  = newPosition;
        this.curr = s.charAt(pos);
    }

    public char current() throws IllegalStateException {
        if (pos == -1) {
            throw new IllegalStateException("no current character, this iterator not used yet");
        }
        return curr;
    }
}
