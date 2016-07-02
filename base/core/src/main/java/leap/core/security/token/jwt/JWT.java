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
package leap.core.security.token.jwt;

import leap.lang.Strings;
import leap.lang.codec.Base64;

public class JWT {

    public static final String ALG_HS256             = "HS256";
    public static final String ALG_HS384             = "HS384";
    public static final String ALG_HS512             = "HS512";
    public static final String ALG_RS256             = "RS256";
    public static final String ALG_RS384             = "RS384";
    public static final String ALG_RS512             = "RS512";

    public static final String CLAIM_ISSUED_AT       = "iat";
    public static final String CLAIM_ISSUER          = "iss";
    public static final String CLAIM_EXPIRATION_TIME = "exp";
    public static final String CLAIM_SUBJECT         = "sub";
    public static final String CLAIM_AUDIENCE        = "aud";
    public static final String CLAIM_JWT_ID          = "jti";

    public static String base64UrlEncode(String data) {
        return base64UrlEncode(Strings.getBytesUtf8(data));
    }

    public static String base64UrlEncode(byte[] data) {
        String encoded = new String(Base64.urlEncode(data));
        //removes all the '=' characters
        StringBuilder sb = new StringBuilder(encoded);
        while (sb.charAt(sb.length() - 1) == '=') {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public static byte[] base64UrlDeocode(String encoded) {
        for (int j = 0; j < encoded.length() % 4; j++) {
            encoded = encoded + "=";
        }
        return Base64.urlDecodeToBytes(Strings.getBytesUtf8(encoded));
    }
    
    public static String base64UrlDeocodeToString(String encoded) {
        return Strings.newStringUtf8(base64UrlDeocode(encoded));
    }
	
	protected JWT() {
		
	}
	
}
