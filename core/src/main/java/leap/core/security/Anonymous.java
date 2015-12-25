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
package leap.core.security;

public class Anonymous implements UserPrincipal {
	
	private static final long serialVersionUID = -2408741977440943645L;

	public static final String DEFAULT_ANYNYMOUS_ID  = "anonymous";
	
	protected String  id       = DEFAULT_ANYNYMOUS_ID;
	protected String  name     = DEFAULT_ANYNYMOUS_ID;
	protected String  nickName = DEFAULT_ANYNYMOUS_ID;
	
	private final boolean authenticated = false;;
	private final boolean rememberMe    = false;
	private final Object  details		= null;
	
	public Anonymous() {
		this(DEFAULT_ANYNYMOUS_ID);
	}
	
	public Anonymous(String id){
		this.id = id;
	}

	@Override
    public Object getId() {
	    return id;
    }

	@Override
    public String getLoginName() {
	    return name;
    }
	
	public String getName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	@Override
    public boolean isAnonymous() {
	    return true;
    }

	@Override
    public boolean isRememberMe() {
	    return rememberMe;
    }

	@Override
    public boolean isAuthenticated() {
	    return authenticated;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getDetails() {
	    return (T)details;
    }
}
