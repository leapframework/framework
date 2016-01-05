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
package leap.oauth2.rs.token;

import leap.core.annotation.Inject;
import leap.lang.Result;
import leap.oauth2.as.token.AuthzAccessToken;
import leap.oauth2.as.token.AuthzTokenManager;

public class LocalBearerResAccessTokenStore implements ResBearerAccessTokenStore {
    
    protected @Inject AuthzTokenManager authzServerTokenManager;

    @Override
    public Result<ResAccessTokenDetails> loadAccessTokenDetails(ResAccessToken token) {
        AuthzAccessToken at = authzServerTokenManager.loadAccessToken(token.getToken());
        if(null == at) {
            return Result.empty();
        }
        
        SimpleResAccessTokenDetails details = new SimpleResAccessTokenDetails();
        
        details.setClientId(at.getClientId());
        details.setUserId(at.getUserId());
        details.setCreated(at.getCreated());
        details.setExpiresIn(at.getExpiresIn() * 1000);
        
        //TODO : 
        
        return Result.of(details);
    }

    @Override
    public void removeAccessToken(ResAccessToken token) {
        //Do nothing.
    }
}