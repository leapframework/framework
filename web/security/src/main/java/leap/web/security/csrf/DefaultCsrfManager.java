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

import java.util.concurrent.atomic.AtomicInteger;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ioc.PostCreateBean;
import leap.core.security.token.SimpleTokenEncoder;
import leap.core.security.token.TokenEncoder;
import leap.core.security.token.TokenExpiredException;
import leap.web.Request;
import leap.web.security.SecurityConfig;

public class DefaultCsrfManager implements CsrfManager, PostCreateBean {
	
	private final AtomicInteger counter = new AtomicInteger();
	
	protected @Inject SecurityConfig  sc;
	
	protected TokenEncoder tokenEncoder;

	@Override
	public String generateToken(Request request) throws Throwable {
		return doGenerateToken();
	}

	@Override
	public String loadToken(Request request) throws Throwable {
		return sc.getCsrfStore().loadToken(request);
	}
	
	@Override
    public boolean verifyToken(Request request, String token, CsrfToken expected) throws CsrfTokenExpiredException {
	    try {
	        return null == token ? false : tokenEncoder.verifyToken(token);
        } catch (TokenExpiredException e) {
        	throw new CsrfTokenExpiredException(e.getMessage());
        }
    }

	@Override
	public void saveToken(Request request, String token) throws Throwable {
		sc.getCsrfStore().saveToken(request, token);
	}

	@Override
	public void removeToken(Request request) throws Throwable {
		sc.getCsrfStore().removeToken(request);
	}

	protected String doGenerateToken() {
		String value = String.valueOf(counter.incrementAndGet());
		return tokenEncoder.encodeToken(value);
	}

	@Override
    public void postCreate(BeanFactory factory) throws Throwable {
		if(null == tokenEncoder) {
			tokenEncoder = new SimpleTokenEncoder(sc.getSecret(), sc.getDefaultAuthenticationExpires());
		}
	}
}