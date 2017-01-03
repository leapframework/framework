/*
 * Copyright 2013 the original author or authors.
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

import app.interceptors.RoutesInterceptor;
import leap.core.annotation.Inject;
import leap.lang.Assert;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.App;
import leap.web.Results;
import leap.web.assets.AssetConfigurator;
import leap.web.config.WebConfigurator;
import leap.web.config.WebInterceptors;
import leap.web.route.Routes;

public class Global extends App {
    private static final Log log = LogFactory.get(Global.class);
	
	public static final String APPLICATION_START_CALLED_ATTRIBUTE = "application_start_called";
	public static final String APPLICATION_INIT_CALLED_ATTRIBUTE  = "application_init_called";

    private @Inject AssetConfigurator ac;

    @Override
    protected void intercepting(WebInterceptors interceptors) {
        //for testing external routes.
        interceptors.add(factory.createBean(RoutesInterceptor.class));
    }

    @Override
    protected void configure(WebConfigurator c) {
        ac.addFolder(getTempDir().createRelative("./assets").getFilepath());
    }

    @Override
	protected void routing(Routes routes) {
		routes
				.get("/handler/get", (req, resp) -> Results.text("Hello Handler"))

				.get("/handler/test.json", () -> Results.json("Hello Json"));
	}
	
	@Override
    protected void init() throws Throwable {
		Boolean called = (Boolean)getServletContext().getAttribute(APPLICATION_INIT_CALLED_ATTRIBUTE);
		Assert.isNull(called,"init already called");
		getServletContext().setAttribute(APPLICATION_INIT_CALLED_ATTRIBUTE, true);
    }
	
	@Override
    protected void start() throws Throwable {
		Boolean called = (Boolean)getServletContext().getAttribute(APPLICATION_START_CALLED_ATTRIBUTE);
		Assert.isNull(called,"start already called");
		getServletContext().setAttribute(APPLICATION_START_CALLED_ATTRIBUTE, true);
	}

	@Override
    protected void stop() throws Throwable {

    }
}