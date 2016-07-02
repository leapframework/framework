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

public final class Anonymous implements UserPrincipal {
	
	private static final long serialVersionUID = -2408741977440943645L;

	public static final String ANONYMOUS_NAME = "anonymous";

	protected final String id;
	protected final String name;
	protected final String loginName;

	public Anonymous() {
        this(ANONYMOUS_NAME);
	}
	
	public Anonymous(String name){
        this.id        = ANONYMOUS_NAME;
        this.name      = name;
        this.loginName = ANONYMOUS_NAME;
	}

	@Override
    public Object getId() {
	    return id;
    }

    public String getName() {
        return name;
    }

	@Override
    public String getLoginName() {
	    return loginName;
    }
	
    public final boolean isAnonymous() {
	    return true;
    }

}
