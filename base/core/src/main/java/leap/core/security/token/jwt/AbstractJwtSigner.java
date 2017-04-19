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

import java.util.Map;

import leap.core.security.token.AbstractTokenSigner;
import leap.lang.json.JSON;
import leap.lang.json.JsonWriter;

public abstract class AbstractJwtSigner extends AbstractTokenSigner implements JwtSigner {
	
    private String  encodedHeader;
    private boolean encodeHeader = true;
    private boolean encodeTyp    = true;

    public boolean isEncodeHeader() {
        return encodeHeader;
    }

    public void setEncodeHeader(boolean encodeHeader) {
        this.encodeHeader = encodeHeader;
    }

    public boolean isEncodeTyp() {
        return encodeTyp;
    }

    public void setEncodeTyp(boolean encodeTyp) {
        this.encodeTyp = encodeTyp;
    }
	
	@Override
    public String sign(Map<String, Object> claims, Integer expiresInSecond) {
		if(null == claims || claims.isEmpty()) {
			throw new IllegalArgumentException("Claims must not be null or empty");
		}
		
		//Set expiration time.
		if(!claims.containsKey(JWT.CLAIM_EXPIRATION_TIME)) {
			claims.put(JWT.CLAIM_EXPIRATION_TIME, getExpirationTimeInSecond(expiresInSecond));
		}
		
		String content  = encodeContent(claims);
		String signture = sign(content);
		
		return content + "." + signture;
    }
	
	protected String encodeContent(Map<String, Object> claims) {
		if(isEncodeHeader()) {
			StringBuilder s = new StringBuilder();
			
			s.append(encodeHeader(claims))
			 .append('.')
			 .append(encodePayload(claims));
			
			return s.toString();
		}else{
			return encodePayload(claims);
		}
	}
	
    public String getPayload(Map<String, Object> claims) {
        JsonWriter w = JSON.createWriter();
        w.map(claims);
        return w.toString();
	}

    protected String encodePayload(Map<String, Object> claims) {
		JsonWriter w = JSON.createWriter();
		
		w.map(claims);
		
		return JWT.base64UrlEncode(w.toString());
	}

	protected String encodeHeader(Map<String, Object> claims) {
		if(null == encodedHeader) {
			JsonWriter w = JSON.createWriter();

            w.startObject();

            if (isEncodeTyp()) {
                w.property("typ", "JWT");
            }

            w.property("alg", getJwtAlgorithm());
            w.endObject();
			
			encodedHeader = JWT.base64UrlEncode(w.toString());
		}
		
		return encodedHeader;
	}
	
	protected String sign(String content) {
	    return JWT.base64UrlEncode(signToBytes(content));
	}
	
	protected abstract String getJwtAlgorithm();
	
	protected abstract byte[] signToBytes(String payload);
	
}