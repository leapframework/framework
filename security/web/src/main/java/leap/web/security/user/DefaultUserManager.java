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
package leap.web.security.user;

import leap.core.annotation.Inject;
import leap.core.security.UserPrincipal;
import leap.lang.Result;
import leap.web.security.SecurityConfig;
import leap.core.security.Authentication;
import leap.web.security.authc.SimpleAuthentication;

public class DefaultUserManager implements UserManager {
    
    protected @Inject SecurityConfig sc;

    @Override
    public UserDetails getUserDetails(UserPrincipal user) {
        if(user instanceof UserDetails) {
            return (UserDetails)user;
        }
        return sc.getUserStore().loadUserDetailsById(user.getId());
    }

    @Override
    public UserDetails loadUserDetails(String userId) {
        return sc.getUserStore().loadUserDetailsById(userId);
    }

    @Override
    public Result<Authentication> createAuthenticationByUsername(String username) {
        UserDetails details = sc.getUserStore().loadUserDetailsByLoginName(username);
        
        //TODO : check user enabled?
        
        if(null == details) {
            return Result.empty();
        }

        return Result.of(new SimpleAuthentication(details, new TrustedLoginNameCredentials(username)));
    }

    @Override
    public Result<Authentication> createAuthenticationByUserId(String userid) {
        UserDetails details = sc.getUserStore().loadUserDetailsByIdString(userid);

        if(null == details) {
            return Result.empty();
        }

        return Result.of(new SimpleAuthentication(details, new TrustedUserIdCredentials(userid)));
    }
}