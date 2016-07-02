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
package leap.lang.codec;

import java.util.Arrays;

public class Hex {
	
    public final static char[] HEX_DIGITS = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };
	
    private static final int[] HEX_CHARS = initHexChars();

    private static int[] initHexChars() {
        int[] chars = new int[0x80];
        Arrays.fill(chars, -1);

        for (char c = '0'; c <= '9'; c++) {
        	chars[c] = c - '0';
        }
        for (char c = 'A'; c <= 'F'; c++) {
        	chars[c] = c - 'A' + 10;
        }
        for (char c = 'a'; c <= 'f'; c++) {
        	chars[c] = c - 'a' + 10;
        }
        return chars;
    }	
    
    /**
     * Checks is the given character is hexadecimal character.
     */
    public static boolean isHexCharacter(char c){
    	return c < 128 && HEX_CHARS[c] != -1;
    }
	
	public static String encode(byte[] data){
		return HexCommonsImpl.encodeHexString(data);
	}
	
	public static String encode(byte[] data,boolean toLowerCase){
		return new String(HexCommonsImpl.encodeHex(data, toLowerCase));
	}
	
	public static char[] encodeToChars(byte[] data){
		return HexCommonsImpl.encodeHex(data);
	}
	
	public static char[] encodeToChars(byte[] data,boolean toLowerCase){
		return HexCommonsImpl.encodeHex(data,toLowerCase);
	}	
	
	public static byte[] decode(String hex){
		return HexCommonsImpl.decodeHex(hex.toCharArray());
	}
	
	public static byte[] decode(char[] hex){
		return HexCommonsImpl.decodeHex(hex);
	}
	
	protected Hex() {
		
	}
}