/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package leap.lang.text;

public abstract class AbstractStringParser {

    protected static final char EOI = 0x1A;

    protected final StringBuilder chars;

    protected char ch;
    protected int  pos;

    public AbstractStringParser(String expr) {
        this.chars = new StringBuilder(expr.trim());
        this.pos   = -1;
    }

    protected final boolean eof() {
        return ch == EOI;
    }

    protected final boolean isWhitespace() {
        return Character.isWhitespace(ch);
    }

    protected final char charAt(int index) {
        return index < chars.length() ? chars.charAt(index) : EOI;
    }

    protected final void nextChar(){
        ch = charAt(++pos);
    }

    protected final String substring(int start,int end){
        return chars.substring(start,end);
    }

    protected final void skipWhitespaces(){
        for(;;){
            if(!Character.isWhitespace(ch)){
                break;
            }
            if(eof()) {
                error("Unexpected eof");
            }
            nextChar();
        }
    }

    protected void error(String message) {
        throw new IllegalStateException(message + ", " + describePosition());
    }

    protected final String describePosition(){
        return describePosition(pos);
    }

    protected final String describePosition(int pos){
        int fromIndex;
        int endIndex;

        if(pos > chars.length() - 5){
            fromIndex = Math.max(pos - 15, 0);
            endIndex  = chars.length()-1;
        }else{
            fromIndex = pos;
            endIndex  = Math.min(pos + 20, chars.length() - 1);
        }

        StringBuilder sb = new StringBuilder();

        sb.append("pos ").append(pos).append(", \" ");
        sb.append(substring(fromIndex,endIndex));
        if(endIndex < chars.length() - 1){
            sb.append("...");
        }
        sb.append(" \"");

        return sb.toString();
    }

    private final static boolean[] identifierFlags = new boolean[256];
    static {
        for (char c = 0; c < identifierFlags.length; ++c) {
            if (c >= 'A' && c <= 'Z') {
                identifierFlags[c] = true;
            } else if (c >= 'a' && c <= 'z') {
                identifierFlags[c] = true;
            } else if (c >= '0' && c <= '9') {
                identifierFlags[c] = true;
            }
        }
        identifierFlags['_'] = true;
        identifierFlags['$'] = true;
    }

    public static boolean isIdentifierChar(char c) {
        return c > identifierFlags.length || identifierFlags[c];
    }

}
