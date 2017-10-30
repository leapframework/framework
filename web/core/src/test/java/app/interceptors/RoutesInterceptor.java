/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package app.interceptors;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ioc.PostCreateBean;
import leap.lang.intercepting.State;
import leap.web.Request;
import leap.web.RequestInterceptor;
import leap.web.Response;
import leap.web.SimpleRouter;
import leap.web.action.ActionContext;
import leap.web.annotation.Path;
import leap.web.annotation.Restful;
import leap.web.annotation.http.GET;
import leap.web.route.DefaultRoutes;
import leap.web.route.RouteManager;
import leap.web.route.Routes;

public class RoutesInterceptor implements RequestInterceptor, PostCreateBean {

    private static final String PATH_PREFIX = "/test_router/";

    private @Inject RouteManager rm;

    private Routes routes;

    @Override
    public void postCreate(BeanFactory factory) throws Throwable {
        routes = factory.createBean(DefaultRoutes.class);

        rm.loadRoutesFromController(routes, new SimpleController());
        rm.loadRoutesFromController(routes, new RestfulController());
        rm.loadRoutesFromController(routes, new RootController());
    }

    @Override
    public void onPrepareRequest(Request request, Response response) {
        if(request.getPath().startsWith(PATH_PREFIX)) {

            String path = request.getPath().substring(PATH_PREFIX.length()-1);

            if(request.hasQueryParameter("not_found")) {
                request.setExternalRouter(new SimpleRouter(routes, path){
                    @Override
                    public boolean handleNotFound(Request request, Response response, String path) {
                        response.setStatus(404);
                        response.getWriter().write("not found");
                        return true;
                    }
                });
            }else{
                request.setExternalRouter(new SimpleRouter(routes, path));
            }
        }
    }
    private class SimpleController {

        public String action1(String p) {
            return p;
        }

    }

    @Restful
    private class RestfulController {

        public String action1(String p) {
            return p;
        }

        @GET("/action2")
        public String action2(String p) {
            return p;
        }
    }

    @Path("/")
    private class RootController {

        public String action1(String p) {
            return p;
        }

    }

}
