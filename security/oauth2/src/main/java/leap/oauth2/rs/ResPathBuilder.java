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

import leap.lang.path.PathPattern;
import leap.web.security.path.DefaultSecuredPathBuilder;

public class ResPathBuilder extends DefaultSecuredPathBuilder {
	
	protected ResScope scope;
	
	@Override
    public ResPathBuilder setPattern(PathPattern pathPattern) {
        super.setPattern(pathPattern);
        return this;
    }

	@Override
    public ResPathBuilder path(String pattern) {
        super.path(pattern);
        return this;
    }

	@Override
    public ResPathBuilder setAllowAnonymous(boolean allowAnonymous) {
        super.setAllowAnonymous(allowAnonymous);
        return this;
    }
	
	@Override
    public ResPathBuilder setAllowClientOnly(boolean allowClientOnly) {
        super.setAllowClientOnly(allowClientOnly);
        return this;
    }

    @Override
    public ResPathBuilder allowClientOnly() {
        super.allowClientOnly();
        return this;
    }

    @Override
    public ResPathBuilder allowAnonymous() {
        super.allowAnonymous();
        return this;
	}

	@Override
    public ResPathBuilder setAllowRememberMe(boolean denyRememberMe) {
        super.setAllowRememberMe(denyRememberMe);
        return this;
    }

	public ResScope getScope() {
		return scope;
	}

	public ResPathBuilder setScope(ResScope scope) {
		this.scope = scope;
		return this;
	}

	@Override
    public ResPath build() {
		return new ResPath(pattern, allowAnonymous, allowClientOnly, allowRememberMe, scope);
	}
}