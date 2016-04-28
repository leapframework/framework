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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserStore implements UserStore {
    
    private final Map<String, User> users = new ConcurrentHashMap<String, InMemoryUserStore.User>();

    @Override
    public UserDetails loadUserDetailsById(Object userId) {
        return users.get((String)userId);
    }
    
    @Override
    public UserDetails loadUserDetailsByLoginName(String username) {
        return users.get(username);
    }

    public InMemoryUserStore add(String username, String encryptedPassword) {
        users.put(username, new User(username, encryptedPassword));
        return this;
    }
    
    public InMemoryUserStore add(User user) {
        users.put(user.getLoginName(), user);
        return this;
    }
    
    public User get(String username) {
        return users.get(username);
    }
    
    public static final class User implements UserDetails {
        
        protected String  name;
        protected String  loginName;
        protected String  password;
        protected boolean enabled = true;
        
        public User() {
            super();
        }

        public User(String username, String password) {
            this.name = username;
            this.loginName = username;
            this.password = password;
        }

        @Override
        public Object getId() {
            return loginName;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getLoginName() {
            return loginName;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setLoginName(String loginName) {
            this.loginName = loginName;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
    
}