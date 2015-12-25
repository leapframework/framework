/*
 * Copyright 2015 the original author or authors.
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
package leap.core.security.crypto;

import java.io.UnsupportedEncodingException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import leap.lang.Charsets;
import leap.lang.codec.Base64;
import leap.lang.codec.Hex;
import leap.lang.exception.NestedUnsupportedEncodingException;


public class Signature {
	
	public static final String ALG_HMAC256 = "HmacSHA256";
	
	public static String hexHmac256(byte[] secret, String data) {
		return Hex.encode(hmac256(secret, data));
	}
	
	public static String base64Hmac256(byte[] secret, String data) {
		return Base64.encode(hmac256(secret, data));
	}
	
	public static byte[] hmac256(byte[] secret, String data) throws NestedUnsupportedEncodingException,SignatureException{
		try {
			return hmac256(secret, data.getBytes(Charsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
        	throw new NestedUnsupportedEncodingException(e);
        }
	}
	
	public static byte[] hmac256(byte[] secret, byte[] data) throws SignatureException{
		try {
	        Mac mac = Mac.getInstance(ALG_HMAC256);
	        mac.init(new SecretKeySpec(secret, ALG_HMAC256));
	        return mac.doFinal(data);
        } catch (Exception e) {
        	throw new SignatureException("Error signing hmac256 data, " + e.getMessage(), e);
        }
	}
	
	protected Signature() {
		
	}

}
