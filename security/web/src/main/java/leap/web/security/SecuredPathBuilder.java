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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import leap.lang.Buildable;
import leap.lang.Collections2;
import leap.lang.path.AntPathPattern;
import leap.lang.path.PathPattern;

public class SecuredPathBuilder implements Buildable<SecuredPath> {

	protected PathPattern         pathPattern;
	protected boolean		      allowAnonymous  = false;
	protected boolean             allowClientOnly = false;
	protected boolean		      allowRememberMe = true;
	protected List<SecuredRule>   rules 		  = new ArrayList<SecuredRule>();
	
	public SecuredPathBuilder() {
	    super();
    }
	
	public SecuredPathBuilder(String path) {
		this.path(path);
	}

	public PathPattern getPathPattern() {
		return pathPattern;
	}

	public SecuredPathBuilder setPathPattern(PathPattern pathPattern) {
		this.pathPattern = pathPattern;
		return this;
	}
	
	public SecuredPathBuilder path(String pattern) {
		return setPathPattern(new AntPathPattern(pattern));
	}
	
	public boolean isAllowAnonymous() {
		return allowAnonymous;
	}

	public SecuredPathBuilder setAllowAnonymous(boolean allowAnonymous) {
		this.allowAnonymous = allowAnonymous;
		return this;
	}
	
	public boolean isAllowClientOnly() {
        return allowClientOnly;
    }

    public SecuredPathBuilder setAllowClientOnly(boolean allowClientOnly) {
        this.allowClientOnly = allowClientOnly;
        return this;
    }

    public SecuredPathBuilder allowAnonymous(){
		return setAllowAnonymous(true);
	}
	
	public SecuredPathBuilder allowClientOnly() {
	    return setAllowClientOnly(true);
	}
	
	public boolean isAllowRememberMe() {
		return allowRememberMe;
	}

	public SecuredPathBuilder setAllowRememberMe(boolean denyRememberMe) {
		this.allowRememberMe = denyRememberMe;
		return this;
	}
	
	public List<SecuredRule> getRules() {
		return rules;
	}

	public SecuredPathBuilder addRules(SecuredRule... rules) {
		Collections2.addAll(this.rules, rules);
		return this;
	}
	
	public SecuredPathBuilder addRules(Collection<SecuredRule> rules) {
		if(null != rules){
			this.rules.addAll(rules);
		}
		return this;
	}
	
	public SecuredPathBuilder addRule(SecuredRule rule) {
		this.rules.add(rule);
		return this;
	}

	@Override
    public SecuredPath build() {
        return new SecuredPath(pathPattern, allowAnonymous, allowClientOnly, allowRememberMe, rules.toArray(new SecuredRule[]{}));
    }
}