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

import leap.lang.Arrays2;
import leap.lang.Strings;

public class Base64 {

	/**
	 * Byte used to pad output.
	 */
	static final byte PAD_DEFAULT = '=';

	/**
	 * This array is a lookup table that translates Unicode characters drawn from the "Base64 Alphabet" (as specified in
	 * Table 1 of RFC 2045) into their 6-bit positive integer equivalents. Characters that are not in the Base64
	 * alphabet but fall within the bounds of the array are translated to -1.
	 * 
	 * Note: '+' and '-' both decode to 62. '/' and '_' both decode to 63. This means decoder seamlessly handles both
	 * URL_SAFE and STANDARD base64. (The encoder, on the other hand, needs to know ahead of time what to emit).
	 * 
	 * Thanks to "commons" project in ws.apache.org for this code.
	 * http://svn.apache.org/repos/asf/webservices/commons/trunk/modules/util/
	 */
	static final byte[]	DECODE_TABLE	= { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
	        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, 62, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1,
	        -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, 63, -1,
	        26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51 };

	/**
	 * Encodes binary data using the base64 algorithm but does not chunk the output.
	 * 
	 * @param binaryData binary data to encode
	 * 
	 * @return String containing Base64 characters.
	 */
	public static String encode(byte[] data) {
		if (null == data) {
			return Strings.EMPTY;
		}
		return Strings.newStringUtf8(encodeToBytes(data));
	}
	
	public static String urlEncode(byte[] data) {
		if(null == data) {
			return Strings.EMPTY;
		}
		return Strings.newStringUtf8(urlEncodeToBytes(data));
	}

	/**
	 * Encodes binary data using the base64 algorithm but does not chunk the output.
	 * 
	 * @param binaryData binary data to encode
	 * 
	 * @return String containing Base64 characters.
	 */
	public static String encode(String data) {
		if (null == data) {
			return Strings.EMPTY;
		}
		return Strings.newStringUtf8(encodeToBytes(data));
	}
	
	public static String urlEncode(String data) {
		if (null == data) {
			return Strings.EMPTY;
		}
		return Strings.newStringUtf8(urlEncodeToBytes(data));
	}
	
	/**
	 * Encodes binary data using the base64 algorithm, optionally chunking the output into 76 character blocks.
	 * 
	 * @param data Array containing binary data to encode.
	 * 
	 * @param isChunked if <code>true</code> this encoder will chunk the base64 output into 76 character blocks.
	 * 
	 * @return String containg Base64 characters.
	 */	
	/*
	private static String encode(byte[] data, boolean isChunked) {
		if (null == data) {
			return Strings.EMPTY;
		}
		return Strings.newStringUtf8(encodeToBytes(data, isChunked));
	}
	*/

	/**
	 * Encodes binary data using the base64 algorithm but does not chunk the output.
	 * 
	 * @param data binary data to encode
	 * 
	 * @return byte[] containing Base64 characters.
	 */
	public static byte[] encodeToBytes(byte[] data) {
		if (null == data) {
			return Arrays2.EMPTY_BYTE_ARRAY;
		}
		return java.util.Base64.getEncoder().encode(data);
	}
	
	public static byte[] urlEncodeToBytes(byte[] data) {
		if (null == data) {
			return Arrays2.EMPTY_BYTE_ARRAY;
		}
		return java.util.Base64.getUrlEncoder().encode(data);
	}
	
	/**
	 * Encodes binary data using the base64 algorithm but does not chunk the output.
	 * 
	 * @param data binary data to encode
	 * 
	 * @return byte[] containing Base64 characters.
	 */
	public static byte[] encodeToBytes(String data) {
		if (null == data) {
			return Arrays2.EMPTY_BYTE_ARRAY;
		}
		return java.util.Base64.getEncoder().encode(Strings.getBytesUtf8(data));
	}
	
	public static byte[] urlEncodeToBytes(String data) {
		if (null == data) {
			return Arrays2.EMPTY_BYTE_ARRAY;
		}
		return java.util.Base64.getUrlEncoder().encode(Strings.getBytesUtf8(data));
	}

    /**
     * Decodes Base64 data into byte array.
     * 
     * @param base64 String containing Base64 data
     * 
     * @return Array containing decoded data.
     */	
	public static byte[] decodeToBytes(String base64) {
		if (null == base64) {
			return Arrays2.EMPTY_BYTE_ARRAY;
		}

		return decodeToBytes(Strings.getBytesUtf8(base64));
	}
	
    /**
     * Decodes Base64 data into byte array.
     * 
     * @param base64Bytes Byte array containing Base64 data
     * 
     * @return Array containing decoded data.
     */	
	public static byte[] decodeToBytes(byte[] base64Bytes) {
		if (null == base64Bytes) {
			return Arrays2.EMPTY_BYTE_ARRAY;
		}

		byte[] decodes = java.util.Base64.getDecoder().decode(base64Bytes);
		return decodes == null ? Arrays2.EMPTY_BYTE_ARRAY : decodes;
	}
	
	public static byte[] urlDecodeToBytes(byte[] base64Bytes) {
		if (null == base64Bytes) {
			return Arrays2.EMPTY_BYTE_ARRAY;
		}

		byte[] decodes = java.util.Base64.getUrlDecoder().decode(base64Bytes);
		return decodes == null ? Arrays2.EMPTY_BYTE_ARRAY : decodes;
	}
	
    /**
     * Decodes Base64 data into utf8 string.
     * 
     * @param base64 String containing Base64 data
     * 
     * @return String containing decoded data.
     */	
	public static String decode(String base64) {
		return Strings.newStringUtf8(decodeToBytes(base64));
	}
	
    /**
     * Decodes Base64 data into utf8 string.
     * 
     * @param base64 String containing Base64 data
     * 
     * @return String containing decoded data.
     */	
	public static String decode(byte[] base64) {
		return Strings.newStringUtf8(decodeToBytes(base64));
	}

	/**
	 * Returns whether or not the <code>octet</code> is in the base 64 alphabet.
	 * 
	 * @param base64Byte The value to test
	 * 
	 * @return <code>true</code> if the value is defined in the the base 64 alphabet, <code>false</code> otherwise.
	 */
	public static boolean isBase64(byte base64Byte) {
		return base64Byte == PAD_DEFAULT || (base64Byte >= 0 && base64Byte < DECODE_TABLE.length && DECODE_TABLE[base64Byte] != -1);
	}

	/**
	 * Tests a given String to see if it contains only valid characters within the Base64 alphabet. Currently the method
	 * treats whitespace as valid.
	 * 
	 * @param base64 String to test
	 * @return <code>true</code> if all characters in the String are valid characters in the Base64 alphabet or if the
	 *         String is empty; <code>false</code>, otherwise
	 */
	public static boolean isBase64(String base64) {
		return null == base64 ? false : isBase64(Strings.getBytesUtf8(base64));
	}

	/**
	 * Tests a given byte array to see if it contains only valid characters within the Base64 alphabet. Currently the
	 * method treats whitespace as valid.
	 * 
	 * @param base64Bytes byte array to test
	 * @return <code>true</code> if all bytes are valid characters in the Base64 alphabet or if the byte array is empty;
	 *         <code>false</code>, otherwise
	 */
	public static boolean isBase64(byte[] base64Bytes) {
		if (null == base64Bytes) {
			return false;
		}

		for (int i = 0; i < base64Bytes.length; i++) {
			if (!isBase64(base64Bytes[i]) && !isWhiteSpace(base64Bytes[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if a byte value is whitespace or not. Whitespace is taken to mean: space, tab, CR, LF
	 * 
	 * @param byteToCheck the byte to check
	 * @return true if byte is whitespace, false otherwise
	 */
	protected static boolean isWhiteSpace(byte byteToCheck) {
		switch (byteToCheck) {
			case ' ':
			case '\n':
			case '\r':
			case '\t':
				return true;
			default:
				return false;
		}
	}

	protected Base64() {

	}
}
