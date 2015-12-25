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

import leap.core.AppContext;
import leap.core.security.crypto.PasswordEncoder;
import leap.web.security.SecurityConfig;
import leap.web.security.SecurityTestCase;

import org.junit.Test;

import tested.models.User;

public class UserDetailsTest extends SecurityTestCase {
	
	private UserStore userStore;
	private PasswordEncoder passwordEncoder;

	@Override
    protected void doSetUp() throws Exception {
		userStore       = AppContext.factory().getBean(UserStore.class);
		passwordEncoder = AppContext.factory().getBean(SecurityConfig.class).getPasswordEncoder();
    }

	@Test
	public void testUserStore(){
		String username = "testLoginName";
		String password  = "1";
		String encodedPassword = passwordEncoder.encode(password);
		
		User.deleteBy("loginName", username);
		
		assertNull(userStore.findUserAccount(username, null));
		
		User user = new User();
		user.setLoginName(username);
		user.setPassword(encodedPassword);
		user.save();

		try{
			UserAccount account = userStore.findUserAccount(username, null);
			assertNotNull(account);
			assertNotNull(account.getId());
			assertEquals(encodedPassword, account.getPassword());
			assertTrue(passwordEncoder.matches(password, encodedPassword));
			
			UserDetails details = userStore.findUserDetails(account.getId());
			assertNotNull(details);
			assertNotNull(details.getLoginName());
			assertNotNull(details.getName());
		}finally{
			user.delete();	
		}
	}
}