/*
 * Copyright 2014 the original author or authors.
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
package leap.web.security;

import leap.core.security.SEC;
import leap.orm.dmo.Dmo;
import leap.webunit.WebTestBaseContextual;
import leap.webunit.client.THttpRequest;
import tested.models.User;

public abstract class SecurityTestCase extends WebTestBaseContextual {
	
	public static final String USERNAME = "admin";
	public static final String PASSWORD = "1";
	
	static {
		Dmo.get().cmdUpgradeSchema().execute();
		
		User.deleteAll();
		
		User admin = new User();
		admin.setLoginName(USERNAME);
		admin.setPassword(SEC.encodePassword(PASSWORD));
		admin.save();
		
		User test1 = new User();
		test1.setLoginName("test1");
		test1.setPassword("bad password");
		test1.save();
		
		User test2 = new User();
		test2.setLoginName("test2");
		test2.setPassword("1");
		test2.setEnabled(false);
		test2.save();
	}
	
	protected void login() {
		login("",USERNAME, PASSWORD);
	}
	
	protected void login(String contextPath) {
		login(contextPath, USERNAME, PASSWORD);
	}
	
    protected void login(String contextPath, String username, String password) {
        forLogin(contextPath, username, password).send();
    }
	
	protected THttpRequest forLogin() {
		return forLogin(contextPath, USERNAME, PASSWORD);
	}
	
	protected THttpRequest forLogin(String contextPath) {
		return forLogin(contextPath, USERNAME, PASSWORD);
	}
	
	protected THttpRequest forLogin(String contextPath, String username, String password) {
		return client().request(contextPath + "/login")
		               .addFormParam("username", username)
		               .addFormParam("password", password);
	}
	
	protected void logout() {
		post("/logout");
	}
	
	protected void logout(String contextPath) {
		post(contextPath + "/logout");
	}
}