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
package leap.web.api.spec.swagger;

import leap.core.annotation.Inject;
import leap.lang.http.HTTP;
import leap.web.App;
import leap.web.Request;
import leap.web.Response;
import leap.web.api.Apis;
import leap.web.api.config.ApiConfig;
import leap.web.api.config.ApiConfigProcessor;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.ApiMetadataBuilder;
import leap.web.api.meta.ApiMetadataContext;
import leap.web.api.meta.ApiMetadataProcessor;
import leap.web.api.spec.ApiSpecContext;

public class SwaggerProcessor implements ApiConfigProcessor,ApiMetadataProcessor {
	
	private static final String SWAGGER_JSON_FILE = "swagger.json";

	protected @Inject App  app;
	protected @Inject Apis apis;

	@Override
	public void preProcess(ApiConfigurator c) {
		app.routes().create().get(getJsonSpecPath(c.config()), (req, resp) -> {
			handleJsonSpecRequest(c.config(), req, resp, c.config().getName());
		}).enableCors()
		  .allowAnonymous()
		  .apply();
	}
	
	@Override
    public void preProcess(ApiMetadataContext context, ApiMetadataBuilder m) {
		m.getPaths().remove("/" + SWAGGER_JSON_FILE);
    }

	void handleJsonSpecRequest(ApiConfig c, Request req, Response resp, String name) throws Throwable {
		ApiMetadata m = apis.tryGetMetadata(name);
        if(null == m) {
            resp.setStatus(HTTP.SC_NOT_FOUND);
            return;
        }
		
		SwaggerJsonWriter w = new SwaggerJsonWriter();
		w.setPropertyNamingStyle(c.getPropertyNamingStyle());
		
		resp.setContentType(w.getContentType());
		w.write(new ApiSpecContextImpl(req), m, resp.getWriter());
	}
	
	protected String getJsonSpecPath(ApiConfig c) {
		return c.getBasePath() + "/" + SWAGGER_JSON_FILE;
	}

    private static final class ApiSpecContextImpl implements ApiSpecContext {
        private final Request request;

        public ApiSpecContextImpl(Request request) {
            this.request = request;
        }

        @Override
        public String getHost() {
            return request.getServletRequest().getServerName();
        }

        @Override
        public int getPort() {
            return request.getServletRequest().getServerPort();
        }
    }

}