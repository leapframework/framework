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
import leap.lang.net.Urls;
import leap.oauth2.server.OAuth2ServerConfigurator;
import leap.oauth2.server.client.OAuth2ClientBuilder;
import leap.oauth2.server.store.OAuth2InMemoryStore;
import leap.oauth2.resource.OAuth2ResourceConfigurator;
import leap.web.App;
import leap.web.Filter;
import leap.web.FilterChain;
import leap.web.FilterMappings;
import leap.web.Request;
import leap.web.Response;
import leap.web.config.WebConfigurator;
import leap.web.security.SecurityConfigurator;
import leap.web.security.csrf.CSRF;

/**
 * App : in-memory oauth2 authorization server test
 */
public class Global extends App {
	public static final String TEST_CLIENT_ID                   = "test";
	public static final String TEST_CLIENT_SECRET               = "test_secret";
	public static final String TEST_CLIENT_REDIRECT_URI         = "/oauth2/redirect_uri";
	public static final String TEST_CLIENT_REDIRECT_URI_ENCODED = Urls.encode(TEST_CLIENT_REDIRECT_URI);

	protected @Inject SecurityConfigurator       sc;
    protected @Inject OAuth2ServerConfigurator   ac;
    protected @Inject OAuth2ResourceConfigurator rc;
	
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
		sc.enable(true);
		
		configureAuthzServer(ac.enable());
		configureResourceServer(rc.enable());
	}
	
	protected void configureAuthzServer(OAuth2ServerConfigurator c) {
	    c.useInMemoryStore();
	    
	    //Enables in-memory store for testing.
	    OAuth2InMemoryStore ms = c.inMemoryStore();
	    
	    ms.addClient(TEST_CLIENT_ID, TEST_CLIENT_SECRET, TEST_CLIENT_REDIRECT_URI);
	    
	    ms.addClient(new OAuth2ClientBuilder("app2", "app2_secret").setRedirectUriPattern("http*://*/app2/oauth2_redirect").build());
	    ms.addClient(new OAuth2ClientBuilder("app3", "app3_secret").setRedirectUriPattern("http*://*/app3/auth_redirect").build());
	}
	
    protected void configureResourceServer(OAuth2ResourceConfigurator c) {

    }
}