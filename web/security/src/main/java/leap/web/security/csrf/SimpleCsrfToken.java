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
package leap.web.security.csrf;

import leap.web.security.SecurityConfig;

class SimpleCsrfToken implements CsrfToken {
	
	protected final boolean		   _new;
	protected final SecurityConfig config;
	protected final String		   token;
	
	public SimpleCsrfToken(SecurityConfig config,String token, boolean isNew) {
		this.config = config;
		this.token  = token;
		this._new   = isNew;
	}

	@Override
    public String getHeader() {
        return config.getCsrfHeaderName();
    }

	@Override
    public String getParameter() {
        return config.getCsrfParameterName();
    }

	@Override
    public String getToken() {
        return token;
    }

	@Override
    public boolean isNew() {
	    return _new;
    }

	@Override
    public String toString() {
		return token;
    }
}