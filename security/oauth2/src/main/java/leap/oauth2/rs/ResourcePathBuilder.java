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
package leap.oauth2.rs;

import java.util.Collection;

import leap.lang.path.PathPattern;
import leap.web.security.SecuredPathBuilder;
import leap.web.security.SecuredRule;

public class ResourcePathBuilder extends SecuredPathBuilder {
	
	protected ResourceScope scope;
	
	@Override
    public ResourcePathBuilder setPathPattern(PathPattern pathPattern) {
        super.setPathPattern(pathPattern);
        return this;
    }

	@Override
    public ResourcePathBuilder path(String pattern) {
        super.path(pattern);
        return this;
    }

	@Override
    public ResourcePathBuilder setAllowAnonymous(boolean allowAnonymous) {
        super.setAllowAnonymous(allowAnonymous);
        return this;
    }
	
	@Override
    public ResourcePathBuilder setAllowClientOnly(boolean allowClientOnly) {
        super.setAllowClientOnly(allowClientOnly);
        return this;
    }

    @Override
    public ResourcePathBuilder allowClientOnly() {
        super.allowClientOnly();
        return this;
    }

    @Override
    public ResourcePathBuilder allowAnonymous() {
        super.allowAnonymous();
        return this;
	}

	@Override
    public ResourcePathBuilder setAllowRememberMe(boolean denyRememberMe) {
        super.setAllowRememberMe(denyRememberMe);
        return this;
    }

	@Override
    public ResourcePathBuilder addRules(SecuredRule... rules) {
        super.addRules(rules);
        return this;
    }

	@Override
    public ResourcePathBuilder addRules(Collection<SecuredRule> rules) {
        super.addRules(rules);
        return this;
    }

	@Override
    public ResourcePathBuilder addRule(SecuredRule rule) {
        super.addRule(rule);
        return this;
    }
	
	public ResourceScope getScope() {
		return scope;
	}

	public ResourcePathBuilder setScope(ResourceScope scope) {
		this.scope = scope;
		return this;
	}

	@Override
    public ResourcePath build() {
		return new ResourcePath(pathPattern, allowAnonymous, allowClientOnly, allowRememberMe, rules.toArray(new SecuredRule[]{}), scope);
	}
}