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

import leap.lang.Arrays2;
import leap.lang.Buildable;
import leap.lang.Collections2;
import leap.lang.path.AntPathPattern;
import leap.lang.path.PathPattern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SecurityPathBuilder implements Buildable<SecurityPath> {

	protected PathPattern        pathPattern;
	protected boolean            allowAnonymous  = false;
	protected boolean            allowClientOnly = false;
	protected boolean            allowRememberMe = true;
	protected List<String>		 permissions	 = new ArrayList<>();
    protected List<String>       roles           = new ArrayList<>();
	protected List<SecurityRule> rules           = new ArrayList<>();
	
	public SecurityPathBuilder() {
	    super();
    }
	
	public SecurityPathBuilder(String path) {
		this.path(path);
	}

	public PathPattern getPathPattern() {
		return pathPattern;
	}

	public SecurityPathBuilder setPathPattern(PathPattern pathPattern) {
		this.pathPattern = pathPattern;
		return this;
	}
	
	public SecurityPathBuilder path(String pattern) {
		return setPathPattern(new AntPathPattern(pattern));
	}
	
	public boolean isAllowAnonymous() {
		return allowAnonymous;
	}

	public SecurityPathBuilder setAllowAnonymous(boolean allowAnonymous) {
		this.allowAnonymous = allowAnonymous;
		return this;
	}
	
	public boolean isAllowClientOnly() {
        return allowClientOnly;
    }

    public SecurityPathBuilder setAllowClientOnly(boolean allowClientOnly) {
        this.allowClientOnly = allowClientOnly;
        return this;
    }

    public SecurityPathBuilder allowAnonymous(){
		return setAllowAnonymous(true);
	}
	
	public SecurityPathBuilder allowClientOnly() {
	    return setAllowClientOnly(true);
	}
	
	public boolean isAllowRememberMe() {
		return allowRememberMe;
	}

	public SecurityPathBuilder setAllowRememberMe(boolean denyRememberMe) {
		this.allowRememberMe = denyRememberMe;
		return this;
	}
	
	public List<SecurityRule> getRules() {
		return rules;
	}

	public SecurityPathBuilder addRules(SecurityRule... rules) {
		Collections2.addAll(this.rules, rules);
		return this;
	}
	
	public SecurityPathBuilder addRules(Collection<SecurityRule> rules) {
		if(null != rules){
			this.rules.addAll(rules);
		}
		return this;
	}
	
	public SecurityPathBuilder addRule(SecurityRule rule) {
		this.rules.add(rule);
		return this;
	}

	@Override
    public SecurityPath build() {
        return new SecurityPath(pathPattern,
                                allowAnonymous,
                                allowClientOnly,
                                allowRememberMe,
                                permissions.toArray(Arrays2.EMPTY_STRING_ARRAY),
                                roles.toArray(Arrays2.EMPTY_STRING_ARRAY),
                                rules.toArray(new SecurityRule[]{}));
    }
}