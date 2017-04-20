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


public abstract class AbstractTokenSigner implements TokenSigner {
    
	protected int defaultExpires = DEFAULT_EXPIRES_SECONDS;
	
	protected AbstractTokenSigner() {
		
	}

    public int getDefaultExpires() {
        return defaultExpires;
    }

    public void setDefaultExpires(int defaultExpires) {
        this.defaultExpires = defaultExpires;
    }

    protected long getExpirationTimeMs(Integer expiresInSecond) {
		long now = System.currentTimeMillis();
		
		if(null == expiresInSecond || expiresInSecond <= 0) {
			return now + defaultExpires * 1000L;
		}else{
			return now + expiresInSecond * 1000L;
		}
	}
	
	protected long getExpirationTimeInSecond(Integer expiresInSecond){
		long now = System.currentTimeMillis()/1000L;

		if(null == expiresInSecond || expiresInSecond <= 0) {
			return now + defaultExpires;
		}else{
			return now + expiresInSecond;
		}
	}
	
}