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
package leap.core.security.token;

import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.codec.Base64;
import leap.lang.codec.MD5;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

public class SimpleTokenEncoder implements TokenEncoder {
	
	private static final Log log = LogFactory.get(SimpleTokenEncoder.class);
	
	protected String  secret;
	protected int     expiresInSeconds;
	
	public SimpleTokenEncoder() {
	    super();
    }

	public SimpleTokenEncoder(String secret, int expiresInSeconds) {
		Args.notEmpty(secret,"secret");
		this.secret = secret;
		this.expiresInSeconds = expiresInSeconds;
	}
	
	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public int getExpiresInSeconds() {
		return expiresInSeconds;
	}

	public void setExpiresInSeconds(int expiresInSeconds) {
		this.expiresInSeconds = expiresInSeconds;
	}

	@Override
	public String encodeToken(String token) {
		String expires = String.valueOf(expiresInSeconds > 0 ? System.currentTimeMillis() + expiresInSeconds * 1000 : 0); 
		return doEncodeToken(token, expires);
	}

	@Override
	public boolean verifyToken(String encoded) throws TokenExpiredException {
        for (int j = 0; j < encoded.length() % 4; j++) {
        	encoded = encoded + "=";
        }
		
		if(!Base64.isBase64(encoded)) {
			log.debug("The encoded token '{}' is not a valid base64 string",encoded);
			return false;
		}
		
		String   data  = Base64.decode(encoded);
		String[] parts = Strings.split(data,':');
		if(parts.length != 3) {
			log.debug("The encoded token '{}' is invalid", encoded);
			return false;
		}
		
		String token   = parts[0];
		String expires = parts[1];
		String signed  = parts[2];
		
		try{
			verifyTokenExpired(expires);
			
			return verifyTokenSignature(token, expires, signed);
			
		}catch(Throwable e){
			log.info("Error verifying the encoded token '{}', {}", encoded, e.getMessage(), e);
			return false;
		}
	}
	
	protected void verifyTokenExpired(String expires) throws TokenExpiredException {
        long expirationTime = Long.parseLong(expires);
        long now			= System.currentTimeMillis();
        
        if(expirationTime > 0 && now - expirationTime > 0) {
        	throw new TokenExpiredException("Token expired, expration time '" + expirationTime + "', current time '" + now + "'");
        }
	}
	
	protected boolean verifyTokenSignature(String token, String expires, String signed) {
		return sign(token, expires).equals(signed);
	}
	
	/**
	 * token + ":" + expires + ":" + md5Hex(token + ":" + expires + ":" + secret)
	 */
	protected String doEncodeToken(String token, String expires) {
		Args.assertFalse(token.contains(":"), "The token must not contains character ':'");
		
		String encoded = token + ":" + expires + ":" + sign(token, expires) ;
		
		//removes all the '=' characters
		StringBuilder sb = new StringBuilder(Base64.encode(encoded));
        while (sb.charAt(sb.length() - 1) == '=') {
            sb.deleteCharAt(sb.length() - 1);
        }
		
		return sb.toString();
	}
	
	protected String sign(String token , String expires) {
		String content = token + ":" + expires +  ":" + secret;
		String signed  = MD5.hex(Strings.getBytesUtf8(content));
		return signed;
	}

}