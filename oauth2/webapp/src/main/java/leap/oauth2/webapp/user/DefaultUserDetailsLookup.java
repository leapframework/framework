/*
 *
 *  * Copyright 2019 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package leap.oauth2.webapp.user;

import leap.lang.New;
import leap.lang.Strings;
import leap.orm.annotation.SqlKey;
import leap.orm.dao.DaoCommand;
import leap.web.security.user.JdbcUserStore;
import leap.web.security.user.SimpleUserDetails;
import leap.web.security.user.UserDetails;

public class DefaultUserDetailsLookup implements UserDetailsLookup {

    public static final String SQL_KEY_FIND_USER_DETAILS_BY_ID = "oauth2.findUserDetailsById";
    public static final String SQL_KEY_CREATE_USER             = "oauth2.createUser";

    @SqlKey(key = SQL_KEY_FIND_USER_DETAILS_BY_ID, required = false)
    protected DaoCommand findUserDetails;

    @SqlKey(key = SQL_KEY_CREATE_USER, required = false)
    protected DaoCommand createUser;

    @Override
    public boolean isEnabled() {
        return null != findUserDetails && findUserDetails.exists();
    }

    @Override
    public UserDetails lookupUserDetails(String userId, String name, String loginName) {
        UserDetails user = findUserDetails(userId);

        if (null == user && (null != createUser && createUser.exists()) && !Strings.isEmpty(loginName)) {
            if (Strings.isEmpty(name)) {
                name = loginName;
            }
            createUser.executeUpdate(New.hashMap("userId", userId, "name", name, "loginName", loginName));
            user = findUserDetails(userId);
        }

        return user;
    }

    protected UserDetails findUserDetails(String userId) {
        return findUserDetails.createQuery(SimpleUserDetails.class)
                .param(JdbcUserStore.SQL_PARAM_USER_ID, userId)
                .singleOrNull();
    }

}