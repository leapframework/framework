/*
 * Copyright 2013 the original author or authors.
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

import java.util.Map;

import leap.core.annotation.Inject;
import leap.lang.Lazy;
import leap.orm.dao.Dao;

public class DefaultUserStore implements UserStore {
	
    public static final String SQL_KEY_FIND_USER_ACCOUNT             = "security.findUserAccount";
    public static final String SQL_KEY_FIND_USER_DETAILS             = "security.findUserDetails";
    public static final String SQL_KEY_FIND_USER_DETAILS_BY_USERNAME = "security.findUserDetailsByUsername";
    public static final String SQL_PARAM_LOGIN_NAME                  = "username";
    public static final String SQL_PARAM_USER_ID                     = "userid";
	
	@Inject(name="security",namedOrPrimary=true)
	protected Lazy<Dao> lazyDao;
	
	public void setLazyDao(Lazy<Dao> lazyDao) {
		this.lazyDao = lazyDao;
	}

	@Override
    public UserAccount findUserAccount(String loginName, Map<String, Object> parameters) {
	    return lazyDao.get()
	    		  .createNamedQuery(SQL_KEY_FIND_USER_ACCOUNT, SimpleUserAccount.class)
	    	      .params(parameters)
	    	      .param(SQL_PARAM_LOGIN_NAME, loginName)
	    	      .singleOrNull();
    }

	@Override
    public UserDetails findUserDetails(Object userId) {
	    return lazyDao.get()
	    		  .createNamedQuery(SQL_KEY_FIND_USER_DETAILS, SimpleUserDetails.class)
	    		  .param(SQL_PARAM_USER_ID, userId)
	    		  .singleOrNull();
    }

    @Override
    public UserDetails findUserDetailsByUsername(String username) {
        return lazyDao.get()
                .createNamedQuery(SQL_KEY_FIND_USER_DETAILS_BY_USERNAME, SimpleUserDetails.class)
                .param(SQL_PARAM_LOGIN_NAME, username)
                .singleOrNull();
    }

}