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

import leap.core.AppConfig;
import leap.core.annotation.Inject;
import leap.lang.New;
import leap.lang.Strings;
import leap.lang.http.HTTP;
import leap.lang.http.Headers;
import leap.web.Handler;
import leap.web.Request;
import leap.web.Response;
import leap.web.api.Api;
import leap.web.api.Apis;
import leap.web.api.config.ApiConfig;
import leap.web.api.config.ApiConfigProcessor;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.ApiMetadataBuilder;
import leap.web.api.meta.ApiMetadataContext;
import leap.web.api.meta.ApiMetadataProcessor;
import leap.web.api.spec.ApiSpecContext;
import leap.web.assets.AssetStrategy;
import leap.web.route.RouteConfigurator;
import leap.web.route.Routes;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

public class SwaggerProcessor implements ApiConfigProcessor,ApiMetadataProcessor {
	
	private static final String SWAGGER_JSON_FILE = "swagger.json";
	private static final String SWAGGER_JSON_ANONYMOUS = "swagger.anonymous";

	protected @Inject Apis          apis;
	protected @Inject AssetStrategy assetStrategy;
	protected @Inject AppConfig     appConfig;

	@Override
    public void preProcess(ApiMetadataContext context, ApiMetadataBuilder m) {
		m.getPaths().remove("/" + SWAGGER_JSON_FILE);
    }

    @Override
    public void completeProcess(ApiMetadataContext context, ApiMetadata m) {
        ApiConfig config = context.getConfig();

        Routes routes = config.getContainerRoutes();

        RouteConfigurator route = routes.create()
                .enableCors()
                .get(getJsonSpecPath(config, routes), new Handler() {
                    @Override
                    public void handle(Request request, Response response) throws Throwable {
                        handleJsonSpecRequest(context.getApi(), request, response);
                    }

                    @Override
                    public String toString() {
                        return SwaggerProcessor.class.getSimpleName() + "(swagger.json)";
                    }
                });

        if (appConfig.getBooleanProperty(SWAGGER_JSON_ANONYMOUS, true)) {
            route.allowAnonymous();
        }

        context.getApi().getConfigurator().addDynamicRoute(route.build(), false);
    }

    void handleJsonSpecRequest(Api api, Request req, Response resp) throws Throwable {
		SwaggerJsonWriter w = new SwaggerJsonWriter();
		w.setPropertyNamingStyle(api.getConfig().getPropertyNamingStyle());

		resp.setContentType(w.getContentType());

        String[] parts = req.getParameterValues("parts");
        if(null != parts && parts.length == 1) {
            parts = Strings.split(parts[0], ',');
        }
        Set<String> partsSet = null == parts ? Collections.emptySet() : New.hashSet(parts);

        ApiSpecContext context = new ApiSpecContextImpl(req, partsSet);
        String json = toSwaggerJson(w, context, api.getMetadata());
        String fingerprint = assetStrategy.getFingerprint(json.getBytes());

        resp.setHeader(Headers.ETAG, "\"" + fingerprint + "\"");

        String ifNoneMatch = req.getHeader(Headers.IF_NONE_MATCH);
        if(!Strings.isEmpty(ifNoneMatch) && ifNoneMatch.equals("\"" + fingerprint + "\"")) {
            resp.setStatus(HTTP.SC_NOT_MODIFIED);
        }else {
            resp.getWriter().write(json);
        }
	}

    protected String toSwaggerJson(SwaggerJsonWriter w, ApiSpecContext context, ApiMetadata md) throws IOException {
        StringBuilder out = new StringBuilder();
        w.write(context, md, out);
        return out.toString();
    }
	
	protected String getJsonSpecPath(ApiConfig c, Routes routes) {
	    if(Strings.isEmpty(routes.getPathPrefix())) {
            return c.getBasePath() + "/" + SWAGGER_JSON_FILE;
        }else {
            return "/" + SWAGGER_JSON_FILE;
        }
	}

    protected static class ApiSpecContextImpl implements ApiSpecContext {
        private final Request     request;
        private final Set<String> parts;

        public ApiSpecContextImpl(Request request,Set<String> parts) {
            this.request = request;
            this.parts   = parts;
        }

        @Override
        public String getHost() {
            return request.getServletRequest().getServerName();
        }

        @Override
        public int getPort() {
            return request.getServletRequest().getServerPort();
        }

        @Override
        public String getContextPath() {
            return request.getContextPath();
        }

        @Override
        public Set<String> getParts() {
            return parts;
        }
    }
    
}