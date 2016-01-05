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
package app;

import leap.core.annotation.Inject;
import leap.lang.naming.NamingStyles;
import leap.oauth2.rs.OAuth2ResServerConfigurator;
import leap.web.App;
import leap.web.api.Apis;
import leap.web.config.WebConfigurator;

public class Global extends App {
	
    protected @Inject Apis                        apis;
    protected @Inject OAuth2ResServerConfigurator rsc;
	
	@Override
    protected void configure(WebConfigurator c) {
	    //Enables oauth2 resource server.
	    rsc.enable()
	       .useRemoteAuthzServer("https://localhost:8443/auth/oauth2/tokeninfo");
	    
	    apis.setDefaultOAuthAuthorizationUrl("http://127.0.0.1:8080/auth/oauth2/authorize", 
	                                         "app", 
	                                         "http://localhost:8080/oauth2_redirect")
	        .setDefaultOAuthTokenUrl("http://127.0.0.1:8080/auth/oauth2/token");
	    
	    //Add simple api.
		apis.add("api",    "/api");
		
		//Add Box api
		apis.add("boxapi", "/boxapi")
			.setPropertyNamingStyle(NamingStyles.LOWER_UNDERSCORE)
			.removeModelNamePrefixes("Box");
	}

}