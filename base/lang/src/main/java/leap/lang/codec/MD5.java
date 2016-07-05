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


public class MD5 {
	
	/**
	 * Calculates the MD5 digest and returns the value as a base64 string.
	 */
	public static String digest(String text) {
		return Digests.md5Base64(text);
	}
	
	/**
	 * Calculates the MD5 digest and returns the value as a base64 string.
	 */
	public static String digest(byte[] data) {
		return Digests.md5Base64(data);
	}
	
	/**
     * Calculates the MD5 digest and returns the value as a byte array.
     */
	public static byte[] digestToBytes(byte[] data) {
	    return Digests.md5(data);
	}
	
	/**
	 * Calculates the MD5 digest and returns the value as a hex string.
	 */
	public static String hex(byte[] data){
		return Digests.md5Hex(data);
	}

}
