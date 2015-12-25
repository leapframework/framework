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
package app;

import java.io.IOException;

import javax.servlet.ServletException;

import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.lang.intercepting.State;
import leap.web.App;
import leap.web.Filter;
import leap.web.FilterChain;
import leap.web.FilterMappings;
import leap.web.Request;
import leap.web.Response;
import leap.web.config.WebConfigurator;
import leap.web.security.SecurityConfigurator;
import leap.web.security.SecurityContextHolder;
import leap.web.security.SecurityInterceptor;
import leap.web.security.csrf.CSRF;

public class Global extends App {
    
	protected @Inject SecurityConfigurator sc;
	
	@Override
    protected void filtering(FilterMappings filters) {
		//csrf attribute test filter
		filters().add(new Filter() {
			@Override
			public void doFilter(final Request request, Response response, FilterChain chain) throws ServletException, IOException {
				String token = request.getParameter("csrf_attr_test_token");
				if(!Strings.isEmpty(token)) {
					CSRF.setRequestToken(request, token);
				}
				
				String ignored = request.getParameter("csrf_ignored");
				if(!Strings.isEmpty(ignored)) {
					CSRF.ignore(request.getServletRequest());
				}
				
				chain.doFilter(request, response);
			}
		});
	}

	@Override
    protected void configure(WebConfigurator c) {
		sc.enable(true)
		  .allowAnonymousAccessTo("/anonymous")
		  .allowAnonymousAccessTo("/public/**/*");
	}
	
}