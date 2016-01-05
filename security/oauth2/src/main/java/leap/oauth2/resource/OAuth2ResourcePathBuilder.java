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
package leap.oauth2.resource;

import java.util.Collection;

import leap.lang.path.PathPattern;
import leap.web.security.SecuredPathBuilder;
import leap.web.security.SecuredRule;

public class OAuth2ResourcePathBuilder extends SecuredPathBuilder {
	
	protected OAuth2ResourceScope scope;
	
	@Override
    public OAuth2ResourcePathBuilder setPathPattern(PathPattern pathPattern) {
        super.setPathPattern(pathPattern);
        return this;
    }

	@Override
    public OAuth2ResourcePathBuilder path(String pattern) {
        super.path(pattern);
        return this;
    }

	@Override
    public OAuth2ResourcePathBuilder setAllowAnonymous(boolean allowAnonymous) {
        super.setAllowAnonymous(allowAnonymous);
        return this;
    }
	
	@Override
    public OAuth2ResourcePathBuilder setAllowClientOnly(boolean allowClientOnly) {
        super.setAllowClientOnly(allowClientOnly);
        return this;
    }

    @Override
    public OAuth2ResourcePathBuilder allowClientOnly() {
        super.allowClientOnly();
        return this;
    }

    @Override
    public OAuth2ResourcePathBuilder allowAnonymous() {
        super.allowAnonymous();
        return this;
	}

	@Override
    public OAuth2ResourcePathBuilder setAllowRememberMe(boolean denyRememberMe) {
        super.setAllowRememberMe(denyRememberMe);
        return this;
    }

	@Override
    public OAuth2ResourcePathBuilder addRules(SecuredRule... rules) {
        super.addRules(rules);
        return this;
    }

	@Override
    public OAuth2ResourcePathBuilder addRules(Collection<SecuredRule> rules) {
        super.addRules(rules);
        return this;
    }

	@Override
    public OAuth2ResourcePathBuilder addRule(SecuredRule rule) {
        super.addRule(rule);
        return this;
    }
	
	public OAuth2ResourceScope getScope() {
		return scope;
	}

	public OAuth2ResourcePathBuilder setScope(OAuth2ResourceScope scope) {
		this.scope = scope;
		return this;
	}

	@Override
    public OAuth2ResourcePath build() {
		return new OAuth2ResourcePath(pathPattern, allowAnonymous, allowClientOnly, allowRememberMe, rules.toArray(new SecuredRule[]{}), scope);
	}
}