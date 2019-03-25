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

import leap.core.security.SimpleUserPrincipal;
import leap.core.security.UserPrincipal;
import leap.orm.annotation.SqlKey;
import leap.orm.dao.DaoCommand;
import leap.web.security.user.JdbcUserStore;

public class DefaultUserDetailsLookup implements UserDetailsLookup {

    @SqlKey(key = JdbcUserStore.SQL_KEY_FIND_USER_DETAILS_BY_ID, required = false)
    protected DaoCommand findUserDetails;

    @Override
    public boolean isEnabled() {
        return null != findUserDetails;
    }

    @Override
    public UserPrincipal lookupUserDetails(String at, String userId) {
        return findUserDetails.createQuery(SimpleUserPrincipal.class)
                .param(JdbcUserStore.SQL_PARAM_USER_ID, userId)
                .singleOrNull();
    }

}